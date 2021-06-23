package eu.europeana.entitymanagement;


import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.testutils.MongoContainer;
import eu.europeana.entitymanagement.testutils.SolrContainer;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {
    protected static final Logger logger = LogManager.getLogger(AbstractIntegrationTest.class);

    static final MongoContainer MONGO_CONTAINER;
    static final SolrContainer SOLR_CONTAINER;


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
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected EntityRecordService entityRecordService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @BeforeAll
    static void setupAll() {
        mockMetis = new MockWebServer();
    }

    @AfterAll
    static void teardownAll() throws IOException {
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
        registry.add("auth.enabled", () -> "false");
        registry.add("entitymanagement.solr.indexing.url", SOLR_CONTAINER::getConnectionUrl);
        // enable explicit commits while indexing to Solr in tests
        registry.add("entitymanagement.solr.indexing.explicitCommits", () -> true);


        logger.info("MONGO_CONTAINER : {}", MONGO_CONTAINER.getConnectionUrl());
        logger.info("SOLR_CONTAINER : {}", SOLR_CONTAINER.getConnectionUrl());
        logger.info("METIS SERVER : host = {}; port={}", mockMetis.getHostName(), mockMetis.getPort());
    }
    
    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
       
        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }
    
    protected MvcResult createTestEntityRecord(String europeanaMetadataFile, String metisResponseFile, boolean forUpdate)
    	    throws IOException, Exception {
    	// set mock Metis response
            mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));
            //second request for update task during create
            mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));
            //third request of update when update method is called
            if(forUpdate) {
                mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));  
            }

        	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                    .content(loadFile(europeanaMetadataFile))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isAccepted())
                    .andReturn();
    	return resultRegisterEntity;
        }
}
