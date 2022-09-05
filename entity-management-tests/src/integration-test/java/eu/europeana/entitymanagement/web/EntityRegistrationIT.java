package eu.europeana.entitymanagement.web;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityRegistrationIT extends BaseWebControllerTest {

  @Autowired SolrService solrService;

  @Test
  public void registerConceptShouldBeSuccessful() throws Exception {

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  void registerAgentShouldBeSuccessful() throws Exception {

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // JSON includes isShownBy and depiction
        .andExpect(jsonPath("$.isShownBy").isNotEmpty())
        .andExpect(jsonPath("$.depiction").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  void registerAgentWithRedirectionShouldBeSuccessful() throws Exception {

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.AGENT_REGISTER_BIRCH_REDIRECTION_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)))
        .andExpect(
            jsonPath(
                "$.sameAs",
                Matchers.hasItems(
                    IntegrationTestUtils.AGENT_BIRCH_URI,
                    IntegrationTestUtils.AGENT_BIRCH_UPDATED_URI)))
        .andExpect(jsonPath("$.proxies[1].id", is(IntegrationTestUtils.AGENT_BIRCH_UPDATED_URI)));
  }

  @Test
  void registerAgentStalinShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.AGENT_REGISTER_STALIN_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // fields to be serialized as string
        .andExpect(jsonPath("$.dateOfBirth", any(String.class)))
        .andExpect(jsonPath("$.dateOfDeath", any(String.class)))
        .andExpect(jsonPath("$.dateOfEstablishment", any(String.class)))
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  void registerAgentSchegkWithURNReferenceShouldBeSuccessful() throws Exception {
    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.AGENT_REGISTER_SCHEGK_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // fields to be serialized as string
        .andExpect(jsonPath("$.dateOfBirth", any(String.class)))
        .andExpect(jsonPath("$.dateOfDeath", any(String.class)))
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)))
        //
        .andExpect(
            jsonPath(
                "$.sameAs",
                containsInRelativeOrder("urn:uuid:387a0a33-bc8e-4bfd-8bbc-439691b63546")));
  }

  @Test
  void registerPlaceShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.PLACE_REGISTER_PARIS_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  void registerPlaceWithRedirectionShouldBeSuccessful() throws Exception {

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.PLACE_REGISTER_HAGENBACH_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)))
        .andExpect(
            jsonPath(
                "$.sameAs",
                Matchers.containsInRelativeOrder(
                    IntegrationTestUtils.PLACE_HAGENBACH_URI,
                    IntegrationTestUtils.PLACE_HAGENBACH_UPDATED_URI)))
        .andExpect(
            jsonPath("$.proxies[1].id", is(IntegrationTestUtils.PLACE_HAGENBACH_UPDATED_URI)));
  }

  @Test
  void registerTimespanShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.TimeSpan.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        .andExpect(jsonPath("$.isShownBy.id").isNotEmpty())
        .andExpect(jsonPath("$.isShownBy.source").isNotEmpty())
        .andExpect(jsonPath("$.isShownBy.type").isNotEmpty())
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  public void registerZohoOrganizationShouldBeSuccessful() throws Exception {

    String expectedId =
        "http://data.europeana.eu/organization/"
            + EntityRecordUtils.getIdFromUrl(IntegrationTestUtils.ORGANIZATION_NATURALIS_URI_ZOHO);

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_NATURALIS_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(expectedId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // isAggregatedBy should contain 3 aggregates (for Europeana, zoho and wikidata proxies)
        .andExpect(
            jsonPath(
                "$.isAggregatedBy.aggregates",
                containsInAnyOrder(
                    EntityRecordUtils.getEuropeanaAggregationId(expectedId),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 1),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 2))))
        // sameAs contains Wikidata and Zoho uris
        .andExpect(
            jsonPath(
                "$.sameAs",
                Matchers.hasItems(
                    IntegrationTestUtils.ORGANIZATION_NATURALIS_URI_WIKIDATA_URI,
                    IntegrationTestUtils.ORGANIZATION_NATURALIS_URI_ZOHO)))
        // should have Europeana, Zoho and Wikidata proxies
        .andExpect(jsonPath("$.proxies", hasSize(3)));
  }

  @Test
  public void registerZohoOrganizationGFMShouldBeSuccessful() throws Exception {

    String expectedId =
        EntityRecordUtils.buildEntityIdUri(
            "organization",
            EntityRecordUtils.getIdFromUrl(IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO));

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(expectedId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // isAggregatedBy should contain 3 aggregates (for Europeana, zoho and wikidata proxies)
        .andExpect(
            jsonPath(
                "$.isAggregatedBy.aggregates",
                containsInAnyOrder(
                    EntityRecordUtils.getEuropeanaAggregationId(expectedId),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 1),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 2))))
        // sameAs contains Wikidata and Zoho uris
        .andExpect(
            jsonPath(
                "$.sameAs",
                Matchers.hasItems(
                    IntegrationTestUtils.ORGANIZATION_GFM_OLD_URI_WIKIDATA_URI,
                    IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO,
                    IntegrationTestUtils.ORGANIZATION_GFM_URI_WIKIDATA_URI)))
        .andExpect(jsonPath("$.prefLabel[*]", hasSize(2)))
        // should have Europeana, Zoho and Wikidata proxies
        .andExpect(jsonPath("$.proxies", hasSize(3)));
  }

  @Test
  public void registerZohoOrganizationBergerShouldBeSuccessful() throws Exception {

    String expectedId =
        EntityRecordUtils.buildEntityIdUri(
            "organization",
            EntityRecordUtils.getIdFromUrl(
                IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_URI_ZOHO));

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(
                    loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_BERGER_MUSEUM_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(expectedId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // isAggregatedBy should contain 3 aggregates (for Europeana, zoho and wikidata proxies)
        .andExpect(
            jsonPath(
                "$.isAggregatedBy.aggregates",
                containsInAnyOrder(
                    EntityRecordUtils.getEuropeanaAggregationId(expectedId),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 1),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 2))))
        // sameAs contains Wikidata and Zoho uris
        .andExpect(
            jsonPath(
                "$.sameAs",
                Matchers.hasItems(
                    IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI,
                    IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_URI_ZOHO)))
        .andExpect(jsonPath("$.prefLabel[*]", hasSize(1)))
        // should have Europeana, Zoho and Wikidata proxies
        .andExpect(jsonPath("$.proxies", hasSize(3)));

    // check if indexing is successfull by searching the organization in solr
    SolrOrganization org = solrService.searchById(SolrOrganization.class, expectedId);
    assertNotNull(org.getHasGeo());
    assertFalse(org.getHasGeo().startsWith("geo:"));
  }

  @Test
  void registerZohoOrganizationWithoutWikidataSameAsShouldBeSuccessful() throws Exception {
    String expectedId =
        EntityRecordUtils.buildEntityIdUri(
            "organization",
            EntityRecordUtils.getIdFromUrl(IntegrationTestUtils.ORGANIZATION_PCCE_URI_ZOHO));

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_PCCE_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(expectedId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        // isAggregatedBy should contain 2 aggregates (for Europeana and zoho proxies). No wikidata
        // sameAs for this org
        .andExpect(
            jsonPath(
                "$.isAggregatedBy.aggregates",
                containsInAnyOrder(
                    EntityRecordUtils.getEuropeanaAggregationId(expectedId),
                    EntityRecordUtils.getDatasourceAggregationId(expectedId, 1))))
        // sameAs contains Zoho uri
        .andExpect(
            jsonPath(
                "$.sameAs", Matchers.contains(IntegrationTestUtils.ORGANIZATION_PCCE_URI_ZOHO)))
        // should have Europeana and Zoho proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  void registerZohoOrganizationBnfWithNewFieldsShouldBeSuccessful() throws Exception {
    String entityId = EntityRecordUtils.buildEntityIdUri(
        "organization",
        EntityRecordUtils.getIdFromUrl(IntegrationTestUtils.ORGANIZATION_BNF_URI_ZOHO));

    ResultActions response =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_BNF_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    response
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.hasAddress.hasGeo").isNotEmpty())
        .andExpect(jsonPath("$.language", everyItem(matchesRegex("[a-z]+"))))
        .andExpect(jsonPath("$.hiddenLabel", hasSize(3)))
        .andExpect(jsonPath("$.organizationDomain[*]", hasSize(1)))
        .andExpect(jsonPath("$.id", is(entityId)));
    
  }

  @Test
  public void registrationForExistingCoreferenceShouldReturn301() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();
    String redirectUrl =
        String.format("/entity/%s", EntityRecordUtils.extractIdentifierFromEntityId(entityId));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string(HttpHeaders.LOCATION, is(redirectUrl)));
  }

  @Test
  public void registrationForDeprecatedCoreferenceShouldReturn410() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isGone());
  }

  @Test
  public void registrationWithUnknownDatasourceShouldReturn400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_INVALID_SOURCE))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registrationWithEmptyMetisResponseShouldReturn400() throws Exception {
    // mockMetis returns an empty response body for this entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_UNKNOWN_ENTITY);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registrationWithInvalidEntityShouldReturn400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON_INVALID))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registrationWithStaticDataSourceShouldReturn422() throws Exception {
    ResultActions result =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.STATIC_ENTITY_NOT_MINTED_FILE))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("profile", "debug"));
    result.andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.trace").isNotEmpty());
  }
}
