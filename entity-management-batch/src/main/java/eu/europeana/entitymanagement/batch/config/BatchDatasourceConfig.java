package eu.europeana.entitymanagement.batch.config;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.AnnotationBuilder;
import dev.morphia.annotations.Embedded;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:entitymanagement.properties", "classpath:entitymanagement.user.properties"}, ignoreResourceNotFound = true)
public class BatchDatasourceConfig {

    private static final Logger logger = LogManager.getLogger(BatchDatasourceConfig.class);

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


        Datastore datastore = Morphia.createDatastore(mongoClient, batchDatabase);

        // map Spring Batch entities to Morphia @Embedded entities
        // see: https://github.com/MorphiaOrg/morphia/issues/1520#issuecomment-727014617
//        datastore.getMapper().mapExternal(null, JobExecution.class);
//        datastore.getMapper().mapExternal(null, StepExecution.class);
//        datastore.getMapper().mapExternal(null, ExecutionContext.class);
//        datastore.getMapper().mapExternal(null, JobInstance.class);
        return datastore;
    }
}
