package eu.europeana.entitymanagement.common.config;

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
import org.springframework.context.annotation.PropertySources;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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

   
}
