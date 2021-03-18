package eu.europeana.entitymanagement.mongo.config;

import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MAPPER_OPTIONS;

import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;

@Configuration
@PropertySource(value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"}, ignoreResourceNotFound = true)
public class DataSourceConfig {

    private static final Logger logger = LogManager.getLogger(DataSourceConfig.class);


    @Value("${mongo.connectionUrl}")
    private String hostUri;


    @Value("${mongo.em.database}")
    private String emDatabase;

    @Value("${mongo.batch.database}")
    private String batchDatabase;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(hostUri);
    }

    @Primary
    @Bean(AppConfigConstants.BEAN_EM_DATA_STORE)
    public Datastore emDataStore(MongoClient mongoClient) {
        logger.info("Configuring Entity Management database: {}", emDatabase);
        return Morphia.createDatastore(mongoClient, emDatabase);
    }

    /**
     * Configures Morphia data store for the batch job repository
     *
     * @param mongoClient Mongo connection
     * @return data store for Spring batch JobRepository
     */
    @Bean(name = AppConfigConstants.BEAN_BATCH_DATA_STORE)
    public Datastore batchDataStore(MongoClient mongoClient) {
        logger.info("Configuring Batch database: {}", batchDatabase);
        return Morphia.createDatastore(mongoClient, batchDatabase);
    }
}
