package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.IntegrationTestUtils.*;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityReenableIT extends BaseWebControllerTest {

  @Test
  void reEnableDisabledEntityShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());
    mockMvc
        .perform(post(BASE_SERVICE_URL + "/" + requestPath).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // check that record was re-enabled
    mockMvc
        .perform(
            get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.getEntityType())));

    // confirm that Solr document now exists
    SolrConcept solrConcept = solrService.searchById(SolrConcept.class, entityRecord.getEntityId());
    Assertions.assertNotNull(solrConcept);
  }
}
