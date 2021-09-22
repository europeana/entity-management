package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_DAVINCI_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_BNF_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_BNF_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_REGISTER_BNF_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_PARIS_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_PARIS_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REGISTER_PARIS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_1ST_CENTURY_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_1ST_CENTURY_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityRetrievalIT extends BaseWebControllerTest {

    @Test
    void retrieveWithInvalidProfileShouldReturn400() throws Exception {
        /*
         * check the error if the profile parameter is wrong
         */

        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "wrong-profile-parameter")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrieveNonExistingEntityShouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier.jsonld")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void retrievalErrorWithDebugProfileShouldIncludeStacktrace() throws Exception{
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier.jsonld?profile=debug")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.trace").exists());
    }

    @Test
    void retrieveDeprecatedEntityShouldReturn410() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        deprecateEntity(entityRecord);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());

    }

    @Test
    void retrieveConceptJsonExternalShouldBeSuccessful() throws Exception {

        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)));
    }

    @Test
    void retrieveConceptJsonExternalWithLanguageFilteringShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .param(WebEntityConstants.QUERY_PARAM_LANGUAGE, "en,de")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
                .andExpect(jsonPath("$.prefLabel[*]", hasSize(2)))
                .andExpect(jsonPath("$.altLabel[*]", hasSize(2)));
    }

    @Test
    public void retrieveAgentJsonExternalShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(AGENT_REGISTER_DAVINCI_JSON);
        String metisResponse = loadFile(AGENT_DA_VINCI_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, AGENT_DA_VINCI_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())));
    }

    @Test
    void retrieveConceptJsonExternalSchemaOrgShouldBeSuccessful() throws Exception {

        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.@id", is(entityId)))
        		.andExpect(jsonPath("$.name").isNotEmpty())
        		.andExpect(jsonPath("$.description").isNotEmpty())
        		.andExpect(jsonPath("$.alternateName").isNotEmpty());
    }
  
    @Test
    public void retrieveAgentExternalSchemaOrgShouldBeSuccessful() throws Exception {

    	String europeanaMetadata = loadFile(AGENT_REGISTER_DAVINCI_JSON);
        String metisResponse = loadFile(AGENT_DA_VINCI_XML);
        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, AGENT_DA_VINCI_URI);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.@id", is(entityRecord.getEntityId())));

        for (String sameAsElem : entityRecord.getEntity().getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        }
        resultActions.andExpect(jsonPath("$.gender").isNotEmpty());
        resultActions.andExpect(jsonPath("$.deathDate").isNotEmpty());
        resultActions.andExpect(jsonPath("$.birthDate").isNotEmpty());
        resultActions.andExpect(jsonPath("$.name").isNotEmpty());
        resultActions.andExpect(jsonPath("$.alternateName").isNotEmpty());
    }

    @Test
    public void retrieveOrganizationExternalSchemaOrgShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(ORGANIZATION_REGISTER_BNF_JSON);
        String metisResponse = loadFile(ORGANIZATION_BNF_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, ORGANIZATION_BNF_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.@id", is(entityId)))
                .andExpect(jsonPath("$.sameAs").isNotEmpty());
    }

    @Test
    public void retrievePlaceExternalSchemaOrgShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(PLACE_REGISTER_PARIS_JSON);
        String metisResponse = loadFile(PLACE_PARIS_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, PLACE_PARIS_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.@id", is(entityId)))
                .andExpect(jsonPath("$.geo").isNotEmpty())
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.alternateName").isNotEmpty())
                .andExpect(jsonPath("$.sameAs").isNotEmpty());
    }

    @Test
    public void retrieveOrganizationJsonExternalShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(ORGANIZATION_REGISTER_BNF_JSON);
        String metisResponse = loadFile(ORGANIZATION_BNF_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, ORGANIZATION_BNF_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())));
    }

    @Test
    public void retrievePlaceJsonExternalShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(PLACE_REGISTER_PARIS_JSON);
        String metisResponse = loadFile(PLACE_PARIS_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, PLACE_PARIS_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())));
    }

    @Test
    public void retrieveTimespanJsonExternalShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(TIMESPAN_REGISTER_1ST_CENTURY_JSON);
        String metisResponse = loadFile(TIMESPAN_1ST_CENTURY_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, TIMESPAN_1ST_CENTURY_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())));
    }


    @Test
    public void retrieveEntityInternalShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        String entityId = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
                .andExpect(jsonPath("$.proxies", hasSize(2)));
    }

    @Test
    public void proxiesShouldNotHaveMetrics() throws Exception {

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_DAVINCI_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal"))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        results
                .andExpect(jsonPath("$.proxies[0].proxyIn").exists())
                .andExpect(jsonPath("$.proxies[0].proxyIn.pageRank").doesNotExist())
                .andExpect(jsonPath("$.proxies[0].proxyIn.recordCount").doesNotExist())
                .andExpect(jsonPath("$.proxies[0].proxyIn.score").doesNotExist());

        results.andExpect(jsonPath("$.proxies[1].proxyIn").exists())
                .andExpect(jsonPath("$.proxies[1].proxyIn.pageRank").doesNotExist())
                .andExpect(jsonPath("$.proxies[1].proxyIn.recordCount").doesNotExist())
                .andExpect(jsonPath("$.proxies[1].proxyIn.score").doesNotExist());


    }
    //TODO: add tests for XML retrieval
}
