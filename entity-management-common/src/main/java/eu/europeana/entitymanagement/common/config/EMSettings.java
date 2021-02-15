package eu.europeana.entitymanagement.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Container for all settings that we load from the entitymanagement.properties
 * file and optionally override from myapi.user.properties file
 */
@Configuration
@PropertySources
({
@PropertySource("classpath:entitymanagement.properties"),
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
})

public class EMSettings {

    private static final Logger LOG = LogManager.getLogger(EMSettings.class);

    @Value("${datasources.config}")
    private String datasourcesXMLConfig;

    @Value("${languagecodes.config}")
    private String languagecodesXMLConfig;
    
    @Value("${altLabelFieldNamePrefix}")
    private String altLabelFieldNamePrefix;

    public String getAltLabelFieldNamePrefix() {
		return altLabelFieldNamePrefix;
	}

	@Value("${prefLabelFieldNamePrefix}")
    private String prefLabelFieldNamePrefix;

    public String getPrefLabelFieldNamePrefix() {
		return prefLabelFieldNamePrefix;
	}

	@Value("${languageSeparator}")
    private String languageSeparator;

    public String getLanguageSeparator() {
		return languageSeparator;
	}

	@Value("${entitymanagement.api.version}")
    private String entitymanagementApiVersion;

//    @Value("${entity.environment}")
//    private String entityEnvironment;

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

    public String getEntitymanagementApiVersion() {
	return entitymanagementApiVersion;
    }

//    public String getEntityEnvironment() {
//	return entityEnvironment;
//    }

    public String getEuropeanaApikeyJwttokenSiganturekey() {
	return europeanaApikeyJwttokenSiganturekey;
    }

    public String getAuthorizationApiName() {
	return authorizationApiName;
    }

//    @Value("${default.user.token}")
//    private String defaultUserToken;

//    public String getDefaultUserToken() {
//	return defaultUserToken;
//    }

	@Bean
	public DataSources getDataSources() throws IOException {
    	XmlMapper xmlMapper = new XmlMapper();
    	try (InputStream inputStream = getClass().getResourceAsStream(datasourcesXMLConfig);
    		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {    		    
    		    String contents = reader.lines()
    		      .collect(Collectors.joining(System.lineSeparator()));
    		    return xmlMapper.readValue(contents, DataSources.class);	
    		}    	         
	}
	
	@Bean
	public LanguageCodes getLanguageCodes() throws IOException {
    	XmlMapper xmlMapper = new XmlMapper();
    	try (InputStream inputStream = getClass().getResourceAsStream(languagecodesXMLConfig);
    		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {    		    
    		    String contents = reader.lines()
    		      .collect(Collectors.joining(System.lineSeparator()));
    		    return xmlMapper.readValue(contents, LanguageCodes.class);	
    		}    	         
	}
	
	@Bean
	public javax.validation.Validator localValidatorFactoryBean() {
	   return new LocalValidatorFactoryBean();
	}

    
    @PostConstruct
    private void logImportantSettings() {
        LOG.info("MyAPI settings:");
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


    @Bean
    public WebClient metisWebClient() {
	return WebClient.builder().baseUrl(metisBaseUrl).build();
    }
}
