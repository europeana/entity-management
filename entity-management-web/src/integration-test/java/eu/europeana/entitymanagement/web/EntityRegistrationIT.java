package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class EntityRegistrationIT extends BaseWebControllerTest {

    @Test
    public void registerConceptShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        results.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));


        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }

    @Test
    void registerAgentShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_DAVINCI_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }


    @Test
    void registerAgentStalinShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_STALIN_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());


        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }

    //@Test
    @Deprecated
    /**
     * @deprecated zoho is the primary source for organizations see registerZohoOrganizationShouldBeSuccessful
     * @throws Exception
     */
    public void registerOrganizationShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_BNF_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }

    //@Test
    /* Doesn't work without correct zoho import configuration
     * TODO: mock zoho response */
    public void registerZohoOrganizationShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_BNF_ZOHO_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }
    
    
    @Test
    void registerPlaceShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_PARIS_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }

    @Test
    void registerTimespanShouldBeSuccessful() throws Exception {
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_1ST_CENTURY_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))

                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkAllowHeaderForPOST(results);
        checkCommonResponseHeaders(results);
    }



    @Test
    public void registrationForExistingCoreferenceShouldReturn301() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();
        String redirectUrl = String.format("/entity/%s", EntityRecordUtils.extractIdentifierFromEntityId(entityId));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        is(redirectUrl)));
    }

    @Test
    public void registrationForDeprecatedCoreferenceShouldReturn410() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        deprecateEntity(entityRecord);

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isGone());
    }


    @Test
    public void registrationWithUnknownDatasourceShouldReturn400() throws Exception {
        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_INVALID_SOURCE))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registrationWithEmptyMetisResponseShouldReturn500() throws Exception {
        // mockMetis returns an empty response body for this entity
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_UNKNOWN_ENTITY);
        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is5xxServerError());
    }
}
