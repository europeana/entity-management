package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityRegistrationIT extends BaseWebControllerTest {

  @Test
  public void registerConceptShouldBeSuccessful() throws Exception {

    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_BATHTUB_JSON))
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
            post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_DAVINCI_JSON))
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
  void registerAgentStalinShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_STALIN_JSON))
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
  void registerPlaceShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_PARIS_JSON))
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
  void registerTimespanShouldBeSuccessful() throws Exception {
    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_1ST_CENTURY_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.TimeSpan.name())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  public void registerZohoOrganizationShouldBeSuccessful() throws Exception {

    String expectedId =
        "http://data.europeana.eu/organization/"
            + EntityRecordUtils.getIdFromUrl(ORGANIZATION_NATURALIS_URI_ZOHO);

    ResultActions response = mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_NATURALIS_ZOHO_JSON))
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
                containsInAnyOrder(ORGANIZATION_NATURALIS_URI_ZOHO, ORGANIZATION_NATURALIS_URI_WIKIDATA_URI)))
        // should have Europeana, Zoho and Wikidata proxies
        .andExpect(jsonPath("$.proxies", hasSize(3)));
  }

  @Test
  public void registerZohoOrganizationGFMShouldBeSuccessful() throws Exception {

    String expectedId =
        "http://data.europeana.eu/organization/"
            + EntityRecordUtils.getIdFromUrl(ORGANIZATION_GFM_URI_ZOHO);

    ResultActions response = mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_GFM_ZOHO_JSON))
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
                containsInAnyOrder(ORGANIZATION_GFM_URI_ZOHO, ORGANIZATION_GFM_URI_WIKIDATA_URI)))
        // should have Europeana, Zoho and Wikidata proxies
        .andExpect(jsonPath("$.proxies", hasSize(3)));
    //TODO: add pref label verification for wikidata
  }
  
  
  @Test
  public void registrationForExistingCoreferenceShouldReturn301() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();
    String redirectUrl =
        String.format("/entity/%s", EntityRecordUtils.extractIdentifierFromEntityId(entityId));

    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string(HttpHeaders.LOCATION, is(redirectUrl)));
  }

  @Test
  public void registrationForDeprecatedCoreferenceShouldReturn410() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isGone());
  }

  @Test
  public void registrationWithUnknownDatasourceShouldReturn400() throws Exception {
    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_INVALID_SOURCE))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registrationWithEmptyMetisResponseShouldReturn400() throws Exception {
    // mockMetis returns an empty response body for this entity
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_UNKNOWN_ENTITY);
    mockMvc
        .perform(
            post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }
}
