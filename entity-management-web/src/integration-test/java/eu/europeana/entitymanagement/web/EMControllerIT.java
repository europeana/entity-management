package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import java.io.IOException;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static eu.europeana.entitymanagement.web.EMController.BASE_URI_DATA;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the main Entity Management controller
 */

@SpringBootTest
@AutoConfigureMockMvc
public class EMControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EntityRecordService entityRecordService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    /**
     * MockWebServer needs to be static, so we can inject it's port into the Spring context.
     * <p>
     * Since the WebServer is re-used across tests, every test interacting with Metis NEEDS to call assertMetisRequest()
     * to clear its queue!
     * <p>
     * See: https://github.com/spring-projects/spring-framework/issues/24825
     */

    private static MockWebServer mockMetis;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("metis.baseUrl", () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
    }

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }


    @BeforeAll
    static void setupAll() {
        mockMetis = new MockWebServer();
    }

    @AfterAll
    static void teardownAll() throws IOException {
        mockMetis.shutdown();
    }

    /**
     * Test that WebApplicationContext loads correctly
     */
    @Test
    public void shouldLoadCorrectly() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean(EMController.class));
    }


    @Test
    public void registerEntityShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(BATHTUB_DEREF)));
        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_BATHTUB))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)));
        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q11019");
    }


    @Test
    void retrieveEntityShouldBeSuccessful() throws Exception {
        // directly insert test data in db
        EntityPreview entityRequest = objectMapper.readValue(loadFile(CONCEPT_BATHTUB), EntityPreview.class);
        EntityRecord dbRecord = entityRecordService.createEntityFromRequest(entityRequest, EntityTypes.Concept.name());

        String requestPath = getEntityRequestPath(dbRecord.getEntityId());
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(dbRecord.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));
    }

    /**
     * Asserts that a de-reference request was actually made to the mock Metis server
     *
     * @param uri uri param in request
     * @throws InterruptedException
     */
    private void assertMetisRequest(String uri) throws InterruptedException {
        // check that app actually sent request
        HttpUrl requestedUrl = mockMetis.takeRequest().getRequestUrl();
        assert requestedUrl != null;
        Assertions.assertEquals(METIS_DEREF_PATH, requestedUrl.encodedPath());
        Assertions.assertEquals(uri, requestedUrl.queryParameter("uri"));
    }
}

