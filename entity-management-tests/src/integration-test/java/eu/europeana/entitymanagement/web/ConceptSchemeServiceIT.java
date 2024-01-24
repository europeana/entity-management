package eu.europeana.entitymanagement.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@SpringBootTest
@AutoConfigureMockMvc
public class ConceptSchemeServiceIT extends BaseWebControllerTest {

  protected ConceptScheme createConceptScheme(List<String> items) throws Exception {
    ConceptScheme scheme =
        objectMapper.readValue(
            loadFile(IntegrationTestUtils.CONCEPT_SCHEME_PHOTO_GENRE_JSON), ConceptScheme.class);
    scheme.setItems(items);
    return emConceptSchemeService.createConceptScheme(scheme);
  }

  @Test
  void registerConceptSchemeShouldBeSuccessful() throws Exception {
    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SCHEME_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_SCHEME_PHOTO_GENRE_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", containsString(emConfig.getSchemeDataEndpoint())))
        .andExpect(jsonPath("$.type", is(EntityTypes.ConceptScheme.getEntityType())))
        .andExpect(jsonPath("$.prefLabel").isNotEmpty());
  }

  @Test
  public void retrieveConceptSchemeShouldBeSuccessful() throws Exception {
    ConceptScheme scheme = createConceptScheme(null);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SCHEME_URL + scheme.getIdentifier() + ".jsonld")
                //                .param(WebEntityConstants.QUERY_PARAM_WSKEY, "testapikey")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(scheme.getConceptSchemeId())))
        .andExpect(jsonPath("$.type", is(EntityTypes.ConceptScheme.getEntityType())));

    // check solr obj also exists
//    SolrConceptScheme solrConceptScheme = solrService.searchConceptSchemeById(scheme.getConceptSchemeId());
//    Assertions.assertNotNull(solrConceptScheme);
  }

  // TODO: add tests for XML retrieval

  @Test
  void retrieveNonExistingConceptScheme() throws Exception {
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SCHEME_URL + "wrong-identifier.jsonld")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void retrieveDeprecatedConceptScheme() throws Exception {
    ConceptScheme scheme = createConceptScheme(null);
    emConceptSchemeService.disableConceptScheme(scheme, true);

    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SCHEME_URL + scheme.getIdentifier())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
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
    JSONObject conceptJson =
        new JSONObject(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON));
    JSONArray inScheme = new JSONArray();
    inScheme.put(scheme.getConceptSchemeId());
    conceptJson.put(WebEntityFields.IN_SCHEME, inScheme);

    String europeanaMetadata = conceptJson.toString();
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);

    /*
     * 3. confirm concept contains the inScheme field
     */
    Assertions.assertTrue(
        entityRecord.getEntity().getInScheme().contains(scheme.getConceptSchemeId()));
    // confirm also that Solr document contains the inScheme field
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertTrue(solrConcept.getInScheme().contains(scheme.getConceptSchemeId()));

    /*
     * 4. deprecate concept scheme
     */
    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SCHEME_URL + "/" + scheme.getIdentifier())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    /*
     * 5. confirm that Solr concept scheme no longer exists and the db obj is disabled
     */
    Assertions.assertTrue(
        emConceptSchemeService.retrieveConceptScheme(scheme.getIdentifier(), true).isDisabled());
    Assertions.assertNull(solrService.searchConceptSchemeById(scheme.getConceptSchemeId()));

    /*
     * 6. confirm the concept inScheme field does not contain the disabled scheme id
     */
    /* currently not supported to be implemented later
    Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertNull(dbRecordOptional.get().getEntity().getInScheme());
    */
    /*
     * 7. after repeated delete (delete disabled), 410 error code should be returned
     */
    mockMvc
        .perform(
            delete(
                    IntegrationTestUtils.BASE_SCHEME_URL
                        + scheme.getConceptSchemeId().substring(scheme.getConceptSchemeId().lastIndexOf('/') + 1))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }

  @Test
  void updatingNonExistingConceptSchemeShouldReturn404() throws Exception {
    mockMvc
        .perform(
            put(IntegrationTestUtils.BASE_SCHEME_URL + "1")
            .content(loadFile(IntegrationTestUtils.CONCEPT_SCHEME_UPDATE_PHOTO_GENRE_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void updatingDeprecatedConceptSchemeReturn410() throws Exception {
    ConceptScheme scheme = createConceptScheme(null);
    emConceptSchemeService.disableConceptScheme(scheme, true);

    mockMvc
      .perform(
          put(IntegrationTestUtils.BASE_SCHEME_URL + scheme.getIdentifier())
          .content(loadFile(IntegrationTestUtils.CONCEPT_SCHEME_UPDATE_PHOTO_GENRE_JSON))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isGone());
  }


  @Test
  void updateConceptSchemeShouldBeSuccessful() throws Exception {
    List<String> items = new ArrayList<>();
    items.add(WebEntityFields.BASE_DATA_EUROPEANA_URI + "concept/1");
    ConceptScheme scheme = createConceptScheme(items);

    mockMvc
      .perform(
          put(IntegrationTestUtils.BASE_SCHEME_URL + scheme.getIdentifier())
          .content(loadFile(IntegrationTestUtils.CONCEPT_SCHEME_UPDATE_PHOTO_GENRE_JSON))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk());

    ConceptScheme updatedSchemeMongo = emConceptSchemeService.retrieveConceptScheme(scheme.getIdentifier(), false);
//    SolrConceptScheme updatedSchemeSolr = solrService.searchConceptSchemeById(scheme.getConceptSchemeId());
    
    Assertions.assertNotNull(updatedSchemeMongo);
//    Assertions.assertNotNull(updatedSchemeSolr);
    Assertions.assertTrue(updatedSchemeMongo.getPrefLabel().keySet().size()==2);
//    Assertions.assertTrue(updatedSchemeSolr.getPrefLabel().keySet().size()==2);
    Assertions.assertTrue(updatedSchemeMongo.getDefinition().keySet().size()==2);
//    Assertions.assertTrue(updatedSchemeSolr.getDefinition().keySet().size()==2);
    Assertions.assertTrue(updatedSchemeMongo.getTotal()==1);
    Assertions.assertTrue(updatedSchemeMongo.getModified().getTime()!=scheme.getModified().getTime());
//    Assertions.assertTrue(updatedSchemeSolr.getModified().getTime()!=scheme.getModified().getTime());
  }
  
  /*
   * TODO: create a test for the validation of the scheme fields, when the validation gets enabled (currently that code is commented out)
   */

}
