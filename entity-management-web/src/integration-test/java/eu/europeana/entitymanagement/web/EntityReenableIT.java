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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

public class EntityReenableIT extends AbstractIntegrationTest {

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
