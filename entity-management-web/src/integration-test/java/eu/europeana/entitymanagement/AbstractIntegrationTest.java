package eu.europeana.entitymanagement;


import eu.europeana.entitymanagement.testutils.MongoContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

public abstract class AbstractIntegrationTest {

    static final MongoContainer MONGO_CONTAINER;

    static {
        MONGO_CONTAINER = new MongoContainer("entity-management", "job-repository")
                .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

        MONGO_CONTAINER.start();
    }


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongo.em.database", MONGO_CONTAINER::getBatchDb);
        registry.add("mongo.batch.database", MONGO_CONTAINER::getEntityDb);
    }


}
