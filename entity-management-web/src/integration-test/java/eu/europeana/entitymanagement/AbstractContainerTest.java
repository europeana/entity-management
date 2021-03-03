package eu.europeana.entitymanagement;


import eu.europeana.entitymanagement.testutils.MongoContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

public abstract class AbstractContainerTest {

    static final MongoContainer MONGO_CONTAINER;
    private static final WaitingConsumer wait = new WaitingConsumer();
    private static final ToStringConsumer toString = new ToStringConsumer();

    static {
        MONGO_CONTAINER = new MongoContainer("em_itest_user", "password1", "entity-management", "job-repository")
                .withLogConsumer(wait.andThen(toString));

        MONGO_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongo.em.database", MONGO_CONTAINER::getBatchDb);
        registry.add("mongo.batch.database", MONGO_CONTAINER::getEntityDb);
    }


}
