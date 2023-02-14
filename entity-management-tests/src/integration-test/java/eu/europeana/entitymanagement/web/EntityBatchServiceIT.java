package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.service.EntityBatchService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityBatchServiceIT extends AbstractIntegrationTest {

    @Autowired
    private EntityBatchService entityBatchService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void performGlobalReferentialIntegrity() throws Exception {
        entityRecordService.dropRepository();
        // read the test data from the file
        Agent agent1 =
                objectMapper.readValue(
                        IntegrationTestUtils.loadFile(IntegrationTestUtils.AGENT1_REFERENTIAL_INTEGRITY_JSON),
                        Agent.class);
        EntityRecord entityRecord1 = new EntityRecord();
        entityRecord1.setEntity(agent1);
        entityRecord1.setEntityId(agent1.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord1);

        Agent agent2 =
                objectMapper.readValue(
                        IntegrationTestUtils.loadFile(IntegrationTestUtils.AGENT2_REFERENTIAL_INTEGRITY_JSON),
                        Agent.class);
        EntityRecord entityRecord2 = new EntityRecord();
        entityRecord2.setEntity(agent2);
        entityRecord2.setEntityId(agent2.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord2);

        Place place =
                objectMapper.readValue(
                        IntegrationTestUtils.loadFile(IntegrationTestUtils.PLACE_REFERENTIAL_INTEGRITY_JSON),
                        Place.class);
        EntityRecord entityRecord3 = new EntityRecord();
        entityRecord3.setEntity(place);
        entityRecord3.setEntityId(place.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord3);

        entityBatchService.performReferentialIntegrity(agent1);

        /*
         * Assertions
         */
        // Optional<EntityRecord> agent1_updated =
        // entityRecordService.retrieveEntityRecordByUri(agent1.getEntityId());
        assertEquals(1, agent1.getPlaceOfBirth().size());
        Assertions.assertTrue(
                agent1.getPlaceOfBirth().contains("http://data.europeana.eu/place/base/143914"));
        Assertions.assertNull(agent1.getProfessionOrOccupation());
        List<String> isRelatedTo_agent1 = agent1.getIsRelatedTo();
        assertEquals(3, isRelatedTo_agent1.size());
        Assertions.assertTrue(String.join(",", isRelatedTo_agent1).contains("Leonardo_da_Vinci"));
        Assertions.assertTrue(
                String.join(",", isRelatedTo_agent1)
                        .contains("http://data.europeana.eu/Leonardo_da_Vinci"));
    }

    @Test
    public void performReferentialIntegrity_DaVinci() throws Exception {
        // TODO: implement the following
        // 1. create record for agent-davinci-referential-integrity.json
        // 2. create records for all references entities, xml files available in
        // resources/ref-integrity/references
        // 3. perform referential integrity processing for da vinci record
        // 4. compare (all fields) the updated da vinci entity against the expected result available in
        // agent-davinci-integrity-performed.json

        entityRecordService.dropRepository();
        // create record for agent-davinci-referential-integrity.json
        // TODO: change the input data to use the wikidata URIs instead of semium time
        Agent agentDaVinci =
                objectMapper.readValue(
                        IntegrationTestUtils.loadFile(
                                IntegrationTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON),
                        Agent.class);
        EntityRecord entityRecord1 = new EntityRecord();
        entityRecord1.setEntity(agentDaVinci);
        entityRecord1.setEntityId(agentDaVinci.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord1);

        // create records for all references entities, xml files available in
        // resources/ref-integrity/references
        String metisResponse = IntegrationTestUtils.AGENT_FLORENCE_REFERENTIAL_INTEGRTITY;
        Entity entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord2 = new EntityRecord();
        entityRecord2.setEntity(entityFromMetisResponse);
        entityRecord2.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord2);

        metisResponse = IntegrationTestUtils.AGENT_SALAI_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord3 = new EntityRecord();
        entityRecord3.setEntity(entityFromMetisResponse);
        entityRecord3.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord3);

        metisResponse = IntegrationTestUtils.CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord4 = new EntityRecord();
        entityRecord4.setEntity(entityFromMetisResponse);
        entityRecord4.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord4);

        metisResponse = IntegrationTestUtils.PLACE_AMBOISE_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord5 = new EntityRecord();
        entityRecord5.setEntity(entityFromMetisResponse);
        entityRecord5.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord5);

        metisResponse = IntegrationTestUtils.PLACE_FLORENCE_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord6 = new EntityRecord();
        entityRecord6.setEntity(entityFromMetisResponse);
        entityRecord6.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord6);

        metisResponse = IntegrationTestUtils.PLACE_FRANCE_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord7 = new EntityRecord();
        entityRecord7.setEntity(entityFromMetisResponse);
        entityRecord7.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord7);

        metisResponse = IntegrationTestUtils.PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        EntityRecord entityRecord8 = new EntityRecord();
        entityRecord8.setEntity(entityFromMetisResponse);
        entityRecord8.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord8);

        metisResponse = IntegrationTestUtils.TIMESPAN_15_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        entityFromMetisResponse.getSameReferenceLinks().add(entityFromMetisResponse.getEntityId());
        EntityRecord entityRecord9 = new EntityRecord();
        entityRecord9.setEntity(entityFromMetisResponse);
        entityRecord9.setEntityId("http://data.europeana.eu/timespan/15");
        entityRecordService.saveEntityRecord(entityRecord9);

        metisResponse = IntegrationTestUtils.TIMESPAN_16_REFERENTIAL_INTEGRTITY;
        entityFromMetisResponse = IntegrationTestUtils.getMetisResponse(metisResponse).toEntityModel();
        entityFromMetisResponse.getSameReferenceLinks().add(entityFromMetisResponse.getEntityId());
        EntityRecord entityRecord10 = new EntityRecord();
        entityRecord10.setEntity(entityFromMetisResponse);
        entityRecord10.setEntityId("http://data.europeana.eu/timespan/16");
        entityRecordService.saveEntityRecord(entityRecord10);

        // perform referential integrity processing for da vinci record
        entityBatchService.performReferentialIntegrity(agentDaVinci);

        // compare (all fields) of the updated da vinci entity against the expected result available in
        // given file
        Agent agentDaVinciForChecking =
                objectMapper.readValue(
                        IntegrationTestUtils.loadFile(
                                IntegrationTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON),
                        Agent.class);
        EntityComparator entityComparator = new EntityComparator();
        assertEquals(0, entityComparator.compare(agentDaVinciForChecking, agentDaVinci));
    }

}
