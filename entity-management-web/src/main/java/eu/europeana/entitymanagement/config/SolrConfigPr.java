package eu.europeana.entitymanagement.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;

@Configuration
@EnableSolrRepositories
public class SolrConfigPr {

	@Autowired
    public SolrConfigPr(EntityManagementConfiguration emConfiguration) {
		this.emConfiguration=emConfiguration;
	}

	private static final Logger logger = LogManager.getLogger(SolrConfigPr.class);

	final EntityManagementConfiguration emConfiguration;
	
	@Bean(AppConfigConstants.BEAN_PR_SOLR_CLIENT)
	@Autowired
	public SolrClient getPrSolrClient() {
		logger.info("Configuring the solr client at the url: {}", emConfiguration.getPrSolrUrl());
		return new HttpSolrClient.Builder(emConfiguration.getPrSolrUrl()).build();
	}
	
	@Bean
	@Autowired
	public SolrTemplate solrTemplate() throws Exception {
		return new SolrTemplate(getPrSolrClient());
	}
}
