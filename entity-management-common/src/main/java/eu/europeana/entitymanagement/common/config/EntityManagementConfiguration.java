package eu.europeana.entitymanagement.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Container for all settings that we load from the entitymanagement.properties
 * file and optionally override from entitymanagement.user.properties file
 */
@Configuration
@PropertySources({ @PropertySource("classpath:entitymanagement.properties"),
	@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true) })
public class EntityManagementConfiguration  {

    private static final Logger LOG = LogManager.getLogger(EntityManagementConfiguration.class);

    @Value("${datasources.config}")
    private String datasourcesXMLConfig;

    @Value("${languagecodes.config}")
    private String languagecodesXMLConfig;

    @Value("${europeana.apikey.jwttoken.siganturekey}")
    private String europeanaApikeyJwttokenSiganturekey;

    @Value("${entitymanagement.solr.pr.url}")
    private String prSolrUrl;

    @Value("${entitymanagement.solr.searchapi.url}")
    private String searchApiSolrUrl;

    @Value("${entitymanagement.solr.searchapi.enrichments.query}")
    private String enrichmentsQuery;

    @Value("${entitymanagement.solr.searchapi.hits.query}")
    private String hitsQuery;

    @Value("${authorization.api.name}")
    private String authorizationApiName;

    @Value("${metis.baseUrl}")
    private String metisBaseUrl;

    @Value("${batch.chunkSize: 10}")
    private int batchChunkSize;

    @Value("${batch.job.executor.corePool: 10}")
    private int batchJobExecutorCorePool;

    @Value("${batch.job.executor.maxPool: 100}")
    private int batchJobExecutorMaxPool;

    @Value("${batch.job.executor.queueSize: 50}")
    private int batchJobExecutorQueueSize;

  @Value("${batch.step.executor.corePool: 10}")
  private int batchStepExecutorCorePool;

  @Value("${batch.step.executor.maxPool: 100}")
  private int batchStepExecutorMaxPool;

  @Value("${batch.step.executor.queueSize: 50}")
  private int batchStepExecutorQueueSize;

  @Value("${batch.computeMetrics: false}")
  private boolean batchComputeMetrics;



  public EntityManagementConfiguration() {
	LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
    }

    
    public String getPrSolrUrl() {
	return prSolrUrl;
    }

    
    public String getSearchApiSolrUrl() {
	return searchApiSolrUrl;
    }

    
    public String getEnrichmentsQuery() {
	return enrichmentsQuery;
    }

    
    public String getHitsQuery() {
	return hitsQuery;
    }

    
    public String getJwtTokenSignatureKey() {
	return europeanaApikeyJwttokenSiganturekey;
    }

    
    public String getAuthorizationApiName() {
	return authorizationApiName;
    }

    
    public String getMetisBaseUrl() {
	return metisBaseUrl;
    }

    
    public String getDatasourcesXMLConfig() {
	return datasourcesXMLConfig;
    }

    
    public String getLanguagecodesXMLConfig() {
	return languagecodesXMLConfig;
    }


  public int getBatchChunkSize() {
    return batchChunkSize;
  }

  public int getBatchJobExecutorCorePool() {
    return batchJobExecutorCorePool;
  }

  public int getBatchJobExecutorMaxPool() {
    return batchJobExecutorMaxPool;
  }

  public int getBatchJobExecutorQueueSize() {
    return batchJobExecutorQueueSize;
  }

  public int getBatchStepExecutorCorePool() {
    return batchStepExecutorCorePool;
  }

  public int getBatchStepExecutorMaxPool() {
    return batchStepExecutorMaxPool;
  }

  public int getBatchStepExecutorQueueSize() {
    return batchStepExecutorQueueSize;
  }

  public boolean shouldComputeMetrics() {
    return batchComputeMetrics;
  }
}
