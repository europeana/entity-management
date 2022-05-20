package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ENTITY_CONTEXT;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class EntityMultipleRetrievalIT extends BaseWebControllerTest {

  @Test
  void multipleEntitiesRetrievedSuccessfully() throws Exception {
    // create 1st entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    String entityId1 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    // add second entity
    europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
    metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);
    String entityId2 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI)
            .getEntityId();

    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(entityId1, entityId2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.@context", is(ENTITY_CONTEXT)))
        .andExpect(jsonPath("$.total", is(2)))
        .andExpect(jsonPath("$.items", hasSize(2)));
  }

  @Test
  void multipleEntitiesRetrievedNotFound() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                // db is cleared between test runs, so these shouldn't exist
                .content(
                    "[ \"http://data.europeana.eu/concept/1\" , \"http://data.europeana.eu/concept/2\"]"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnEntitiesWhenSomeInvalidEntitiesInRequest() throws Exception {
    // create 1st entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    String entityId1 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    // one invalid and valid entityId each
    String requestBody =
        objectMapper.writeValueAsString(List.of("http://data.europeana.eu/concept/0", entityId1));

    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)));
  }
}
