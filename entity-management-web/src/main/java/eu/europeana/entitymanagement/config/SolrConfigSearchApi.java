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
public class SolrConfigSearchApi {

	@Autowired
    public SolrConfigSearchApi(EntityManagementConfiguration emConfiguration) {
		this.emConfiguration=emConfiguration;
	}

	private static final Logger logger = LogManager.getLogger(SolrConfigSearchApi.class);

	final EntityManagementConfiguration emConfiguration;
		
	@Bean(AppConfigConstants.BEAN_SEARCH_API_SOLR_CLIENT)
	@Autowired
	public SolrClient searchApiSolrClient() {
		logger.info("Configuring the solr client at the url: {}", emConfiguration.getSearchApiSolrUrl());
		return new HttpSolrClient.Builder(emConfiguration.getSearchApiSolrUrl()).build();
	}

	@Bean
	@Autowired
	public SolrTemplate searchApiSolrTemplate() throws Exception {
		return new SolrTemplate(searchApiSolrClient());
	}

}
