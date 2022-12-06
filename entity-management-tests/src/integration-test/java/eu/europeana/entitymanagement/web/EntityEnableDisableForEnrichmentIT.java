package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class EntityEnableDisableForEnrichmentIT extends BaseWebControllerTest {

  // private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void disableForEnrichmentConceptShouldBeSuccessful() throws Exception {
    EntityRecord entityRecord = createConcept();

    // verify that the enrichment fields are present in SOLR
    // label_enrich
    SolrQuery query =
        buildSolrQueryWithIdAndFieldExists(entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL);
    List<SolrEntity<Entity>> res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());
    // label_enrich.en
    query =
        buildSolrQueryWithIdAndFieldExists(
            entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL + ".en");
    res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());
    // call disable for enrichment
    ResultActions result =
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + "/management/enrich")
                .param("action", "disable"));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
        .andExpect(jsonPath("$.isAggregatedBy.enrich", is(false)));

    // verify that the enrichment fields are empty in SOLR
    // label_enrich
    query = buildSolrQueryWithIdAndFieldExists(entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL);
    res = emSolrService.searchByQuery(query);
    assertNull(res);
    // label_enrich.en
    query =
        buildSolrQueryWithIdAndFieldExists(
            entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL + ".en");
    res = emSolrService.searchByQuery(query);
    assertNull(res);
  }

  @Test
  void enableForEnrichmentConceptShouldBeSuccessful() throws Exception {
    EntityRecord entityRecord = createConcept();

    // verify that the enrichment fields are present in SOLR
    // label_enrich
    SolrQuery query =
        buildSolrQueryWithIdAndFieldExists(entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL);
    List<SolrEntity<Entity>> res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());
    // label_enrich.en
    query =
        buildSolrQueryWithIdAndFieldExists(
            entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL + ".en");
    res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());
    // call disable for enrichment
    ResultActions result =
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + "/management/enrich")
                .param("action", "enable"));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
        .andExpect(jsonPath("$.isAggregatedBy.enrich", is(true)));

    // verify that the enrichment fields are empty in SOLR
    // label_enrich
    query = buildSolrQueryWithIdAndFieldExists(entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL);
    res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());

    // label_enrich.en
    query =
        buildSolrQueryWithIdAndFieldExists(
            entityRecord, EntitySolrFields.LABEL_ENRICH_GENERAL + ".en");
    res = emSolrService.searchByQuery(query);
    assertNotNull(res);
    assertEquals(1, res.size());
  }

  SolrQuery buildSolrQueryWithIdAndFieldExists(EntityRecord entityRecord, String field) {
    StringBuilder queryBuilder =
        new StringBuilder("id:\"")
            .append(entityRecord.getEntityId())
            .append("\" AND ")
            .append(field)
            .append(":*");
    SolrQuery query = new SolrQuery(queryBuilder.toString());
    return query;
  }
}
