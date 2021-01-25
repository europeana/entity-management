package eu.europeana.entitymanagement.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Container for all settings that we load from the entitymanagement.properties
 * file and optionally override from myapi.user.properties file
 */
@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
public class EMSettings {

    private static final Logger LOG = LogManager.getLogger(EMSettings.class);

    @Value("${datasources.config}")
    private String datasourcesXMLConfig;

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

//    @Bean
    public DataSources getDataSources() throws IOException {
	XmlMapper xmlMapper = new XmlMapper();
	try (InputStream inputStream = getClass().getResourceAsStream(datasourcesXMLConfig);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
	    String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
	    return xmlMapper.readValue(contents, DataSources.class);
	}
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
