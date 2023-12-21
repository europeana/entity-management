package eu.europeana.entitymanagement.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Container for all settings that we load from the entitymanagement.properties file and optionally
 * override from entitymanagement.user.properties file
 */
@Configuration
@PropertySources({
  @PropertySource("classpath:entitymanagement.properties"),
  @PropertySource(
      value = "entitymanagement.user.properties",
      ignoreResourceNotFound = true)
})
public class EntityManagementConfiguration implements InitializingBean {

  private static final Logger LOG = LogManager.getLogger(EntityManagementConfiguration.class);
  /** Matches spring.profiles.active property in test/resource application.properties file */
  public static final String ACTIVE_TEST_PROFILE = "test";

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

  @Value("${entitymanagement.solr.indexing.url:}")
  private String indexingSolrUrl;

  @Value("${entitymanagement.solr.indexing.zookeeper.url:}")
  private String indexingSolrZookeeperUrl;

  @Value("${entitymanagement.solr.indexing.timeout:60000}")
  private int indexingSolrTimeoutMillis;

  @Value("${entitymanagement.solr.indexing.collection}")
  private String indexingSolrCollection;

  @Value("${entitymanagement.solr.indexing.explicitCommits: false}")
  private boolean explicitCommitsEnabled;

  @Value("${entitymanagement.solr.indexing.query.maxPageSize: 500}")
  private int solrQueryMaxPageSize;

  @Value("${entitymanagement.solr.searchapi.hits.query}")
  private String hitsQuery;

  @Value("${authorization.api.name}")
  private String authorizationApiName;

  @Value("${metis.baseUrl}")
  private String metisBaseUrl;

  @Value("${wikidata.baseUrl:}")
  private String wikidataBaseUrl;

  @Value("${batch.step.chunkSize: 10}")
  private int batchChunkSize;

  @Value("${batch.step.updates.executor.corePool: 10}")
  private int batchUpdatesCorePoolSize;

  @Value("${batch.step.updates.executor.maxPool: 100}")
  private int batchUpdatesMaxPoolSize;

  @Value("${batch.step.updates.executor.queueSize: 50}")
  private int batchUpdatesQueueSize;

  @Value("${batch.step.updates.throttleLimit: 10}")
  private int batchUpdatesThrottleLimit;

  @Value("${batch.step.removals.executor.corePool: 2}")
  private int batchRemovalsCorePoolSize;

  @Value("${batch.step.removals.executor.maxPool: 10}")
  private int batchRemovalsMaxPoolSize;

  @Value("${batch.step.removals.executor.queueSize: 5}")
  private int batchRemovalsQueueSize;

  @Value("${batch.step.removals.throttleLimit: 2}")
  private int batchRemovalsThrottleLimit;

  @Value("${batch.computeMetrics: false}")
  private boolean batchComputeMetrics;

  @Value("${batch.maxFailedTaskRetries: 3}")
  private int maxFailedTaskRetries;

  @Value("${auth.read.enabled: true}")
  private boolean authReadEnabled;

  @Value("${auth.write.enabled: true}")
  private boolean authWriteEnabled;

  @Value("${metis.proxy.enabled: false}")
  private boolean useMetisProxy;

  @Value("${metis.proxy.url:}")
  private String metisProxyUrl;

  @Value("${entitymanagement.entityIdResponseMaxSize: 100}")
  private int entityIdResponseMaxSize;

  @Value("${zoho.sync.filter.owner:DPS Team}")
  private String zohoSyncOwnerFilter;

  @Value("${zoho.sync.batch.size: 100}")
  private int zohoSyncBatchSize;

  @Value("${zoho.generate.organization.europeanaid: false}")
  private boolean generateOrganizationEuropeanaId;
  
  @Value("${zoho.sync.register.deprecated: false}")
  private boolean registerDeprecated;
  
  @Value("${europeana.item.data.endpoint:'http://data.europeana.eu/item'}")
  private String itemDataEndpoint;

  @Value("${europeana.scheme.data.endpoint:'http://data.europeana.eu/scheme'}")
  private String schemeDataEndpoint;

  @Value("${spring.profiles.active:}")
  private String activeProfileString;
  
  @Value("${zoho.country.mapping.file:#{null}}")
  private String zohoCountryMappingFile;

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

  public int getBatchUpdatesCorePoolSize() {
    return batchUpdatesCorePoolSize;
  }

  public int getBatchUpdatesMaxPoolSize() {
    return batchUpdatesMaxPoolSize;
  }

  public int getBatchUpdatesQueueSize() {
    return batchUpdatesQueueSize;
  }

  public boolean shouldComputeMetrics() {
    return batchComputeMetrics;
  }

  public boolean isAuthReadEnabled() {
    return authReadEnabled;
  }

  public boolean isAuthWriteEnabled() {
    return authWriteEnabled;
  }

  public int getBatchUpdatesThrottleLimit() {
    return batchUpdatesThrottleLimit;
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

  /**
   * Gets a custom base url to use for Wikidata dereference requests. Mainly used for mocking
   * wikidata responses in integration tests
   */
  public String getWikidataBaseUrl() {
    return wikidataBaseUrl;
  }

  public int getBatchRemovalsCorePoolSize() {
    return batchRemovalsCorePoolSize;
  }

  public int getBatchRemovalsMaxPoolSize() {
    return batchRemovalsMaxPoolSize;
  }

  public int getBatchRemovalsQueueSize() {
    return batchRemovalsQueueSize;
  }

  public int getBatchRemovalsThrottleLimit() {
    return batchRemovalsThrottleLimit;
  }

  public int getMaxFailedTaskRetries() {
    return maxFailedTaskRetries;
  }

  public int getEntityIdResponseMaxSize() {
    return entityIdResponseMaxSize;
  }

  public String getZohoSyncOwnerFilter() {
    return zohoSyncOwnerFilter;
  }

  public int getZohoSyncBatchSize() {
    return zohoSyncBatchSize;
  }

  public String getIndexingSolrZookeeperUrl() {
    return indexingSolrZookeeperUrl;
  }

  public int getIndexingSolrTimeoutMillis() {
    return indexingSolrTimeoutMillis;
  }

  public String getIndexingSolrCollection() {
    return indexingSolrCollection;
  }

  public String getItemDataEndpoint() {
    return itemDataEndpoint;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (isNotTestProfile(activeProfileString)) {
      verifyRequiredProperties();
    }
  }

  public static boolean isNotTestProfile(String activeProfileString) {
    return Arrays.stream(activeProfileString.split(",")).noneMatch(ACTIVE_TEST_PROFILE::equals);
  }

  /** verify properties */
  private void verifyRequiredProperties() {
    List<String> missingProps = new ArrayList<>();

    // search api prefix is mandatory
    if (StringUtils.isBlank(getSearchApiUrlPrefix())) {
      missingProps.add("europeana.searchapi.urlPrefix");
    }

    // one of zookeeperUrl or solr indexing url are
    if (StringUtils.isBlank(getIndexingSolrZookeeperUrl())
        && StringUtils.isBlank(getIndexingSolrUrl())) {
      missingProps.add(
          "entitymanagement.solr.indexing.url/entitymanagement.solr.indexing.zookeeper.url");
    }

    // collection name is mandatory when using zookeeper url
    if (StringUtils.isNotBlank(getIndexingSolrZookeeperUrl())
        && StringUtils.isBlank(getIndexingSolrCollection())) {
      missingProps.add("entitymanagement.solr.indexing.collection");
    }

    if (!missingProps.isEmpty()) {
      throw new IllegalStateException(
          String.format(
              "The following config properties are not set: %s", String.join("\n", missingProps)));
    }
  }

  public String getSchemeDataEndpoint() {
    return schemeDataEndpoint;
  }

  public void setSchemeDataEndpoint(String schemeDataEndpoint) {
    this.schemeDataEndpoint = schemeDataEndpoint;
  }

  public boolean isGenerateOrganizationEuropeanaId() {
    return generateOrganizationEuropeanaId;
  }

  public String getZohoCountryMappingFile() {
    return zohoCountryMappingFile;
  }
  
  public boolean isRegisterDeprecated() {
    return registerDeprecated;
  }

}
