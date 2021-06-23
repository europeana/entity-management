package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JAN_VERMEER_XML_VIAF;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_JAN_VERMEER;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

public class EntityChangeProvenanceIT extends AbstractIntegrationTest {

    @Test
    public void changeProvenanceShouldBeSuccessful() throws Exception {
        MvcResult resultRegisterEntity = createTestEntityRecord(AGENT_REGISTER_JAN_VERMEER, AGENT_JAN_VERMEER_XML_VIAF, false);

        // matches the id in the JSON file (also used to remove the queued Metis request)
        String externalUriViaf = "http://viaf.org/viaf/51961439";
 
        final ObjectNode registeredEntityNode = new ObjectMapper().readValue(resultRegisterEntity.getResponse().getContentAsString(StandardCharsets.UTF_8), ObjectNode.class);

        // assert content of External proxy
        Optional<EntityRecord> savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        EntityProxy externalProxy = savedRecord.get().getExternalProxy();

        Assertions.assertEquals(externalUriViaf, externalProxy.getProxyId());

        entityRecordService.mergeEntity(savedRecord.get());
        entityRecordService.saveEntityRecord(savedRecord.get());
        
        String externalUriWikidata = "http://www.wikidata.org/entity/Q41264";

        String requestPath = getEntityRequestPath(registeredEntityNode.path("id").asText());
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_SERVICE_URL + "/" + requestPath + "/management/source")
                .param(WebEntityConstants.PATH_PARAM_URL, externalUriWikidata)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();

        // check that ExternalProxy ID is updated
        savedRecord = entityRecordService.retrieveByEntityId(registeredEntityNode.path("id").asText());
        Assertions.assertTrue(savedRecord.isPresent());
        externalProxy = savedRecord.get().getExternalProxy();

        Assertions.assertEquals(externalUriWikidata, externalProxy.getProxyId());
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
