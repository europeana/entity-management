package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.METIS_DEREF_PATH;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.OrganizationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.definitions.model.impl.TimespanImpl;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

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

    /**
     * MockWebServer needs to be static, so we can inject it's port into the Spring context.
     * <p>
     * Since the WebServer is re-used across tests, every test interacting with Metis NEEDS to call assertMetisRequest()
     * to clear its queue!
     * <p>
     * See: https://github.com/spring-projects/spring-framework/issues/24825
     */

    private static MockWebServer mockMetis;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("metis.baseUrl", () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
    }

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }


    @BeforeAll
    static void setupAll() {
        mockMetis = new MockWebServer();
    }

    @AfterAll
    static void teardownAll() throws IOException {
        mockMetis.shutdown();
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
    public void registerEntityShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(BATHTUB_DEREF)));
        MvcResult result = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_BATHTUB))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andReturn();
        
        EntityRecord response = objectMapper.readValue(result.getResponse().getContentAsString(), EntityRecord.class);
        Optional<EntityRecord> dbRecord = entityRecordService.retrieveEntityRecordByUri(response.getEntityId());
        Assertions.assertTrue(dbRecord.isPresent());
        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q11019");
    }


    @Test
    void retrieveEntityShouldBeSuccessful() throws Exception {
        // read the test data for the Concept entity from the file
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(concept.getEntityId());
        ResultActions resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(concept.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));
        
        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        byte[] bytes = contentXml.getBytes();
        String utf8DecodedString = new String(bytes, StandardCharsets.UTF_8);
        
        Assertions.assertTrue(utf8DecodedString.contains(XmlFields.XML_SKOS_PREF_LABEL));        
        for (Entry<String, String> prefLabel : concept.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(utf8DecodedString.contains(prefLabel.getKey()));
        }      
        Assertions.assertTrue(utf8DecodedString.contains(XmlFields.XML_OWL_SAME_AS));  
        for (String sameAsElem : concept.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(utf8DecodedString.contains(sameAsElem));
        }
               
        // read the test data for the Agent entity from the file
        AgentImpl agent = objectMapper.readValue(loadFile(AGENT_JSON), AgentImpl.class);
        entityRecord.setEntity(agent);
        entityRecord.setEntityId(agent.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        requestPath = getEntityRequestPath(agent.getEntityId());
        resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(agent.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())));
        
        contentXml = getRetrieveEntityXmlResponse(requestPath);
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_SKOS_PREF_LABEL));        
        for (Entry<String, String> prefLabel : agent.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
        }      
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_OWL_SAME_AS));  
        for (String sameAsElem : agent.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(contentXml.contains(sameAsElem));
        }

        // read the test data for the Organization entity from the file
        OrganizationImpl organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), OrganizationImpl.class);
        entityRecord.setEntity(organization);
        entityRecord.setEntityId(organization.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        requestPath = getEntityRequestPath(organization.getEntityId());
        resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(organization.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())));
        
        contentXml = getRetrieveEntityXmlResponse(requestPath);
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_SKOS_PREF_LABEL));        
        for (Entry<String, String> prefLabel : organization.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
        }      
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_OWL_SAME_AS));  
        for (String sameAsElem : organization.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(contentXml.contains(sameAsElem));
        }

        
        // read the test data for the Place entity from the file
        PlaceImpl place = objectMapper.readValue(loadFile(PLACE_JSON), PlaceImpl.class);
        entityRecord.setEntity(place);
        entityRecord.setEntityId(place.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        requestPath = getEntityRequestPath(place.getEntityId());
        resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(place.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())));
        
        contentXml = getRetrieveEntityXmlResponse(requestPath);
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_SKOS_PREF_LABEL));        
        for (Entry<String, String> prefLabel : place.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
        }      
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_OWL_SAME_AS));  
        for (String sameAsElem : place.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(contentXml.contains(sameAsElem));
        }
        // read the test data for the Timespan entity from the file
        TimespanImpl timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), TimespanImpl.class);
        entityRecord.setEntity(timespan);
        entityRecord.setEntityId(timespan.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        requestPath = getEntityRequestPath(timespan.getEntityId());
        resultActions = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId", is(timespan.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())));
        
        contentXml = getRetrieveEntityXmlResponse(requestPath);
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_SKOS_PREF_LABEL));        
        for (Entry<String, String> prefLabel : timespan.getPrefLabelStringMap().entrySet()) {
            resultActions.andExpect(jsonPath("$.prefLabel", Matchers.hasKey(prefLabel.getKey())));
            Assertions.assertTrue(contentXml.contains(prefLabel.getKey()));
        }      
        Assertions.assertTrue(contentXml.contains(XmlFields.XML_OWL_SAME_AS));  
        for (String sameAsElem : timespan.getSameAs()) {
        	resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
        	Assertions.assertTrue(contentXml.contains(sameAsElem));
        }
    }
    
    private String getRetrieveEntityXmlResponse(String requestPath) throws Exception {
    	MvcResult resultXml = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))            
        		.andExpect(status().isOk())
        		.andReturn();               
        return resultXml.getResponse().getContentAsString();
    }

    /**
     * Asserts that a de-reference request was actually made to the mock Metis server
     *
     * @param uri uri param in request
     * @throws InterruptedException
     */
    private void assertMetisRequest(String uri) throws InterruptedException {
        // check that app actually sent request
        HttpUrl requestedUrl = mockMetis.takeRequest().getRequestUrl();
        assert requestedUrl != null;
        Assertions.assertEquals(METIS_DEREF_PATH, requestedUrl.encodedPath());
        Assertions.assertEquals(uri, requestedUrl.queryParameter("uri"));
    }
}

