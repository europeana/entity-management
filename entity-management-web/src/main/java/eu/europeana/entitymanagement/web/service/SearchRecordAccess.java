package eu.europeana.entitymanagement.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.ParamValidationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.Map;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPathWithBase;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;

/**
 * Class for accessing SR API
 * @author srishti singh
 * @since 26 November 2025
 */
public class SearchRecordAccess {

    protected static final Logger logger = LogManager.getLogger(SearchRecordAccess.class);

    /** Query fields for entity types */
    private static final Map<String, String> ENRICHMENT_QUERY_FIELD_MAP =
            Map.of(
                    EntityTypes.Agent.getEntityType(), "edm_agent",
                    EntityTypes.Concept.getEntityType(), "skos_concept",
                    EntityTypes.Place.getEntityType(), "edm_place",
                    EntityTypes.TimeSpan.getEntityType(), "edm_timespan",
                    EntityTypes.Organization.getEntityType(), "foaf_organization",
                    EntityTypes.Aggregator.getEntityType(), "foaf_organization"
            );

    private static final String contentTierPrefix = " AND contentTier:";


    @Resource
    protected EntityManagementConfiguration configuration;

    protected final HttpConnection httpConnection;
    protected ObjectMapper mapper;
    protected AuthenticationHandler auth;

    /**
     * Constructor
     * @param auth authentication for accessing SR api
     */
    public SearchRecordAccess(AuthenticationHandler auth) {
        this.auth = auth;
        httpConnection = new HttpConnection(true);
        mapper = new ObjectMapper();
    }


    /**
     * Build the search api url for retrieving depiction for a given entity
     * @param entityUri id
     * @return URL
     * @throws ParamValidationException
     */
    protected String buildSearchDepictionRequestUrl(String entityUri) throws ParamValidationException {
        try {
            return new URIBuilder(configuration.getSearchApiUrlPrefix())
                    .addParameter("query",
                            "\"" +entityUri + "\" AND provider_aggregation_edm_isShownBy:*&sort=contentTier+desc,metadataTier+desc&profile=minimal&rows=1")
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new ParamValidationException("Error building the search request url - " + e.getMessage(), e);
        }
    }


    /**
     * Build the url for retrieving enrichment count from search and record api
     * @param entity entity
     * @return URL
     * @throws ParamValidationException
     */
    protected String buildEnrichmentCountRequestUrl(Entity entity) throws ParamValidationException {
        try {
            return new URIBuilder(configuration.getSearchApiUrlPrefix())
                    .addParameter("query", buildEnrichmentCountSearchQuery(entity))
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new ParamValidationException("Error building the search request url - " + e.getMessage(), e);
        }
    }

    private String buildEnrichmentCountSearchQuery(Entity entity) {
        StringBuilder searchQuery = new StringBuilder(50); // resized as atleast 35 characters are appended
        searchQuery.append(String.format(
                "%s:%s ", ENRICHMENT_QUERY_FIELD_MAP.get(entity.getType()), getEntityIdsForQuery(entity)));

        if (!EntityTypes.isOrganizationType(entity.getType())) {
            searchQuery.append(contentTierPrefix);
            searchQuery.append(configuration.getEnrichmentsQueryContentTier());
        }
        // no rows needed, only the count
        searchQuery.append("&profile=minimal&rows=0");
        return searchQuery.toString();
    }

    /**
     * EntityID format is different in Search API. So we need to add the /base/ namespace when
     * querying for enrichment counts.
     *
     * <p>TODO: This should be changed when entities are re-indexed in Search API with the "correct"
     * ids (EA-2944 suport both URIs with and without /base/ in the path)
     */
    private String getEntityIdsForQuery(Entity entity) {
        // not applicable for timespans
        if (EntityTypes.isTimeSpan(entity.getType())) {
            return "\"" + entity.getEntityId() + "\"";
        }

        //for the organizations search also for all data.europeana.eu uris from the sameAs
        if(EntityTypes.isOrganizationType(entity.getType())) {
            return buildSearchedIdsForOrganizations(entity);
        }

        // EA-2944 suport both URIs with and without /base/ in the path
        StringBuilder entityIdsBuilder = new StringBuilder("(\"");
        entityIdsBuilder
                .append(BASE_DATA_EUROPEANA_URI)
                .append(getEntityRequestPathWithBase(entity.getEntityId()))
                .append("\" OR \"")
                .append(BASE_DATA_EUROPEANA_URI)
                .append(getEntityRequestPath(entity.getEntityId()))
                .append("\")");

        return entityIdsBuilder.toString();
    }

    /**
     * Build list of search ids to match and organization (org id and corefs)
     * @param entity organization
     * @return search query
     */
    String buildSearchedIdsForOrganizations(Entity entity) {
        StringBuilder orgIdsBuilder = new StringBuilder("(\"");
        orgIdsBuilder.append(entity.getEntityId());

        if(entity.getSameReferenceLinks()!=null) {
            for(String sameAsUri : entity.getSameReferenceLinks()) {
                if(sameAsUri.startsWith(BASE_DATA_EUROPEANA_URI)) {
                    orgIdsBuilder.append("\" OR \"");
                    orgIdsBuilder.append(sameAsUri);
                }
            }
        }
        orgIdsBuilder.append("\")");
        return orgIdsBuilder.toString();
    }

}
