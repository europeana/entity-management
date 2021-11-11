package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_ADMIN_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.BaseMvcTestUtils;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityAdminControllerIT extends BaseWebControllerTest {

  @Test
  void permanentDeletionShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);

    // confirm that Solr document is saved
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertNotNull(solrConcept);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), PERMANENT_DELETION);
  }

  @Test
  void permanentDeletionForDeprecatedEntityShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), PERMANENT_DELETION);
  }

  @Disabled
  @Test
  void migrationExistingEntityShouldBeSuccessful() throws Exception {
    String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";
    String entityId = EntityRecordUtils.buildEntityIdUri("concept", "1");
    ResultActions results =
        mockMvc
            .perform(
                post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "concept", "1")
                    .accept(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isAccepted());

    results
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));

    // check that record is present
    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityId);
    Assertions.assertFalse(dbRecordOptional.isEmpty());
  }

  @Disabled
  @Test
  void migrationExistingEntityInvalidEntityType() throws Exception {
    String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";
    mockMvc
        .perform(
            post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "testing", "1")
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isInternalServerError());
  }

  @Disabled
  @Test
  void migrationExistingEntityInvalidDataSource() throws Exception {
    String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.INVALID_MIGRATION_ID + "\"}";
    mockMvc
        .perform(
            post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "concept", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Disabled
  @Test
  void migrationExistingEntityAlreadyExist() throws Exception {
    String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";

    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            post(BASE_SERVICE_URL + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }
}
