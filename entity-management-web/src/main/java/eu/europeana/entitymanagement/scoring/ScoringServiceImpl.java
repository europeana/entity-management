package eu.europeana.entitymanagement.scoring;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.config.EMSettings;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.EntityRetrievalException;
import eu.europeana.entitymanagement.scoring.model.EntityMetrics;
import eu.europeana.entitymanagement.scoring.model.PageRank;

@Component("scoringService")
public class ScoringServiceImpl implements ScoringService {

    SolrClient prSolrClient;

    SolrClient searchApiSolrClient;

    @Autowired
    private EMSettings emSettings;
    
    public static final String WIKIDATA_PREFFIX = "http://www.wikidata.org/entity/";
    public static final String WIKIDATA_DBPEDIA_PREFIX = "http://wikidata.dbpedia.org/resource/";


    @Override
    public EntityMetrics computeMetrics(Entity entity) {
	EntityMetrics metrics = new EntityMetrics(entity.getEntityId());
	
	PageRank pr = getPageRank(entity);
	if(pr != null) {
	    metrics.setPageRank(pr.getPageRank());		    
	}
	
	metrics.setEnrichmentCount(getEnrichmentCount(entity));
	metrics.setHitCount(getHitCount(entity));
	computeScore(metrics);
	return metrics;
    }

    private Integer getHitCount(Entity entity) {
	return -1;
    }

    private void computeScore(EntityMetrics metrics) {
	metrics.setScore(1);
    }

    private Integer getEnrichmentCount(Entity entity) {
	String queryStr = emSettings.getEnrichmentsQuery();
	queryStr = String.format(queryStr, entity.getEntityId());
	SolrQuery query = new SolrQuery(queryStr);
	query.setRows(0);

	try {
	    QueryResponse rsp =  getSearchApiSolrClient().query(query);
	    return (int) rsp.getResults().getNumFound();
	} catch (Exception e) {
	    throw new EntityRetrievalException(
		    "Unexpected exception occured when retrieving pagerank: " + entity.getEntityId(), e);
	}
    }

    private PageRank getPageRank(Entity entity) {
	SolrQuery query = new SolrQuery();
	String wikidataUrl = getWikidataUrl(entity);
	
	if(wikidataUrl == null) {
	    return null;
	}
	
	query.setQuery("page_url:\"" + wikidataUrl + "\"");
//	getLogger().trace("query: " + query);

	try {
	    QueryResponse rsp = getPrSolrClient().query(query);
	    List<PageRank> beans = rsp.getBeans(PageRank.class);
	    if(beans.isEmpty()) {
		return null;
	    }else {
		return beans.get(0);
	    }
	} catch (Exception e) {
	    throw new EntityRetrievalException(
		    "Unexpected exception occured when retrieving pagerank: " + wikidataUrl, e);
	}
    }

    private String getWikidataUrl(Entity entity) {
	if(entity.getSameAs() == null) {
	    return null;
	}
	List<String> values = Arrays.asList(entity.getSameAs());
		    
	String wikidataUri = values.stream().filter(value -> value.startsWith(WIKIDATA_PREFFIX)).findFirst().orElse(null);
	return wikidataUri;
    }

    public SolrClient getPrSolrClient() {
	if (prSolrClient == null) {
	    prSolrClient = new HttpSolrClient.Builder(emSettings.getPrSolrUrl()).build();
	}
	return prSolrClient;
    }

    public SolrClient getSearchApiSolrClient() {
	if (searchApiSolrClient == null) {
	    searchApiSolrClient = new HttpSolrClient.Builder(emSettings.getSearchApiSolrUrl()).build();
	}
	return searchApiSolrClient;
    }

}
