package eu.europeana.entitymanagement.config;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
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
    logger.info("Configuring pageRank solr client at the url: {}", emConfiguration.getPrSolrUrl());
    return new HttpSolrClient.Builder(emConfiguration.getPrSolrUrl()).build();
  }

  @Bean(AppConfigConstants.BEAN_INDEXING_SOLR_CLIENT)
  public SolrClient indexingSolrClient() {
    if (StringUtils.isNotBlank(emConfiguration.getIndexingSolrZookeeperUrl())) {
      return initSolrCloudClient();
    } else {
      return initSolrClient();
    }
  }

  private SolrClient initSolrClient() {
    logger.info(
        "Configuring indexing solr client at the url: {}", emConfiguration.getIndexingSolrUrl());
    
    if (emConfiguration.getIndexingSolrUrl().contains(",")) {
      LBHttpSolrClient.Builder builder = new LBHttpSolrClient.Builder();
      return builder
          .withBaseSolrUrls(emConfiguration.getIndexingSolrUrl().split(","))
          .withConnectionTimeout(emConfiguration.getIndexingSolrTimeoutMillis())
          .build();
    } else {
      HttpSolrClient.Builder builder = new HttpSolrClient.Builder();
      return builder
          .withBaseSolrUrl(emConfiguration.getIndexingSolrUrl())
          .withConnectionTimeout(emConfiguration.getIndexingSolrTimeoutMillis())
          .build();
    }
  }

  private SolrClient initSolrCloudClient() {
    logger.info(
        "Configuring indexing solr client with the zookeperurls: {} and collection: {}", emConfiguration.getIndexingSolrUrl(), emConfiguration.getIndexingSolrCollection());
    
    String[] solrZookeeperUrls = emConfiguration.getIndexingSolrZookeeperUrl().trim().split(",");

    CloudSolrClient client =
        new CloudSolrClient.Builder(Arrays.asList(solrZookeeperUrls), Optional.empty())
            .withConnectionTimeout(emConfiguration.getIndexingSolrTimeoutMillis())
            .build();

    client.setDefaultCollection(emConfiguration.getIndexingSolrCollection());
    return client;
  }
}
