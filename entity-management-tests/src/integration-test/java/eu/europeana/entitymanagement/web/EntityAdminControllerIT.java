package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;

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
        .perform(delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath
            + IntegrationTestUtils.BASE_ADMIN_URL).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertDeletionResponse(entityRecord);
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
        .perform(delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath
            + IntegrationTestUtils.BASE_ADMIN_URL).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertDeletionResponse(entityRecord);

  }

  private void assertDeletionResponse(EntityRecord entityRecord) throws SolrServiceException {
    // confirm that Solr document no longer exists
    Assertions.assertNull(solrService.searchById(SolrConcept.class, entityRecord.getEntityId()));

    // retrieval should throw exception
    Assertions.assertThrows(EntityNotFoundException.class, () -> entityRecordService
            .retrieveEntityRecord(entityRecord.getEntityId(), EntityProfile.internal.name(), true));
  }

}
