package eu.europeana.entitymanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;

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

    // add 3rd entity
    europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_STALIN_JSON);
    metisResponse = loadFile(IntegrationTestUtils.AGENT_STALIN_XML);
    String entityId3 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_STALIN_URI)
            .getEntityId();

    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(entityId1, entityId2, entityId3))))
        .andExpect(status().isOk())
        // entities in response should match entityId ordering
        .andExpect(jsonPath("$.items.*.id", is(List.of(entityId1, entityId2, entityId3))));

    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        // change ordering of entityIds and retry request. Also include id that
                        // doesn't
                        // exist
                        List.of(
                            entityId3,
                            entityId1,
                            "http://data.europeana.eu/agent/200",
                            entityId2))))
        .andExpect(status().isOk())
        // ordering should match request entityIds, minus id that doesn't exist
        .andExpect(jsonPath("$.items.*.id", is(List.of(entityId3, entityId1, entityId2))));
  }

  @Test
  void multipleEntitiesRetrieveOldOrganizationIdsWithoutDuplicates() throws Exception {
    //register zoho GFM org with old id in sameAs
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);
    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    //please check that this id is also in the sameAs of the org json file (IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON)
    String oldId="http://data.europeana.eu/organization/486281000000940433";

    mockMvc
    .perform(
        post(IntegrationTestUtils.BASE_SERVICE_URL + "/retrieve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(List.of(oldId, entityId))))//here we ask for 2 orgs ids which should return the same org 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.items.*.id", is(List.of(entityId))));//only 1 org is returned (no duplicates)

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
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.*.id").isEmpty());
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
