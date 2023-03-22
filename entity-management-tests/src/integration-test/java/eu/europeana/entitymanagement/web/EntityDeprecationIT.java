package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.PARAM_PROFILE_SYNC;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_PROFILE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

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

    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(dbRecordOptional.get().isDisabled());

    // confirm that Solr document no longer exists
    Assertions.assertNull(solrService.searchById(SolrConcept.class, entityRecord.getEntityId()));
  }
  
  @Test
  void deprecationConceptSchemeShouldBeSuccessful() throws Exception {
    /*
     * 1. register concept scheme with items
     */
    List<String> items = new ArrayList<>();
    items.add(WebEntityFields.BASE_DATA_EUROPEANA_URI + "concept/1");
    ConceptScheme scheme = createConceptScheme(items);
    
    /*
     * 2. register concept that refers to the given concept scheme
     */
    JSONObject conceptJson = new JSONObject(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON));
    JSONArray inScheme = new JSONArray();
    inScheme.put(scheme.getEntityId());
    conceptJson.put(WebEntityFields.IN_SCHEME, inScheme);
    
    String europeanaMetadata = conceptJson.toString();
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);

    /*
     * 3. confirm concept contains the inScheme field 
     */
    Assertions.assertTrue(entityRecord.getEntity().getInScheme().contains(scheme.getEntityId()));
    //confirm also that Solr document contains the inScheme field
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertTrue(solrConcept.getInScheme().contains(scheme.getEntityId()));

    /*
     * 4. deprecate concept scheme
     */
    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SCHEME_URL + "/" + scheme.getEntityId().substring(scheme.getEntityId().lastIndexOf('/') + 1))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    
    /*
     * 5. confirm that Solr concept scheme no longer exists and the db obj is disabled
     */
    Assertions.assertTrue(entityRecordRepository.findConceptScheme(scheme.getEntityId()).isDisabled());
    Assertions.assertNull(solrService.searchConceptScheme(scheme.getEntityId()));

    /*
     * 6. confirm the concept inScheme field does not contain the disabled scheme id
     */
    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertNull(dbRecordOptional.get().getEntity().getInScheme());  
    /*
     * 7. after repeated delete, the concept scheme should be permanently deleted
     */
    mockMvc
    .perform(
        delete(IntegrationTestUtils.BASE_SCHEME_URL + "/" + scheme.getEntityId().substring(scheme.getEntityId().lastIndexOf('/') + 1))
            .accept(MediaType.APPLICATION_JSON))
    .andExpect(status().isNoContent());
    Assertions.assertNull(entityRecordRepository.findConceptScheme(scheme.getEntityId()));
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
