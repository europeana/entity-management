package eu.europeana.entitymanagement.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.LanguageCodes;

/**
 * 
 * @author GordeaS
 *
 */
@Configuration
public class AppConfig {

    private static final Logger LOG = LogManager.getLogger(AppConfig.class);
    
    public static final String BEAN_EM_CONFIGURATION = "emConfiguration";
    public static final String BEAN_EM_DATA_SOURCES = "emDataSources";
    public static final String BEAN_EM_LANGUAGE_CODES = "emLanguageCodes";
    public static final String BEAN_EM_VALIDATOR_FACTORY = "emValidatorFactory";
    public static final String BEAN_CLIENT_DETAILS_SERVICE = "clientDetailsService";
    
    @Resource(name=BEAN_EM_CONFIGURATION)
    private EntityManagementConfiguration emConfiguration;
    
    public AppConfig() {
	LOG.info("Initializing EntityManagementConfiguration bean as: configuration");
    }
    
    @Bean(name=BEAN_EM_DATA_SOURCES)
    public DataSources getDataSources() throws IOException {
	XmlMapper xmlMapper = new XmlMapper();
	String datasourcesXMLConfigFile = emConfiguration.getDatasourcesXMLConfig();
		
	try (InputStream inputStream = getClass().getResourceAsStream(datasourcesXMLConfigFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
	    String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
	    return xmlMapper.readValue(contents, DataSources.class);
	}
    }

    @Bean(name=BEAN_EM_LANGUAGE_CODES)
    public LanguageCodes getLanguageCodes() throws IOException {
	XmlMapper xmlMapper = new XmlMapper();
	String languagecodesXMLConfig = emConfiguration.getLanguagecodesXMLConfig();
	try (InputStream inputStream = getClass().getResourceAsStream(languagecodesXMLConfig);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
	    String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
	    return xmlMapper.readValue(contents, LanguageCodes.class);
	}
    }

    @Bean(name=BEAN_EM_VALIDATOR_FACTORY)
    public ValidatorFactory getValidatorFactoryBean() {
	return new LocalValidatorFactoryBean();
    }

    @Bean(name=BEAN_CLIENT_DETAILS_SERVICE)
    public EuropeanaClientDetailsService getClientDetailsService() {
	return new EuropeanaClientDetailsService();
    }
    
}
