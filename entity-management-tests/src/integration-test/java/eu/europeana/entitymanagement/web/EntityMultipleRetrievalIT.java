package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityMultipleRetrievalIT extends BaseWebControllerTest{

    private static final String requestPath = "retrieve";
    private static List<String> entityIds = new ArrayList<>();

    @BeforeEach
    public void setup() throws Exception {
        // create 1st entity
        String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
        String entityId = createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
                        .getEntityId();
        entityIds.add(entityId);

        // add second entity
        europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
        metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);
        entityId = createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI)
                        .getEntityId();
        entityIds.add(entityId);

        Assertions.assertNotNull(entityIds);
        Assertions.assertEquals(2, entityIds.size());

    }

    @Test
    public void multipleEntitiesRetrievedSuccessfully() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(
                                        IntegrationTestUtils.BASE_SERVICE_URL
                                                + "/"
                                                + requestPath)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(entityIds)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.entities", hasSize(2)));
    }

    @Test
    public void multipleEntitiesRetrievedNotFound() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(
                                        IntegrationTestUtils.BASE_SERVICE_URL
                                                + "/"
                                                + requestPath)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("[ \"xyz\" , \"test\"]"))
                .andExpect(status().isNotFound());
    }
}
