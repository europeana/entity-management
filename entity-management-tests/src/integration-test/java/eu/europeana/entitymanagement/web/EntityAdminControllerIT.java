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
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@SpringBootTest
@AutoConfigureMockMvc
class EntityAdminControllerIT extends BaseWebControllerTest {

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

    Optional<EntityRecord> dbRecordOptional = retrieveEntityEvenIfDisabled(entityRecord.getEntityId());
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

}
