package eu.europeana.entitymanagement.solr;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.exception.InvalidSearchQueryException;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fetches documents from Solr using a cursor.
 * See https://solr.apache.org/guide/7_6/pagination-of-results.html#fetching-a-large-number-of-sorted-results-cursors
 */
public class SolrSearchCursorIterator {
    private static final String UNDEFINED_FIELD = "undefined field";
    private static final String CANNOT_PARSE_TEXT_FRAGMENT = "org.apache.solr.search.SyntaxError: Cannot parse ";


    private final DocumentObjectBinder objectBinder = new DocumentObjectBinder();
    private final Logger log = LogManager.getLogger(SolrSearchCursorIterator.class);

    private final SolrClient client;
    private final SolrQuery solrQuery;

    private String cursorMark;
    private String previousCursorMark;

    public SolrSearchCursorIterator(SolrClient client, SolrQuery solrQuery) {
        validateQueryFields(solrQuery);
        ensureSortClause(solrQuery);

        this.solrQuery = solrQuery;
        this.client = client;
        this.cursorMark = CursorMarkParams.CURSOR_MARK_START;
    }



    /**
     * Checks if additional documents can be retrieved for the Solr query
     * @return true if documents can be retrieved, false otherwise
     */
    public boolean hasNext() {
        return !cursorMark.equals(previousCursorMark);
    }

    /**
     * Retrieves the next chunk of documents that match the search query.
     */
    public <T extends Entity> List<SolrEntity<T>> next() throws SolrServiceException {
        solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
        QueryResponse response;

        try {
            response = client.query(solrQuery);
        } catch (HttpSolrClient.RemoteSolrException e) {
            throw handleRemoteSolrException(solrQuery, e);
        } catch (SolrServerException | IOException ex) {
            throw new SolrServiceException(String.format("Error while searching Solr q=%s", solrQuery.getQuery()), ex);
        }

        previousCursorMark = cursorMark;
        cursorMark = response.getNextCursorMark();

        if(log.isDebugEnabled()) {
            log.debug("Performed Solr search query in {}ms: numFound={}, cursorMark={}, q={}",
                    response.getElapsedTime(), response.getResults().getNumFound(),
                    cursorMark, solrQuery.getQuery());
        }


        SolrDocumentList documents = response.getResults();
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }

        return documents.stream()
                .<SolrEntity<T>>map(this::convertFromSolrDoc)
                .collect(Collectors.toList());
    }


    private <T extends Entity> SolrEntity<T> convertFromSolrDoc(SolrDocument solrDocument) {

        Object fieldValue = solrDocument.getFieldValue(EntitySolrFields.TYPE);
        Assert.notNull(fieldValue, "Solr document type cannot be empty");

        return objectBinder.getBean(SolrUtils.getSolrEntityClass(fieldValue.toString()), solrDocument);

    }


    /**
     * 'id' and 'type' fields are required.
     * If query specifies fields, both must be included
     *
     * @param solrQuery query object
     */
    private void validateQueryFields(SolrQuery solrQuery) {
        String fieldString = solrQuery.getFields();

        if (!StringUtils.hasLength(fieldString)) {
            return;
        }

        List<String> fields = Arrays.asList(fieldString.split(","));
        if (!Set.of(EntitySolrFields.TYPE, EntitySolrFields.ID).containsAll(fields)) {
            throw new IllegalArgumentException("SolrQuery fields must either be empty or contain id and type");
        }
    }

    /**
     * Cursor functionality requires a sort clause in the query
     * @param solrQuery query object
     */
    private void ensureSortClause(SolrQuery solrQuery) {
        if(CollectionUtils.isEmpty(solrQuery.getSorts())){
            throw new IllegalArgumentException("SolrQuery must specify a sort with a unique field");
        }
    }


    private SolrServiceException handleRemoteSolrException(SolrQuery searchQuery, HttpSolrClient.RemoteSolrException e) {
        String remoteMessage = e.getMessage();

        if (remoteMessage.contains(UNDEFINED_FIELD)) {
            // invalid search field
            int startPos = remoteMessage.indexOf(UNDEFINED_FIELD) + UNDEFINED_FIELD.length();
            String fieldName = remoteMessage.substring(startPos);
            return new InvalidSearchQueryException("Unknown field '" + fieldName + "' in search query");
        } else if (remoteMessage.contains(CANNOT_PARSE_TEXT_FRAGMENT)) {
            return new InvalidSearchQueryException("Invalid syntax in search query");
        }

        int separatorPos = remoteMessage.lastIndexOf(':');
        if (separatorPos > 0) {
            // remove server url from remote message
            remoteMessage = remoteMessage.substring(separatorPos + 1);
        }
        return new SolrServiceException("An error occurred when searching entities: " + searchQuery.toString()
                + ", remote message: " + remoteMessage, e);
    }
}
