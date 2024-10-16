package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.PARAM_PROFILE_SYNC;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_PROFILE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityDeprecationIT extends BaseWebControllerTest {

  @Test
  void deprecationShouldBeSuccessful() throws Exception {
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
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), DEPRECATION);
  }

  @Test
  void deprecationWithSyncProfileShouldBeSuccessful() throws Exception {
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
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .param(QUERY_PARAM_PROFILE, PARAM_PROFILE_SYNC)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    Optional<EntityRecord> dbRecordOptional = retrieveEntityEvenIfDisabled(entityRecord.getEntityId());
    Assertions.assertTrue(dbRecordOptional.get().isDisabled());

    // confirm that Solr document no longer exists
    Assertions.assertNull(solrService.searchById(SolrConcept.class, entityRecord.getEntityId()));
  }

  @Test
  void deprecatingNonExistingEntityShouldReturn404() throws Exception {
    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void deprecatingAlreadyDeprecatedEntityShouldReturn410() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }
}
