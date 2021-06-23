package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_EMPTY_UPDATE__JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import okhttp3.mockwebserver.MockResponse;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

public class EntityUpdateIT extends AbstractIntegrationTest {
	
    @Test
    public void updateEntityErrorCheck_wrongIfMatchHeader() throws Exception {
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

        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.header(HttpHeaders.IF_MATCH, "wrong-if-match-header")
        		.content(loadFile(CONCEPT_UPDATE_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }
    
    @Test
    public void updateEntityErrorCheck_entityDoesNotExist() throws Exception { 
        /*
         * check the error if the entity does not exist prior to its update
         */
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + "concept/1")
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.content(loadFile(CONCEPT_UPDATE_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());        
    }
    
    @Test
    public void updateEntityErrorCheck_entityDisabled() throws Exception {
    	/*
    	 * check the error if the entity is removed/disabled
    	 */
        Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
        EntityRecord entityRecord =  new EntityRecord();
        entityRecord.setEntity(timespan);
        entityRecord.setEntityId(timespan.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);
        
        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
        		.param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
        		.content(loadFile(TIMESPAN_JSON))
        		.contentType(MediaType.APPLICATION_JSON))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));
    }

    @Test
    public void updateEntityFromExternalSourceErrorCheck_entityDoesNotExist() throws Exception {
    	/*
    	 * check the error if the entity does not exist
    	 */
    	mockMvc.perform(post(BASE_SERVICE_URL+"/"+"wrong-type/wrong-identifier/management/update")
    			.contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityNotFoundException));
    }

    @Test
    public void updateEntityFromExternalSourceErrorCheck_entityDisabled() throws Exception {
    	/*
    	 * check the error if the entity is removed/disabled
    	 */
        // create entity in the DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
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
    public void updateConceptShouldBeSuccessful() throws Exception {
    	
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

    	MvcResult resultRegisterEntity = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andReturn();
    	
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
        Optional<EntityRecord> entityRecordUpdated = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
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
        
    @Test
    void updatePUTShouldReplaceEuropeanaProxy() throws Exception{
        MvcResult resultRegisterEntity = createTestEntityRecord(CONCEPT_REGISTER_JSON, CONCEPT_XML, true);

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
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_EMPTY_UPDATE__JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();

        // check that update removed fields from Europeana proxy in original request
        savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        europeanaProxyEntity = savedRecord.get().getEuropeanaProxy().getEntity();

        Assertions.assertNull(europeanaProxyEntity.getPrefLabel());
        Assertions.assertNull(europeanaProxyEntity.getAltLabel());
        Assertions.assertNull(europeanaProxyEntity.getNote());
        Assertions.assertNull(europeanaProxyEntity.getDepiction());
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
