package eu.europeana.entitymanagement;

import org.apache.logging.log4j.LogManager;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import eu.europeana.entitymanagement.common.config.SocksProxyConfig;
import eu.europeana.entitymanagement.config.SocksProxyActivator;

/**
 * Main application. Allows deploying as a war and logs instance data when deployed in Cloud Foundry
 */
//@SpringBootApplication(scanBasePackages = {"eu.europeana.entitymanagement", "eu.europeana.api.commons"},
@SpringBootApplication(scanBasePackages = {"eu.europeana.entitymanagement"},
        exclude = {
                // Remove these exclusions to re-enable security
                SecurityAutoConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                // Required so batch jobs aren't automatically started / configured
                BatchAutoConfiguration.class,
                DataSourceAutoConfiguration.class
        }
)
@EnableBatchProcessing
public class EntityManagementApp extends SpringBootServletInitializer {

    /**
     * Main entry point of this application
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        // When deploying to Cloud Foundry, this will log the instance index number, IP and GUID
        LogManager.getLogger(EntityManagementApp.class).
                info("CF_INSTANCE_INDEX  = {}, CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}",
                        System.getenv("CF_INSTANCE_INDEX"),
                        System.getenv("CF_INSTANCE_GUID"),
                        System.getenv("CF_INSTANCE_IP"));

        // Activate socks proxy (if your application requires it)
        SocksProxyActivator.activate(new SocksProxyConfig("entitymanagement.properties", "myapi.user.properties"));

        SpringApplication.run(EntityManagementApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EntityManagementApp.class);
    }

}
