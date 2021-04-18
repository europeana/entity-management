package eu.europeana.entitymanagement;


import eu.europeana.entitymanagement.testutils.MongoContainer;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.io.IOException;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;

public abstract class AbstractIntegrationTest {

    static final MongoContainer MONGO_CONTAINER;

    protected Logger logger = LogManager.getLogger(getClass());

    static {
        MONGO_CONTAINER = new MongoContainer("entity-management", "job-repository")
                .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

        MONGO_CONTAINER.start();
    }


    /**
     * MockWebServer needs to be static, so we can inject its port into the Spring context.
     * <p>
     * Since the WebServer is reused across tests, every test interacting with Metis NEEDS to call assertMetisRequest()
     * to clear its queue!
     * <p>
     * See: https://github.com/spring-projects/spring-framework/issues/24825
     */
    protected static MockWebServer mockMetis;

    @BeforeAll
    static void setupAll() {
        mockMetis = new MockWebServer();
    }

    @AfterAll
    static void teardownAll() throws IOException {
        mockMetis.shutdown();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongo.em.database", MONGO_CONTAINER::getEntityDb);
        registry.add("mongo.batch.database", MONGO_CONTAINER::getBatchDb);
        registry.add("metis.baseUrl", () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
    }


    /**
     * Asserts that a de-reference request was actually made to the mock Metis server
     *
     * @param uri uri param in request
     * @throws InterruptedException
     */
    protected void assertMetisRequest(String uri) throws InterruptedException {
        // check that app actually sent request
        HttpUrl requestedUrl = mockMetis.takeRequest().getRequestUrl();
        assert requestedUrl != null;
        Assertions.assertEquals(METIS_DEREF_PATH, requestedUrl.encodedPath());
        Assertions.assertEquals(uri, requestedUrl.queryParameter("uri"));
    }
}
