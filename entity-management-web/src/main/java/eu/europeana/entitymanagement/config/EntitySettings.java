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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Container for all settings that we load from the entitymanagement.properties file and optionally override from
 * entitymanagement.user.properties file
 */
@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
public class EntitySettings {

    private static final Logger LOG = LogManager.getLogger(EntitySettings.class);

    @Value("${datasources.config}")
    private String datasourcesXMLConfig;
    
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
    

}
