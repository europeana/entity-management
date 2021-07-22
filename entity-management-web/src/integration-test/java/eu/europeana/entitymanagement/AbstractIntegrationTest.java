package eu.europeana.entitymanagement;


import eu.europeana.entitymanagement.testutils.MongoContainer;
import eu.europeana.entitymanagement.testutils.SolrContainer;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.io.IOException;
import java.util.Objects;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public abstract class AbstractIntegrationTest {
    private static final Logger logger = LogManager.getLogger(AbstractIntegrationTest.class);
    private static final MongoContainer MONGO_CONTAINER;
    private static final SolrContainer SOLR_CONTAINER;

    protected MockMvc mockMvc;

    static {
        MONGO_CONTAINER = new MongoContainer("entity-management", "job-repository", "enrichment")
                .withLogConsumer(new WaitingConsumer()
                        .andThen(new ToStringConsumer()));

        MONGO_CONTAINER.start();

        SOLR_CONTAINER = new SolrContainer("entity-management")
                .withLogConsumer(new WaitingConsumer()
                        .andThen(new ToStringConsumer()));

        SOLR_CONTAINER.start();
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

    @Autowired
    protected EntityRecordService entityRecordService;


    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    protected void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }

    @BeforeAll
    public static void setupAll() throws IOException {
        mockMetis = new MockWebServer();

        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String externalId = Objects.requireNonNull(request.getRequestUrl()).queryParameter("uri");
                try {
                    String responseBody = loadFile(METIS_RESPONSE_MAP.getOrDefault(externalId, EMPTY_METIS_RESPONSE));
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody(responseBody);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        mockMetis.setDispatcher(dispatcher);
        mockMetis.start();
    }

    @AfterAll
    public static void teardownAll() throws IOException {
        logger.info("Shutdown metis server : host = {}; port={}", mockMetis.getHostName(), mockMetis.getPort());
        mockMetis.shutdown();
    }







    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongo.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongo.em.database", MONGO_CONTAINER::getEntityDb);
        registry.add("mongo.batch.database", MONGO_CONTAINER::getBatchDb);
        // enrichment database on the same test Mongo instance
        registry.add("mongo.enrichment.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongo.enrichment.database", MONGO_CONTAINER::getEnrichmentDb);
        registry.add("metis.baseUrl", () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
        registry.add("batch.computeMetrics", () -> "false");
        // Do not run scheduled entity updates in tests
        registry.add("batch.scheduling.enabled", () -> "false");
        registry.add("auth.enabled", () -> "false");
        registry.add("entitymanagement.solr.indexing.url", SOLR_CONTAINER::getConnectionUrl);
        // enable explicit commits while indexing to Solr in tests
        registry.add("entitymanagement.solr.indexing.explicitCommits", () -> true);
        // override setting in .properties file in case this is enabled
        registry.add("metis.proxy.enabled", () -> false);

        logger.info("MONGO_CONTAINER : {}", MONGO_CONTAINER.getConnectionUrl());
        logger.info("SOLR_CONTAINER : {}", SOLR_CONTAINER.getConnectionUrl());
        logger.info("METIS SERVER : host = {}; port={}", mockMetis.getHostName(), mockMetis.getPort());
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
