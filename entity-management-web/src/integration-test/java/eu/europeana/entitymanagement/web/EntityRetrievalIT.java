package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.util.Maps;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import okhttp3.mockwebserver.MockResponse;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

public class EntityRetrievalIT extends AbstractIntegrationTest {

    @Test
    void retrieveEntityErrorCheck_wrongProfileParameter() throws Exception {
    	/*
    	 * check the error if the profile parameter is wrong
    	 */        
    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
    	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	String entityId = registeredEntityNode.get("id").asText();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "wrong-profile-parameter")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof HttpBadRequestException));
    }
    
    @Test
    void retrieveEntityErrorCheck_entityDoesNotExist() throws Exception {
        /*
    	 * check the error if the entity does not exist
    	 */
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier" + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());   
    }
    
    @Test
    void retrieveEntityErrorCheck_entityDisabled() throws Exception {    
        /*
    	 * check the error if the entity is disabled
    	 */
    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
    	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	String entityId = registeredEntityNode.get("id").asText();
        
        String requestPath = getEntityRequestPath(entityId);
        
        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));   

    }

    @Test
    void retrieveConceptExternalShouldBeSuccessful() throws Exception {

    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
    	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	String entityId = registeredEntityNode.get("id").asText();

        String requestPath = getEntityRequestPath(entityId);
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_RDF_ABOUT));
        Assertions.assertTrue(contentXml.contains(entityId));
    }

    @Test
    void retrieveConceptWithLanguageFilteringShouldBeSuccessful() throws Exception {
	    // read the test data for the Concept entity from the file
		//TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
		Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(concept.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.param(WebEntityConstants.QUERY_PARAM_LANGUAGE, "en,de")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(concept.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

        resultActions.andExpect(jsonPath("$.prefLabel[*]", hasSize(2)))
		.andExpect(jsonPath("$.altLabel[*]", hasSize(1)));
    }
    
    @Test
    public void retrieveAgentExternalShouldBeSuccessful() throws Exception {
        //TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
	// read the test data for the Agent entity from the file
        Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(agent);
        entityRecord.setEntityId(agent.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(agent.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(agent.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, agent);
    }

    @Test
    public void retrieveOrganizationExternalShouldBeSuccessful() throws Exception {
	//TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
	// read the test data for the Organization entity from the file
        Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
        EntityRecord entityRecord =  new EntityRecord();
        entityRecord.setEntity(organization);
        entityRecord.setEntityId(organization.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(organization.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(organization.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, organization);
    }

    @Test
    public void retrievePlaceExternalShouldBeSuccessful() throws Exception {
	//TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
	// read the test data for the Place entity from the file
        Place place = objectMapper.readValue(loadFile(PLACE_JSON), Place.class);
        EntityRecord entityRecord =  new EntityRecord();
        entityRecord.setEntity(place);
        entityRecord.setEntityId(place.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(place.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(place.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, place);
    }

    @Test
    public void retrieveTimespanExternalShouldBeSuccessful() throws Exception {
	//TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
	// read the test data for the Timespan entity from the file
        Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
        EntityRecord entityRecord =  new EntityRecord();
        entityRecord.setEntity(timespan);
        entityRecord.setEntityId(timespan.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(timespan.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(timespan.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, timespan);
    }
    
    @SuppressWarnings("unused")
    @Test
    void retrieveWithContentNegotiationInMozillaShouldBeSuccessful() throws Exception {
        // read the test data for the Concept entity from the file
	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
	String entityId = registeredEntityNode.get("id").asText();
	String defaultMozillaAcceptHeader = "ext/html,application/xhtml+xml,application/xml;q=0.9,*/*";
	String requestPath = getEntityRequestPath(entityId);
	logger.debug("Retrieving entity record /{} with accept header: ", requestPath, defaultMozillaAcceptHeader);
	ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(defaultMozillaAcceptHeader));
        
        Map<String, String> namespaces = Maps.newHashMap("skos", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        
        logger.debug("Retrieve entity resonse: {}", resultActions.andReturn().getResponse().getContentAsString());
        resultActions.andExpect(status().isOk())
        	.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_XML));
 //TODO: enable when the XML Serialization is fixed
//                .andExpect(xpath("//Concept/id", namespaces).string(entityId))
//                .andExpect(xpath("//Concept/type", namespaces).string(EntityTypes.Concept.name()));

    }
    
    @SuppressWarnings("unused")
    @Test
    void retrieveWithContentNegotiationShouldBeSuccessful() throws Exception {
        // read the test data for the Concept entity from the file
	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
	String response = resultRegisterEntity.getResponse().getContentAsString();
	//TODO read the id from response, for the time being we can assume the id is 1
	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
	String entityId = registeredEntityNode.get("id").asText();
	String anyFormat = "*/*";
	String requestPath = getEntityRequestPath(entityId);
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(anyFormat));
        
        Map<String, String> namespaces = Maps.newHashMap("skos", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        
        resultActions.andExpect(status().isOk())
        	.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        	.andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));
    }
    
    @Test
    public void retrieveEntityInternalShouldBeSuccessful() throws Exception {

//    	ResultActions registeredEntityResults = registerConceptShouldBeSuccessful();
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

	//TODO: switch to the use of MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
	// read the test data for the entity from the file
        EntityPreview entityPreview = objectMapper.readValue(loadFile(CONCEPT_REGISTER_JSON), EntityPreview.class);

        ResultActions registeredEntityResults = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(registeredEntityResults.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertRetrieveAPIResultsInternalProfile(resultActions, entityPreview);
    }
    
    private String getRetrieveEntityXmlResponse(String requestPath) throws Exception {
    	MvcResult resultXml = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))
        		.andExpect(status().isOk())
        		.andReturn();
        return resultXml.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
    
    private void assertRetrieveAPIResultsExternalProfile(String contentXml, ResultActions resultActions, Entity entity) throws Exception {
        //TODO: use xpath instead of String.contains() for checking XML response
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_SKOS_PREF_LABEL));
        for (Entry<String, String> prefLabel : entity.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
        }
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_OWL_SAME_AS));
        for (String sameAsElem : entity.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(contentXml.contains(sameAsElem));
        }
    }
    
    /**
     * Checks API responses for the internal profile.
     * Only JSON-LD is supported for this profile at the moment.
     */
    private void assertRetrieveAPIResultsInternalProfile(ResultActions resultActions, EntityPreview entity) throws Exception {

    	resultActions.andExpect(jsonPath("$.proxies[0].sameAs", Matchers.hasItem(entity.getId())));
    	resultActions.andExpect(jsonPath("$.proxies[1].id", Matchers.is(entity.getId())));
    	resultActions.andExpect(jsonPath("$.proxies[0].proxyFor", Matchers.containsString(WebEntityFields.BASE_DATA_EUROPEANA_URI)));
    	resultActions.andExpect(jsonPath("$.proxies[1].proxyFor", Matchers.containsString(WebEntityFields.BASE_DATA_EUROPEANA_URI)));

        for (Entry<String, String> prefLabel : entity.getPrefLabel().entrySet()) {
            resultActions.andExpect(jsonPath("$.proxies[0].prefLabel", Matchers.hasKey(prefLabel.getKey())));
        }
    }
    
    private String getRetrieveEntityXmlResponse(String requestPath, String profile) throws Exception {
    	MvcResult resultXml = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, profile)
                .accept(MediaType.APPLICATION_XML))
        		.andExpect(status().isOk())
        		.andReturn();
        return resultXml.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
    
    @TestConfiguration
    public static class TestConfig {

        /**
         * Do not trigger batch jobs in this test.
         */
        @Bean
        @Primary
        public BatchService batchServiceBean() throws Exception {
            BatchService batchService = Mockito.mock(BatchService.class);
            doNothing().when(batchService).launchSpecificEntityUpdate(anyList(), anyBoolean());
            return batchService;
        }
    }

}
