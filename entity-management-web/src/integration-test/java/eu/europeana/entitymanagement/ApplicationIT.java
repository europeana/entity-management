package eu.europeana.entitymanagement;

import eu.europeana.entitymanagement.testutils.MongoContainer;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {
    private static String EM_DB_USER = "api_user";
    private static String EM_DB_PASSWORD = "password";

    private static String EM_APP_DB = "entity-management";
    private static String EM_BATCH_DB = "job-repository";


    @Rule
    static final MongoContainer mongo = new MongoContainer()
            .withEnv("EM_DB_USER", EM_DB_USER)
            .withEnv("EM_DB_PASSWORD", EM_DB_PASSWORD)
            .withEnv("EM_APP_DB", EM_APP_DB)
            .withEnv("EM_BATCH_DB", EM_BATCH_DB);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.connectionUrl", () -> String.format("mongodb://%s:%s@%s:%d", EM_DB_USER, EM_DB_PASSWORD, mongo.getContainerIpAddress(), mongo.getMappedPort(27017)));
        registry.add("mongo.em.database", () -> EM_APP_DB);
        registry.add("mongo.batch.database", () -> EM_BATCH_DB);
    }

    @Test
    public void contextLoads() {
    }

}