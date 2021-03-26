package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_JSON;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Entity;
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
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
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

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q11019");
    }

    @Test
    public void registerAgentShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q762");
    }

    @Test
    public void registerOrganizationShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(ORGANIZATION_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q193563");
    }

    @Test
    public void registerPlaceShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("https://sws.geonames.org/2988507/");
    }

    @Test
    public void registerTimespanShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(TIMESPAN_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.entity.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q8106");
    }

    @Test
    public void updateConceptShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

    	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andReturn();

        // matches the id in the JSON file (also used to remove the queued Metis request)
        assertMetisRequest("http://www.wikidata.org/entity/Q11019");

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
        Assertions.assertTrue(entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction().equals(nodeReference.path("depiction").asText()));
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
    void retrieveConceptShouldBeSuccessful() throws Exception {
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
                .andExpect(jsonPath("$.id", is(concept.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

        String contentXml = getRetrieveEntityXmlResponse(requestPath);
        assertSomeEntityFields(contentXml, resultActions, concept);
    }

    @Test
    void retrieveAgentShouldBeSuccessful() throws Exception {
        // read the test data for the Agent entity from the file
        AgentImpl agent = objectMapper.readValue(loadFile(AGENT_JSON), AgentImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
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
        assertSomeEntityFields(contentXml, resultActions, agent);
    }

    @Test
    void retrieveOrganizationShouldBeSuccessful() throws Exception {
        // read the test data for the Organization entity from the file
        OrganizationImpl organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), OrganizationImpl.class);
        EntityRecord entityRecord =  new EntityRecordImpl();
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
        assertSomeEntityFields(contentXml, resultActions, organization);
    }

    @Test
    void retrievePlaceShouldBeSuccessful() throws Exception {
        // read the test data for the Place entity from the file
        PlaceImpl place = objectMapper.readValue(loadFile(PLACE_JSON), PlaceImpl.class);
        EntityRecord entityRecord =  new EntityRecordImpl();
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
        assertSomeEntityFields(contentXml, resultActions, place);
    }

    @Test
    void retrieveTimespanShouldBeSuccessful() throws Exception {
        // read the test data for the Timespan entity from the file
        TimespanImpl timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), TimespanImpl.class);
        EntityRecord entityRecord =  new EntityRecordImpl();
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
        assertSomeEntityFields(contentXml, resultActions, timespan);
    }

    @Test
    void updateFromExternalDatasourceShouldRunSuccessfully() throws Exception {
        // create entity in DB
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
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
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
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
        Assertions.assertTrue(dbRecordOptional.get().getDisabled());
    }


    private void assertSomeEntityFields(String contentXml, ResultActions resultActions, Entity entity) throws Exception {
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

    private String getRetrieveEntityXmlResponse(String requestPath) throws Exception {
    	MvcResult resultXml = mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))
        		.andExpect(status().isOk())
        		.andReturn();
        return resultXml.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
