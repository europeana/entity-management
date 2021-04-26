package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_ERROR_CHECK_1_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_METIS_ERROR_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_1_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_2_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_REGISTER_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.assertj.core.util.Maps;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.entitymanagement.AbstractEmControllerTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.OrganizationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
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

@SpringBootTest
public class EntityRetrievalIT extends AbstractEmControllerTest {

    @BeforeEach
    public void setup() throws Exception {
    	
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }

    @Test
    void retrieveEntityErrorCheck_wrongProfileParameter() throws Exception {
    	/*
    	 * check the error if the profile parameter is wrong
    	 */        
    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
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
    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
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

    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
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

    
    @SuppressWarnings("unused")
    @Test
    void retrieveWithContentNegotiationInMozillaShouldBeSuccessful() throws Exception {
        // read the test data for the Concept entity from the file
	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
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
	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
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
    void retrieveEntityInternalShouldBeSuccessful() throws Exception {

        EntityPreview entityPreview = objectMapper.readValue(loadFile(CONCEPT_REGISTER_JSON), EntityPreview.class);

    	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML);
    	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	String entityId = registeredEntityNode.get("id").asText();

        String requestPath = getEntityRequestPath(entityId);
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "internal");
        assertRetrieveAPIResultsInternalProfile(contentXml, resultActions, entityPreview);
    } 
    
    private void assertRetrieveAPIResultsInternalProfile(String contentXml, ResultActions resultActions, EntityPreview entity) throws Exception {

    	resultActions.andExpect(jsonPath("$.proxies[0].sameAs", Matchers.hasItem(entity.getId())));
    	resultActions.andExpect(jsonPath("$.proxies[1].id", Matchers.is(entity.getId())));
    	resultActions.andExpect(jsonPath("$.proxies[0].proxyFor", Matchers.containsString(WebEntityFields.BASE_DATA_EUROPEANA_URI)));
    	resultActions.andExpect(jsonPath("$.proxies[1].proxyFor", Matchers.containsString(WebEntityFields.BASE_DATA_EUROPEANA_URI)));
    	Assertions.assertTrue(contentXml.contains(entity.getId()));
    	Assertions.assertTrue(contentXml.contains("proxies"));
    	Assertions.assertTrue(contentXml.contains("proxyFor"));
    	Assertions.assertTrue(contentXml.contains(WebEntityFields.BASE_DATA_EUROPEANA_URI));

        for (Entry<String, String> prefLabel : entity.getPrefLabel().entrySet()) {
            resultActions.andExpect(jsonPath("$.proxies[0].prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
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
    
    MvcResult createTestEntityRecord(String europeanaMetadataFile, String metisResponseFile)
    	    throws IOException, Exception {
    	// set mock Metis response
            mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));

        	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                    .content(loadFile(europeanaMetadataFile))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isAccepted())
                    .andReturn();
    	return resultRegisterEntity;
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
            doNothing().when(batchService).launchSingleEntityUpdate(anyString(), anyBoolean());
            return batchService;
        }
    }

}
