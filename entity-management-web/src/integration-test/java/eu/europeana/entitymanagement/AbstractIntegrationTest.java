package eu.europeana.entitymanagement;


import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.EMPTY_METIS_RESPONSE;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.METIS_RESPONSE_MAP;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_BNF_URI_WIKIDATA_PATH_SUFFIX;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_BNF_WIKIDATA_RESPONSE_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import eu.europeana.entitymanagement.testutils.MongoContainer;
import eu.europeana.entitymanagement.testutils.SolrContainer;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.io.IOException;
import java.util.Objects;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public abstract class AbstractIntegrationTest {
    private static final Logger logger = LogManager.getLogger(AbstractIntegrationTest.class);
    private static final MongoContainer MONGO_CONTAINER;
    private static final SolrContainer SOLR_CONTAINER;


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
     */
    private static MockWebServer mockMetis;
    private static MockWebServer mockWikidata;

    @Autowired
    protected EntityRecordService entityRecordService;

    @BeforeAll
    public static void setupAll() throws IOException {
        mockMetis = new MockWebServer();
        mockMetis.setDispatcher(setupMetisDispatcher());
        mockMetis.start();

        mockWikidata = new MockWebServer();
        mockWikidata.setDispatcher(setupWikidataDispatcher());
        mockWikidata.start();
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
        registry.add("wikidata.baseUrl", () -> String.format("http://%s:%s", mockWikidata.getHostName(), mockWikidata.getPort()));
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


    private static Dispatcher setupMetisDispatcher() {
        return new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) throws InterruptedException {
                String externalId = Objects.requireNonNull(request.getRequestUrl())
                    .queryParameter("uri");
                try {
                    String responseBody = loadFile(
                        METIS_RESPONSE_MAP.getOrDefault(externalId, EMPTY_METIS_RESPONSE));
                    return new MockResponse()
                        .setResponseCode(200)
                        .setBody(responseBody);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    private static Dispatcher setupWikidataDispatcher() {
        return new Dispatcher() {

            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                try {
                    if (ORGANIZATION_BNF_URI_WIKIDATA_PATH_SUFFIX.equals(request.getPath())) {
                        String responseBody = loadFile(ORGANIZATION_BNF_WIKIDATA_RESPONSE_XML);
                        return new MockResponse().setResponseCode(200).setBody(responseBody);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // for now, only one request is mocked in tests
                return new MockResponse().setResponseCode(404);
            }
        };
    }

}
