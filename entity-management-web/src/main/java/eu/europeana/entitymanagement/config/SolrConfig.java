package eu.europeana.entitymanagement.config;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
        logger.info("Configuring indexing solr client at the url: {}", emConfiguration.getIndexingSolrUrl());
        return new HttpSolrClient.Builder(emConfiguration.getIndexingSolrUrl()).build();
    }
}
