package eu.europeana.entitymanagement.batch.config;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"}, ignoreResourceNotFound = true)
public class MongoBatchDatasourceConfig {

    private static final Logger logger = LogManager.getLogger(MongoBatchDatasourceConfig.class);

    @Value("${mongo.batch.database}")
    private String batchDatabase;

    /**
     * Configures Morphia data store for the batch job repository
     *
     * @param mongoClient Mongo connection. Should already be configured in application, so we suppress the auto-wiring
     *                    warning here.
     * @return data store
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public Datastore batchDataStore(MongoClient mongoClient) {
        logger.info("Connecting to Batch database {}", batchDatabase);
        return Morphia.createDatastore(mongoClient, batchDatabase);
    }
}
