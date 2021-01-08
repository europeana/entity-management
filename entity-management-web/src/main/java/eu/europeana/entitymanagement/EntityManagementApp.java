package eu.europeana.entitymanagement;

import eu.europeana.entitymanagement.config.SocksProxyConfig;
import eu.europeana.entitymanagement.util.SocksProxyActivator;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main application. Allows deploying as a war and logs instance data when deployed in Cloud Foundry
 */
@SpringBootApplication(scanBasePackages = "eu.europeana.entitymanagement")
public class EntityManagementApp extends SpringBootServletInitializer {

    /**
     * Main entry point of this application
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
        SocksProxyActivator.activate(new SocksProxyConfig("entitymanagement.properties", "entitymanagement.user.properties"));

        SpringApplication.run(EntityManagementApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EntityManagementApp.class);
    }

}
