package eu.europeana.entitymanagement.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Container for all settings that we load from the entitymanagement.properties
 * file and optionally override from myapi.user.properties file
 */
@Configuration(AppConfigConstants.BEAN_EM_CONFIGURATION)
@PropertySources({ @PropertySource("classpath:entitymanagement.properties"),
	@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true) })
public class EntityManagementConfigurationImpl implements EntityManagementConfiguration {

    private static final Logger LOG = LogManager.getLogger(EntityManagementConfigurationImpl.class);

    @Value("${datasources.config}")
    private String datasourcesXMLConfig;

    @Value("${languagecodes.config}")
    private String languagecodesXMLConfig;

//    @Value("${altLabelFieldNamePrefix}")
//    private String altLabelFieldNamePrefix;
//
//    @Value("${prefLabelFieldNamePrefix}")
//    private String prefLabelFieldNamePrefix;
//
//    @Value("${languageSeparator}")
//    private String languageSeparator;

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

    public EntityManagementConfigurationImpl() {
	LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
    }

    @Override
    public String getPrSolrUrl() {
	return prSolrUrl;
    }

    @Override
    public String getSearchApiSolrUrl() {
	return searchApiSolrUrl;
    }

    @Override
    public String getEnrichmentsQuery() {
	return enrichmentsQuery;
    }

    @Override
    public String getHitsQuery() {
	return hitsQuery;
    }

    @Override
    public String getJwtTokenSignatureKey() {
	return europeanaApikeyJwttokenSiganturekey;
    }

    @Override
    public String getAuthorizationApiName() {
	return authorizationApiName;
    }

    @Override
    public String getMetisBaseUrl() {
	return metisBaseUrl;
    }

    @Override
    public String getDatasourcesXMLConfig() {
	return datasourcesXMLConfig;
    }

    @Override
    public String getLanguagecodesXMLConfig() {
	return languagecodesXMLConfig;
    }

}
