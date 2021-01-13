package eu.europeana.entitymanagement.config;

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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Container for all settings that we load from the entitymanagement.properties file and optionally override from
 * myapi.user.properties file
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
    
    @Value("${entitymanagement.api.version}")
    private String entitymanagementApiVersion;
    
	public String getEntitymanagementApiVersion() {
		return entitymanagementApiVersion;
	}

    @Value("${entity.environment}")
    private String entityEnvironment;
    
    public String getEntityEnvironment() {
		return entityEnvironment;
	}

	@Value("${europeana.apikey.jwttoken.siganturekey}")
    private String europeanaApikeyJwttokenSiganturekey;
    
    public String getEuropeanaApikeyJwttokenSiganturekey() {
		return europeanaApikeyJwttokenSiganturekey;
	}

	@Value("${authorization.api.name}")
    private String authorizationApiName;
    
    public String getAuthorizationApiName() {
		return authorizationApiName;
	}

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
    
    @PostConstruct
    private void logImportantSettings() {
        LOG.info("MyAPI settings:");

    }
}
