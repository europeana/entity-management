package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.util.Maps;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static eu.europeana.api.commons.web.http.HttpHeaders.*;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the main Entity Management controller
 */

@SpringBootTest
@AutoConfigureMockMvc
public class EMControllerIT extends AbstractIntegrationTest {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EntityRecordService entityRecordService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        
        //clear response queue
        if(mockMetis != null && mockMetis.getRequestCount() > 0) {
            for (int i = 0; i < mockMetis.getRequestCount(); i++) {
        	mockMetis.takeRequest(1, TimeUnit.MILLISECONDS);   
	    }
        }
        
        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }

    /**
     * Test that WebApplicationContext loads correctly
     */
    @Test
    public void shouldLoadCorrectly() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean(EMController.class));
    }


    @Test
    public void registerConceptShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));
        

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        results.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));


        checkResponseHeaders(results);


        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q152095");
    }



    @Test
    void registerAgentShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        	.andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        checkResponseHeaders(results);

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q762");
    }

    
    @Test
    void registerAgentStalinShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_STALIN_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_STALIN_XML)));

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

        		
        checkResponseHeaders(results);
    }
    
    @Test
    public void registerOrganizationShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(ORGANIZATION_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(ORGANIZATION_XML)));
        
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

       checkResponseHeaders(results);

        assertMetisRequest("http://www.wikidata.org/entity/Q193563");
    }

    @Test
    void registerPlaceShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

       checkResponseHeaders(results);
        
        // matches id in JSON file
        assertMetisRequest("https://sws.geonames.org/2988507/");
    
    }

    @Test
    void registerTimespanShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(TIMESPAN_XML)));
        //
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(TIMESPAN_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))

                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

checkResponseHeaders(results);
        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q8106");
    }

    @Test
    public void updateConceptShouldBeSuccessful() throws Exception {
        MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, true);

        // matches the id in the JSON file (also used to remove the queued Metis request)
        String externalUri = "http://www.wikidata.org/entity/Q152095";

        // Two calls made to Metis during registration (initial dereferenciation, and again from Update task)
        assertMetisRequest(externalUri);
        assertMetisRequest(externalUri);

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        ResultActions result = mockMvc.perform(put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_UPDATE_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(registeredEntityNode.path("id").asText())));

        checkResponseHeaders(result);

        // Update also triggers a Metis request
        assertMetisRequest(externalUri);

        //EntityPreview entityPreview = objectMapper.readValue(loadFile(CONCEPT_BATHTUB), EntityPreview.class);
        final ObjectNode nodeReference = new ObjectMapper().readValue(loadFile(CONCEPT_UPDATE_JSON), ObjectNode.class);
        Optional<EntityRecord> entityRecordUpdated = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(entityRecordUpdated.isPresent());
        Assertions.assertEquals(nodeReference.path("depiction").asText(),
            entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction());


        // acquire the reader for the right type
        ObjectReader reader = objectMapper.readerFor(new TypeReference<Map<String,String>>() {});
        Map<String,String> prefLabelToCheck = reader.readValue(nodeReference.path("prefLabel"));
        Map<String,String> prefLabelUpdated = entityRecordUpdated.get().getEuropeanaProxy().getEntity().getPrefLabelStringMap();
        for (Map.Entry<String,String> prefLabelEntry : prefLabelToCheck.entrySet()) {
        	Assertions.assertTrue(prefLabelUpdated.containsKey(prefLabelEntry.getKey()));
        	Assertions.assertTrue(prefLabelUpdated.containsValue(prefLabelEntry.getValue()));
        }

    }

    @Test
    void updatePUTShouldReplaceEuropeanaProxy() throws Exception{
        MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, true);

        // matches the id in the JSON file (also used to remove the queued Metis request)
        String externalUri = "http://www.wikidata.org/entity/Q152095";
        assertMetisRequest(externalUri);
        assertMetisRequest(externalUri);

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        // assert content of Europeana proxy
        Optional<EntityRecord> savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        Entity europeanaProxyEntity = savedRecord.get().getEuropeanaProxy().getEntity();

        // values match labels in json file
        Assertions.assertEquals("bathtub", europeanaProxyEntity.getPrefLabelStringMap().get("en"));
        Assertions.assertEquals("bath", europeanaProxyEntity.getAltLabel().get("en").get(0));
        Assertions.assertEquals("tub", europeanaProxyEntity.getAltLabel().get("en").get(1));
        Assertions.assertNotNull(europeanaProxyEntity.getDepiction());

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        ResultActions resultActions = mockMvc.perform(put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_EMPTY_UPDATE__JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        checkResponseHeaders(resultActions);

        // update triggers a Metis request
        assertMetisRequest(externalUri);

        // check that update removed fields from Europeana proxy in original request
        savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        europeanaProxyEntity = savedRecord.get().getEuropeanaProxy().getEntity();

        Assertions.assertNull(europeanaProxyEntity.getPrefLabel());
        Assertions.assertNull(europeanaProxyEntity.getAltLabel());
        Assertions.assertNull(europeanaProxyEntity.getNote());
        Assertions.assertNull(europeanaProxyEntity.getDepiction());
    }

    MvcResult createTestEntityRecord(String europeanaMetadataFile, String metisResponseFile, boolean forUpdate)
	    throws Exception {
	// set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));
        //second request for update task during create
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));
        //third request of update when update method is called
        if(forUpdate) {
            mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(metisResponseFile)));  
        }

    	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(europeanaMetadataFile))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andReturn();
	return resultRegisterEntity;
    }


    @Test
    void retrieveConceptExternalShouldBeSuccessful() throws Exception {
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
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(concept.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

        checkResponseHeaders(resultActions);

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, concept);
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

    
    @SuppressWarnings("unused")
    @Test
    public void retrieveWithContentNegotiationInMozillaShouldBeSuccessful() throws Exception {
        // read the test data for the Concept entity from the file
	MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, false);
	final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
	String entityId = registeredEntityNode.get("id").asText();
	String defaultMozillaAcceptHeader = "ext/html,application/xhtml+xml,application/xml;q=0.9,*/*";
	String requestPath = getEntityRequestPath(entityId);
	ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(defaultMozillaAcceptHeader));
        
        Map<String, String> namespaces = Maps.newHashMap("skos", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        resultActions.andExpect(status().isOk())
        	.andExpect(MockMvcResultMatchers.content().contentType(CONTENT_TYPE_APPLICATION_RDF_XML));
 //TODO: enable when the XML Serialization is fixed
//                .andExpect(xpath("//Concept/id", namespaces).string(entityId))
//                .andExpect(xpath("//Concept/type", namespaces).string(EntityTypes.Concept.name()));

    }
    
    @SuppressWarnings("unused")
    @Test
    public void retrieveWithContentNegotiationShouldBeSuccessful() throws Exception {
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
        
        resultActions.andExpect(status().isOk())
        .andExpect(header().string(HEADER_CONTENT_TYPE,
                is(CONTENT_TYPE_JSONLD_UTF8)))
        	.andExpect(jsonPath("$.id", is(entityId)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));
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

    @Test
    void updateFromExternalDatasourceShouldRunSuccessfully() throws Exception {
        // create entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath + "/management/update")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        //TODO: test error flows
    }

    @Test
    void deletionShouldBeSuccessful() throws Exception {
        // create entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // check that record was disabled
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveByEntityId(record.getEntityId());

        assert dbRecordOptional.isPresent();
        Assertions.assertTrue(dbRecordOptional.get().isDisabled());
    }


    @Test
    void deletionFailsIfMatch() throws Exception {
        // create entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath).header(HttpHeaders.IF_MATCH, "wrong_etag_value")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());

        // check that record was disabled
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveByEntityId(record.getEntityId());

        assert dbRecordOptional.isPresent();
        Assertions.assertFalse(dbRecordOptional.get().isDisabled());
    }

    @Test
    void reEnableDisabledEntityShouldBeSuccessful() throws Exception {
        // create disbaled entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.disableEntityRecord(entityRecord);
       // check if entity is disabled
        Assertions.assertTrue(record.isDisabled());

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // check that record was re-enabled
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveByEntityId(record.getEntityId());

        assert dbRecordOptional.isPresent();
        Assertions.assertFalse(dbRecordOptional.get().isDisabled());
    }

    @Test
    void reEnableNonDisabledEntityShouldBeSuccessful() throws Exception {
        // create entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);
        // check if entity is NOT disabled
        Assertions.assertFalse(entityRecord.isDisabled());

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // check that record was re-enabled
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveByEntityId(record.getEntityId());

        assert dbRecordOptional.isPresent();
        Assertions.assertFalse(dbRecordOptional.get().isDisabled());
    }


    @Test
    public void changeProvenanceShouldBeSuccessful() throws Exception {
        MvcResult resultRegisterEntity = createTestEntityRecord(AGENT_REGISTER_JAN_VERMEER, AGENT_JAN_VERMEER_XML_VIAF, false);

        // matches the id in the JSON file (also used to remove the queued Metis request)
        String externalUriViaf = "http://viaf.org/viaf/51961439";
        assertMetisRequest(externalUriViaf);
        assertMetisRequest(externalUriViaf);

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        // assert content of External proxy
        Optional<EntityRecord> savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        EntityProxy externalProxy = savedRecord.get().getExternalProxy();

        Assertions.assertEquals(externalUriViaf, externalProxy.getProxyId());

        String externalUriWikidata = "http://www.wikidata.org/entity/Q41264";
        // Metis request made with new externalUri
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_JAN_VERMEER_XML_WIKIDATA)));

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_SERVICE_URL + "/" + requestPath + "/management/source")
                .param(WebEntityConstants.PATH_PARAM_URL, externalUriWikidata)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();


        assertMetisRequest(externalUriWikidata);

        // check that ExternalProxy ID is updated
        savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        externalProxy = savedRecord.get().getExternalProxy();

        Assertions.assertEquals(externalUriWikidata, externalProxy.getProxyId());
    }

    private void checkResponseHeaders(ResultActions results) throws Exception {
        results.andExpect(header().string(HEADER_CONTENT_TYPE,
                is(CONTENT_TYPE_JSONLD_UTF8)))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.LINK, is(VALUE_LDP_RESOURCE)))
                .andExpect(header().stringValues(HttpHeaders.VARY, hasItems(containsString(HttpHeaders.ACCEPT))));
    }

    private void assertEntityExists(MvcResult result) throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
    	final ObjectNode node = new ObjectMapper().readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	Optional<EntityRecord> dbRecord = entityRecordService.retrieveByEntityId(node.get("id").asText());
        Assertions.assertTrue(dbRecord.isPresent());
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

    private String getRetrieveEntityXmlResponse(String requestPath) throws Exception {
    	MvcResult resultXml = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))
        		.andExpect(status().isOk())
                // content-type matches Accept header
                .andExpect(header().string(HEADER_CONTENT_TYPE,
                        is(MediaType.APPLICATION_XML_VALUE)))
        		.andReturn();
        return resultXml.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
   
}
