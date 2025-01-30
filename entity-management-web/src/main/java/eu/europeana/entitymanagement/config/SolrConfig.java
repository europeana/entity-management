package eu.europeana.entitymanagement.config;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;

@Configuration
public class SolrConfig {

  private static final Logger logger = LogManager.getLogger(SolrConfig.class);
  private final EntityManagementConfiguration emConfiguration;

  @Autowired
  public SolrConfig(EntityManagementConfiguration emConfiguration) {
    this.emConfiguration = emConfiguration;
  }

  @Bean(AppConfigConstants.BEAN_PR_SOLR_CLIENT)
  public SolrClient pageRankSolrClient() {
    if (StringUtils.isNotBlank(emConfiguration.getPrSolrZookeeperUrl())) {
      return initSolrCloudClient(emConfiguration.getPrSolrZookeeperUrl(), emConfiguration.getIndexingSolrTimeoutMillis(),
          emConfiguration.getPrSolrCollection());
    } else {
      return initSolrClient(emConfiguration.getPrSolrUrl(), emConfiguration.getIndexingSolrTimeoutMillis());
    }
  }
    
  @Bean(AppConfigConstants.BEAN_INDEXING_SOLR_CLIENT)
  public SolrClient indexingSolrClient() {
    if (StringUtils.isNotBlank(emConfiguration.getIndexingSolrZookeeperUrl())) {
      return initSolrCloudClient(emConfiguration.getIndexingSolrZookeeperUrl(), emConfiguration.getIndexingSolrTimeoutMillis(),
          emConfiguration.getIndexingSolrCollection());
    } else {
      return initSolrClient(emConfiguration.getIndexingSolrUrl(), emConfiguration.getIndexingSolrTimeoutMillis());
    }
  }

  private SolrClient initSolrClient(String solrUrl, int timeoutMillis) {
    logger.info(
        "Configuring solr client at the url: {}", solrUrl);

    if (solrUrl.contains(",")) {
      LBHttpSolrClient.Builder builder = new LBHttpSolrClient.Builder();
      return builder
          .withBaseSolrUrls(solrUrl.split(","))
          .withConnectionTimeout(timeoutMillis)
          .build();
    } else {
      HttpSolrClient.Builder builder = new HttpSolrClient.Builder();
      return builder
          .withBaseSolrUrl(solrUrl)
          .withConnectionTimeout(timeoutMillis)
          .build();
    }
  }

  private SolrClient initSolrCloudClient(String solrZookeeperUrl, int timeout, String solrCollection) {
    logger.info(
        "Configuring solr client with the zookeperurls: {} and collection: {}",
        solrZookeeperUrl,
        solrCollection);

    String[] solrZookeeperUrlsList = solrZookeeperUrl.trim().split(",");

    CloudSolrClient client =
        new CloudSolrClient.Builder(Arrays.asList(solrZookeeperUrlsList), Optional.empty())
            .withConnectionTimeout(timeout)
            .build();

    client.setDefaultCollection(solrCollection);
    return client;
  }
}
