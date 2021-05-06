package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_STALIN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_STALIN_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import okhttp3.mockwebserver.MockResponse;

/**
 * Integration test for the main Entity Management controller
 */

@SpringBootTest
@AutoConfigureMockMvc
public class EMControllerIT extends AbstractIntegrationTest {

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

        //TODO assert other important properties

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

        //TODO assert other important properties

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
                
        
//        System.out.println(results);
                results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));
        	//
        	results.andExpect(jsonPath("$.prefLabel[*]", hasSize(24)))
        		.andExpect(jsonPath("$.altLabel[*]", hasSize(12)));
        	        
        		
        //TODO assert other important properties
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
        //TODO Enable when working propertly
//                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
//        http://www.wikidata.org/entity/Q193563
        assertMetisRequest("http://www.wikidata.org/entity/Q193563");

//        return results;
    }

    @Test
    void registerPlaceShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));
        //second enqueue for the update task
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));
//        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));
        
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        System.out.println(results.andReturn().getResponse().getContentAsString());
        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

//        System.out.println(((MockMvc)results).val);
        //TODO assert other important properties
        results.andExpect(jsonPath("$.prefLabel[*]", hasSize(5)))
        .andExpect(jsonPath("$.lat", greaterThan(48.0)))
        .andExpect(jsonPath("$.long", greaterThan(2.0)));
//        .andExpect(jsonPath("$.lat", is(48.85341)))
//        .andExpect(jsonPath("$.long", is(2.3488)));
        
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
        //enable when working propertly
                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())))
//                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q8106");
    }

    @Test
    public void updateConceptShouldBeSuccessful() throws Exception {
        MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, true);

        // matches the id in the JSON file (also used to remove the queued Metis request)
        assertMetisRequest("http://www.wikidata.org/entity/Q152095");

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.content(loadFile(CONCEPT_UPDATE_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(registeredEntityNode.path("id").asText())))
                .andReturn();

        //EntityPreview entityPreview = objectMapper.readValue(loadFile(CONCEPT_BATHTUB), EntityPreview.class);
        final ObjectNode nodeReference = new ObjectMapper().readValue(loadFile(CONCEPT_UPDATE_JSON), ObjectNode.class);
        Optional<EntityRecord> entityRecordUpdated = entityRecordService.retrieveEntityRecordByUri(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(entityRecordUpdated.isPresent());
        Assertions.assertEquals(nodeReference.path("depiction").asText(),
            entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction());
        Assertions.assertEquals(nodeReference.path("note").path("en").path(0).asText(),
            entityRecordUpdated.get().getEuropeanaProxy().getEntity().getNote().get("en").get(0));
        // acquire the reader for the right type
        ObjectReader reader = objectMapper.readerFor(new TypeReference<Map<String,String>>() {});
        Map<String,String> prefLabelToCheck = reader.readValue(nodeReference.path("prefLabel"));
        Map<String,String> prefLabelUpdated = entityRecordUpdated.get().getEuropeanaProxy().getEntity().getPrefLabelStringMap();
        for (Map.Entry<String,String> prefLabelEntry : prefLabelToCheck.entrySet()) {
        	Assertions.assertTrue(prefLabelUpdated.containsKey(prefLabelEntry.getKey()));
        	Assertions.assertTrue(prefLabelUpdated.containsValue(prefLabelEntry.getValue()));
        }

    }

    MvcResult createTestEntityRecord(String europeanaMetadataFile, String metisResponseFile, boolean forUpdate)
	    throws IOException, Exception {
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
        assertRetrieveAPIResultsExternalProfile(contentXml, resultActions, concept);
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
        
        Map<String, String> namespaces = Maps.newHashMap("skos", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        
        resultActions.andExpect(status().isOk())
        	.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "external");
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

        String contentXml = getRetrieveEntityXmlResponse(requestPath, "internal");
        assertRetrieveAPIResultsInternalProfile(contentXml, resultActions, entityPreview);
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
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveEntityRecordByUri(record.getEntityId());

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
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveEntityRecordByUri(record.getEntityId());

        assert dbRecordOptional.isPresent();
        Assertions.assertFalse(dbRecordOptional.get().isDisabled());
    }
    
    
    private void assertEntityExists(MvcResult result) throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
    	final ObjectNode node = new ObjectMapper().readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);
    	Optional<EntityRecord> dbRecord = entityRecordService.retrieveEntityRecordByUri(node.get("id").asText());
        Assertions.assertTrue(dbRecord.isPresent());
    }



    private void assertRetrieveAPIResultsExternalProfile(String contentXml, ResultActions resultActions, Entity entity) throws Exception {
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
   
}
