package eu.europeana.entitymanagement.mongo.config;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

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
//        return MongoClients.create(hostUri);
	CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
	CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
	return MongoClients.create(MongoClientSettings.builder()
		.applyConnectionString(new ConnectionString(hostUri)).codecRegistry(codecRegistry).build());
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
