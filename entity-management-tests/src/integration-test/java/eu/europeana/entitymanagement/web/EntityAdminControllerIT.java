package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.PARAM_PROFILE_SYNC;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_PROFILE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class EntityAdminControllerIT extends BaseWebControllerTest {

  public static final String STATIC_ENTITY_EXTERNAL_ID =
      "http://bib.arts.kuleuven.be/photoVocabulary/-photoVocabulary-11008";
  public static final String STATIC_ENTITY_IDENTIFIER = "1689";
  public static final String STATIC_ENTITY_FILE = "/content/static_concept_1689.json";

  @Test
  void permanentDeletionShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);

    // confirm that Solr document is saved
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertNotNull(solrConcept);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + IntegrationTestUtils.BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), PERMANENT_DELETION);
  }

  @Test
  void permanentDeletionWithSyncProfileShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);

    // confirm that Solr document is saved
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertNotNull(solrConcept);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + IntegrationTestUtils.BASE_ADMIN_URL)
                .param(QUERY_PARAM_PROFILE, PARAM_PROFILE_SYNC)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(dbRecordOptional.isEmpty());

    // confirm that Solr document no longer exists
    Assertions.assertNull(solrService.searchById(SolrConcept.class, entityRecord.getEntityId()));
  }

  @Test
  void permanentDeletionForDeprecatedEntityShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + IntegrationTestUtils.BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), PERMANENT_DELETION);
  }

  @Test
  void migrationShouldBeSuccessful() throws Exception {
    String entityId = "http://data.europeana.eu/concept/1";
    String requestBody =
        "{\"type\" : \"Concept\", \"id\" : \"" + IntegrationTestUtils.VALID_MIGRATION_ID + "\"}";
    ResultActions results =
        mockMvc
            .perform(
                post(
                        IntegrationTestUtils.BASE_SERVICE_URL
                            + "/{type}/{identifier}"
                            + IntegrationTestUtils.BASE_ADMIN_URL,
                        "concept",
                        "1")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(requestBody))
            .andExpect(status().isAccepted());

    results
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));

    // check that record is present
    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityId);
    Assertions.assertFalse(dbRecordOptional.isEmpty());
  }

  @Test
  /**
   * @deprecated the entity migration functionality is deprecated the test case will loose purpose 
   * @throws Exception
   */
  @Deprecated
  void migrationAndUpdateWithStaticDataSourceShouldBeSuccessful() throws Exception {
    String entityId = "http://data.europeana.eu/concept/" + STATIC_ENTITY_IDENTIFIER;
    migrateEntity("Concept", entityId, STATIC_ENTITY_EXTERNAL_ID);
    ResultActions result =
        mockMvc.perform(
            MockMvcRequestBuilders.put(
                    IntegrationTestUtils.BASE_SERVICE_URL + "/{type}/{identifier}",
                    "concept",
                    STATIC_ENTITY_IDENTIFIER)
                .content(loadFile(STATIC_ENTITY_FILE))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
    result
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
        .andExpect(jsonPath("$.prefLabel[*]", hasSize(11))) // 4 labels removed through cleaning
        .andExpect(jsonPath("$.altLabel[*]", hasSize(1)));
  }

  @Test
  void updateForStaticDataSourceShouldBeSuccessful() throws Exception {
    String entityId = "http://data.europeana.eu/concept/" + STATIC_ENTITY_IDENTIFIER;
    migrateEntity("Concept", entityId, STATIC_ENTITY_EXTERNAL_ID);

    // check that record is present
    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityId);
    Assertions.assertFalse(dbRecordOptional.isEmpty());
  }

  @Test
  void migratingInvalidEntityTypeShouldReturn400() throws Exception {
    String requestBody =
        "{\"type\" : \"InvalidEntityType\", \"id\" : \""
            + IntegrationTestUtils.VALID_MIGRATION_ID
            + "\"}";
    mockMvc
        .perform(
            post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/{type}/{identifier}"
                        + IntegrationTestUtils.BASE_ADMIN_URL,
                    "InvalidEntityType",
                    "1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void migratingInvalidDatasourceShouldReturn400() throws Exception {
    String requestBody =
        "{\"type\" : \"Concept\", \"id\" : \"" + IntegrationTestUtils.INVALID_MIGRATION_ID + "\"}";
    mockMvc
        .perform(
            post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/{type}/{identifier}"
                        + IntegrationTestUtils.BASE_ADMIN_URL,
                    "concept",
                    "1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void migratingExistingEntityShouldReturn400() throws Exception {
    String entityId = "http://data.europeana.eu/concept/1";
    migrateEntity("Concept", entityId, IntegrationTestUtils.VALID_MIGRATION_ID);

    String requestBody =
        "{\"type\" : \"Concept\", \"id\" : \"" + IntegrationTestUtils.VALID_MIGRATION_ID + "\"}";

    mockMvc
        .perform(
            post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/{type}/{identifier}"
                        + IntegrationTestUtils.BASE_ADMIN_URL,
                    "concept",
                    "1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  private void migrateEntity(String entityType, String entityId, String externalId)
      throws Exception {
    String requestBody = "{\"type\" : \"" + entityType + "\", \"id\" : \"" + externalId + "\"}";

    mockMvc
        .perform(
            post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/{type}/{identifier}"
                        + IntegrationTestUtils.BASE_ADMIN_URL,
                    entityType.toLowerCase(),
                    EntityRecordUtils.getIdFromUrl(entityId))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(entityType)));
  }
}
