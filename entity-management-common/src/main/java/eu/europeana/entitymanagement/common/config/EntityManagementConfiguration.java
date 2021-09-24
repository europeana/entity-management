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

    @Value("${europeana.apikey.jwttoken.signaturekey}")
    private String apiKeyPublicKey;

    @Value("${europeana.thumbnail.urlPrefix}")
    private String thumbnailBaseUrl;

    @Value("${europeana.apikey.serviceurl}")
    private String apiKeyUrl;

    @Value("${entitymanagement.solr.pr.url}")
    private String prSolrUrl;

    @Value("${europeana.searchapi.urlPrefix}")
    private String searchApiUrlPrefix;


  @Value("${europeana.searchapi.enrichments.contentTier}")
  private String enrichmentsQueryContentTier;

    @Value("${entitymanagement.solr.indexing.url}")
    private String indexingSolrUrl;

    @Value("${entitymanagement.solr.indexing.explicitCommits: false}")
    private boolean explicitCommitsEnabled;

    @Value("${entitymanagement.solr.indexing.query.maxPageSize: 100}")
    private int solrQueryMaxPageSize;


    @Value("${entitymanagement.solr.searchapi.hits.query}")
    private String hitsQuery;

    @Value("${authorization.api.name}")
    private String authorizationApiName;

    @Value("${metis.baseUrl}")
    private String metisBaseUrl;

    @Value("${batch.step.chunkSize: 10}")
    private int batchChunkSize;

  @Value("${batch.step.executor.corePool: 10}")
  private int batchStepExecutorCorePool;

  @Value("${batch.step.executor.maxPool: 100}")
  private int batchStepExecutorMaxPool;

  @Value("${batch.step.executor.queueSize: 50}")
  private int batchStepExecutorQueueSize;

  @Value("${batch.step.throttleLimit: 10}")
  private int batchStepThrottleLimit;

  @Value("${batch.computeMetrics: false}")
  private boolean batchComputeMetrics;

  @Value("${auth.enabled: true}")
  private boolean authEnabled;

  @Value("${enrichmentMigrationPassword}")
  private String enrichmentsMigrationPassword;

  @Value("${metis.proxy.enabled: false}")
  private boolean useMetisProxy;

  @Value("${metis.proxy.url:}")
  private String metisProxyUrl;


  public EntityManagementConfiguration() {
	LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
    }

    
    public String getPrSolrUrl() {
	return prSolrUrl;
    }

    
    public String getSearchApiUrlPrefix() {
	return searchApiUrlPrefix;
    }

    
    public String getHitsQuery() {
	return hitsQuery;
    }


  public String getApiKeyPublicKey() {
    return apiKeyPublicKey;
  }


  public String getApiKeyUrl() {
    return apiKeyUrl;
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

  public boolean isAuthEnabled() {
    return authEnabled;
  }

  public String getEnrichmentsMigrationPassword() {
    return enrichmentsMigrationPassword;
  }

  public int getBatchStepThrottleLimit() {
    return batchStepThrottleLimit;
  }


public String getIndexingSolrUrl() {
	return indexingSolrUrl;
}

    public boolean explicitCommitsEnabled() {
        return explicitCommitsEnabled;
    }

    public int getSolrQueryMaxPageSize() {
      return solrQueryMaxPageSize;
    }

    public boolean useMetisProxy() {
        return useMetisProxy;
    }

    public String getMetisProxyUrl() {
        return metisProxyUrl;
    }

    public String getThumbnailBaseUrl() {
        return thumbnailBaseUrl;
    }

  public String getEnrichmentsQueryContentTier() {
    return enrichmentsQueryContentTier;
  }
}
