package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
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
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    // confirm that Solr document is saved
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertNotNull(solrConcept);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(delete(BASE_SERVICE_URL + "/" + requestPath).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    assertedTaskScheduled(entityRecord.getEntityId(), DEPRECATION);
  }

  @Test
  void deprecatingNonExistingEntityShouldReturn404() throws Exception {
    mockMvc
        .perform(
            delete(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void deprecatingAlreadyDeprecatedEntityShouldReturn410() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(delete(BASE_SERVICE_URL + "/" + requestPath).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }
}
