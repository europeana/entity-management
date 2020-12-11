package eu.europeana.entitymanagement.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

/**
 * Container for all settings that we load from the entitymanagement.properties file and optionally override from
 * myapi.user.properties file
 */
@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:myapi.user.properties", ignoreResourceNotFound = true)
public class EMSettings {

    private static final Logger LOG = LogManager.getLogger(EMSettings.class);

    @PostConstruct
    private void logImportantSettings() {
        LOG.info("MyAPI settings:");

    }
}
