package eu.europeana.entitymanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import java.util.List;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityMultipleRetrievalIT extends BaseWebControllerTest {

  private static final String requestPath = "retrieve";

  @Test
  public void multipleEntitiesRetrievedSuccessfully() throws Exception {
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
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON)
                .content(String.valueOf(List.of(entityId1, entityId2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entities", hasSize(2)));
  }

  @Test
  public void multipleEntitiesRetrievedNotFound() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON)
                // db is cleared between test runs, so these shouldn't exist
                .content(
                    "[ \"http://data.europeana.eu/concept/1\" , \"http://data.europeana.eu/concept/2\"]"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturnEntitiesWhenSomeInvalidEntitiesInRequest() throws Exception {
    // create 1st entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    String entityId1 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    // one invalid and valid entityId each
    List<String> requestBody = List.of("http://data.europeana.eu/concept/0", entityId1);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON)
                .content(String.valueOf(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entities", hasSize(1)));
  }
}
