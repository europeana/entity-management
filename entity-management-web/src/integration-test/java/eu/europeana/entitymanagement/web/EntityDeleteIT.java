package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.entitymanagement.AbstractEmControllerTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

@SpringBootTest
public class EntityDeleteIT extends AbstractEmControllerTest {

    @BeforeEach
    public void setup() throws Exception {
    	
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
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
    void disableEntityErrorCheck_entityDoesNotExist() throws Exception {
    	/*
    	 * check the error if the entity does not exist
    	 */
        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier")
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityNotFoundException));
    }

    @Test
    void disableEntityErrorCheck_entityDisabled() throws Exception {
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
            doNothing().when(batchService).launchSpecificEntityUpdate(anyList(), anyBoolean());
            return batchService;
        }
    }

}
