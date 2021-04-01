package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_ERROR_CHECK_1_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_1_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_2_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractEmControllerTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import okhttp3.mockwebserver.MockResponse;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

@SpringBootTest
public class EMControllerErrorIT extends AbstractEmControllerTest {

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }

    @Test
    public void registerEntityErrorsCheck() throws Exception {
    	/*
    	 * check the error when the entity already exists
    	 *  
    	 */
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

        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q152095");
        
        /*
         * TODO: uncomment the code below when the consolidated entity is populated because when 
    	 * it is left empty (as in the current implementation of the register API), it is not possible to find
    	 * the already existing entity by looking in the "co-reference" table of the consolidated entity
         */
//        mockMvc.perform(post(BASE_SERVICE_URL)
//                .content(loadFile(CONCEPT_REGISTER_JSON))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isMovedPermanently());
     
        /*
         * check the error if the entity exists with the id in the sameAs field of the metis entity
         */
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_ERROR_CHECK_1_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_ERROR_CHECK_1_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isMovedPermanently());
        
        // matches id in JSON file
        assertMetisRequest("http://www.wikidata.org/entity/Q11019-2");

        /*
         * check the error if the entity id is not in the datasources
         */
        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_ERROR_CHECK_2_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
        
    }

    @Test
    public void updateEntityErrorsCheck() throws Exception {
    	/*
    	 * check the error if the If-Match header does not comply
    	 */
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

    	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andReturn();

        // matches the id in the JSON file (also used to remove the queued Metis request)
        assertMetisRequest("http://www.wikidata.org/entity/Q152095");

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.header(HttpHeaders.IF_MATCH, "wrong-if-match-header")
        		.content(loadFile(CONCEPT_UPDATE_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());

        /*
         * check the error if the entity does not exist prior to its update
         */
        this.entityRecordService.dropRepository();
        
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.content(loadFile(CONCEPT_UPDATE_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());        
    }

    @Test
    public void updateEntityFromExternalSourceErrorsCheck() throws Exception {
    	/*
    	 * check the error if the entity does not exist
    	 */
    	mockMvc.perform(post(BASE_SERVICE_URL+"/"+"wrong-type/wrong-identifier/management/update")
    			.contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityNotFoundException));

    	/*
    	 * check the error if the entity is removed/disabled
    	 */
        // create entity in the DB
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath + "/management/update")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));
       
    }
    
    @Test
    void retrieveEntityErrorsCheck() throws Exception {
    	/*
    	 * check the error if the profile parameter is wrong
    	 */
        // read the test data for the Concept entity from the file
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(concept.getEntityId());
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "wrong-profile-parameter")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        /*
    	 * check the error if the entity does not exist
    	 */
        mockMvc.perform(get(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier" + ".jsonld")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());        
    }
   
    @Test
    void disableEntityErrorChecks() throws Exception {
    	/*
    	 * check the error if the entity does not exist
    	 */
        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier")
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityNotFoundException));

    	/*
    	 * check the error if the entity is removed/disabled
    	 */
        // create entity in the DB
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        EntityRecord entityRecord = new EntityRecordImpl();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));
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
            doNothing().when(batchService).launchSingleEntityUpdate(anyString());
            return batchService;
        }
    }


}
