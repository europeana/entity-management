package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class EntityUpdateIT extends BaseWebControllerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void updatingNonExistingEntityShouldReturn404() throws Exception {
    /*
     * check the error if the entity does not exist prior to its update
     */
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + "concept/1")
                .content(loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void updatingDeprecatedEntityShouldReturn410() throws Exception {
    EntityRecord entityRecord = createConcept();
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }

  @Test
  void updatingNonExistingEntityFromExternalSourceShouldReturn404() throws Exception {
    /*
     * check the error if the entity does not exist
     */
    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL
                    + "/"
                    + "wrong-type/wrong-identifier/management/update")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void updatingDeprecatedEntityFromExternalSourceShouldReturn410() throws Exception {
    EntityRecord entityRecord = createConcept();
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            post(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + "/management/update")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }

  @Test
  void updateFromExternalDatasourceShouldBeSuccessful() throws Exception {
    EntityRecord entityRecord = createConcept();

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());
  }

  @Test
  void updateTimespanShouldBeSuccessful() throws Exception {
    EntityRecord entityRecord = createTimeSpan();

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(IntegrationTestUtils.TIMESPAN_UPDATE_1ST_CENTURY_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(entityRecordUpdated.isPresent());
    TimeSpan timespan = (TimeSpan) (entityRecordUpdated.get().getEntity());
    Assertions.assertNotNull(timespan.getPrefLabel());
    Assertions.assertFalse(timespan.getPrefLabel().isEmpty());
    Assertions.assertNotNull(timespan.getAltLabel());
    Assertions.assertNotNull(timespan.getBeginString());
    Assertions.assertNotNull(timespan.getEndString());
    Assertions.assertFalse(timespan.getAltLabel().isEmpty());
  }

  @Test
  void updateConceptShouldBeSuccessful() throws Exception {
    EntityRecord entityRecord = createConcept();

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .content(loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

    final ObjectNode nodeReference =
        mapper.readValue(
            loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON), ObjectNode.class);
    Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(entityRecordUpdated.isPresent());
    Assertions.assertEquals(
        nodeReference.path("depiction").path("id").asText(),
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction().getId());
    Assertions.assertEquals(
        nodeReference.path("note").path("en").path(0).asText(),
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getNote().get("en").get(0));
    // acquire the reader for the right type
    ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, String>>() {});
    Map<String, String> prefLabelToCheck = reader.readValue(nodeReference.path("prefLabel"));
    Map<String, String> prefLabelUpdated =
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getPrefLabel();
    for (Map.Entry<String, String> prefLabelEntry : prefLabelToCheck.entrySet()) {
      Assertions.assertTrue(prefLabelUpdated.containsKey(prefLabelEntry.getKey()));
      Assertions.assertTrue(prefLabelUpdated.containsValue(prefLabelEntry.getValue()));
    }
  }

  @Test
  void updatePUTShouldReplaceEuropeanaProxy() throws Exception {
    EntityRecord savedRecord = createConcept();

    // assert content of Europeana proxy
    Entity europeanaProxyEntity = savedRecord.getEuropeanaProxy().getEntity();

    // values match labels in json file
    Assertions.assertNotNull(europeanaProxyEntity.getPrefLabel().get("en"));
    Assertions.assertNotNull(europeanaProxyEntity.getAltLabel().get("en").get(0));
    Assertions.assertNotNull(europeanaProxyEntity.getAltLabel().get("en").get(1));
    Assertions.assertNotNull(europeanaProxyEntity.getDepiction());

    String requestPath = getEntityRequestPath(savedRecord.getEntityId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_EMPTY_UPDATE_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    // check that update removed fields from Europeana proxy in original request
    Optional<EntityRecord> updatedRecord = retrieveEntity(savedRecord.getEntityId());
    Assertions.assertTrue(updatedRecord.isPresent());
    europeanaProxyEntity = updatedRecord.get().getEuropeanaProxy().getEntity();

    Assertions.assertNull(europeanaProxyEntity.getPrefLabel());
    Assertions.assertNull(europeanaProxyEntity.getAltLabel());
    Assertions.assertNull(europeanaProxyEntity.getNote());
    Assertions.assertNull(europeanaProxyEntity.getDepiction());
  }

  @Test
  void updateWithInvalidEntityShouldReturn400() throws Exception {
    EntityRecord savedRecord = createConcept();

    // check that consolidation is successful during registration
    Assertions.assertNotNull(savedRecord.getEntity().getDepiction());

    String requestPath = getEntityRequestPath(savedRecord.getEntityId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON_INVALID))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void schedulingFullUpdateViaSearchShouldBeSuccessful() throws Exception {
    EntityRecord concept = createConcept();
    createTimeSpan();

    // solr query to fetch all concepts
    String solrSearchQuery = "id:http\\:\\/\\/data.europeana.eu\\/concept\\/*";

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    IntegrationTestUtils.BASE_SERVICE_URL + "/management/update")
                .param(WebEntityConstants.QUERY_PARAM_QUERY, solrSearchQuery))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.expected", is(1)))
        .andExpect(jsonPath("$.successful", hasSize(1)))
        .andExpect(jsonPath("$.successful", contains(concept.getEntityId())));
  }

  @Test
  void schedulingMetricsUpdateViaSearchShouldBeSuccessful() throws Exception {
    createConcept();
    EntityRecord timeSpan = createTimeSpan();

    // solr query to fetch all timespans
    String solrSearchQuery = "id:http\\:\\/\\/data.europeana.eu\\/timespan\\/*";

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    IntegrationTestUtils.BASE_SERVICE_URL + "/management/metrics")
                .param(WebEntityConstants.QUERY_PARAM_QUERY, solrSearchQuery))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.expected", is(1)))
        .andExpect(jsonPath("$.successful", hasSize(1)))
        .andExpect(jsonPath("$.successful", contains(timeSpan.getEntityId())));
  }

  private EntityRecord createConcept() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    return createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
  }

  private EntityRecord createTimeSpan() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.TIMESPAN_1ST_CENTURY_XML);

    return createEntity(
        europeanaMetadata, metisResponse, IntegrationTestUtils.TIMESPAN_1ST_CENTURY_URI);
  }
}
