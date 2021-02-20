package eu.europeana.entitymanagement.mongo.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"}, ignoreResourceNotFound = true)
public class DataSourceConfig {

    private static final Logger logger = LogManager.getLogger(DataSourceConfig.class);


    @Value("${mongo.connectionUrl}")
    private String hostUri;


    @Value("${mongo.em.database}")
    private String emDatabase;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(hostUri);
    }

    @Primary
    @Bean(AppConfigConstants.BEAN_EM_DATA_STORE)
    public Datastore emDataStore(MongoClient mongoClient) {
        logger.info("Connecting to Entity Management database {}", emDatabase);
        return Morphia.createDatastore(mongoClient, emDatabase);
    }
}