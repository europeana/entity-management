package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.any;
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

    private MockMvc mockMvc;
    private static final MockWebServer mockMetis = new MockWebServer();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("metis.baseUrl", () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
    }

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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
    public void registerEntity_concept() throws Exception {
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
