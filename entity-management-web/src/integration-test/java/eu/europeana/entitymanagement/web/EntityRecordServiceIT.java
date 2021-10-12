package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT1_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT2_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_FLORENCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_DAVINCI_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_SALAI_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_CONSOLIDATED_BATHTUB;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_DATA_RECONCELIATION_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_AMBOISE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_FLORENCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_FRANCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_15_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_16_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.corelib.edm.model.schemaorg.SchemaOrgConstants;
import eu.europeana.corelib.edm.model.schemaorg.Text;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.schemaorg.model.SchemaOrgEntity;
import eu.europeana.entitymanagement.serialization.EMTextSerializer;
import eu.europeana.entitymanagement.utils.EntityComparator;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.xml.model.XmlAgentImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityRecordServiceIT extends AbstractIntegrationTest {

  @Autowired private EntityRecordService entityRecordService;

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    entityRecordService.dropRepository();
  }

  @Test
  public void mergeEntities() throws Exception {
    /*
     * metis deserializer -> get the entity for the external proxy
     */
    XmlConceptImpl xmlEntity = (XmlConceptImpl) getMetisResponse(CONCEPT_DATA_RECONCELIATION_XML);
    Concept externalEntity = xmlEntity.toEntityModel();

    /*
     * read the test data from the json file -> get the entity for the internal
     * proxy
     */
    Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);

    // aggregation is reused from consolidated version
    Concept notConsolidated =
        EntityObjectFactory.createProxyEntityObject(EntityTypes.Concept.getEntityType());
    //	    notConsolidated.setIsAggregatedBy(new Aggregation());
    //	    entityRecord.setEntity(notConsolidated);

    Concept mergedEntity = (Concept) entityRecordService.mergeEntities(concept, externalEntity);
    /*
     * here the assertions are manual and are defined based on what is in put in the
     * corresponsing proxy's entity objects
     */
    Assertions.assertNotNull(mergedEntity.getNote());
    Assertions.assertNotNull(mergedEntity.getSameReferenceLinks());
    Assertions.assertTrue(mergedEntity.getBroader().size() > 1);
    Assertions.assertNotNull(mergedEntity.getPrefLabel());
  }

  @Test
  public void mergeEntitiesBathtub() throws Exception {
    /*
     * metis deserializer -> get the entity for the external proxy
     */
    XmlConceptImpl xmlEntity = (XmlConceptImpl) getMetisResponse(CONCEPT_BATHTUB_XML);

    /*
     * read the test data from the json file -> get the entity for the internal
     * proxy
     */
    Concept concept =
        objectMapper.readValue(loadFile(CONCEPT_REGISTER_BATHTUB_JSON), Concept.class);
    Concept externalEntity = xmlEntity.toEntityModel();

    Concept mergedEntity = (Concept) entityRecordService.mergeEntities(concept, externalEntity);
    /*
     * here the assertions are manual and are defined based on what is in put in the
     * corresponsing proxy's entity objects
     */
    Concept concept_consolidated =
        objectMapper.readValue(loadFile(CONCEPT_CONSOLIDATED_BATHTUB), Concept.class);
    // TODO: temporary fix untill the merge entities is stable see
    // EntityRecordService.UPDARTE_FIELDS_TO_IGNORE
    // reuse the isAggregatedBy field
    //	    concept_consolidated.setIsAggregatedBy(entityRecord.getEntity().getIsAggregatedBy());

    EntityComparator entityComparator = new EntityComparator();
    assertEquals(0, entityComparator.compare(concept_consolidated, mergedEntity));
  }

  /*
   * testing some properties of the schemaorg entity (e.g. the jobTitle of the Agent entity)
   */
  // @Test
  public void schemaorgTestProperties() throws Exception {
    XmlAgentImpl xmlEntity = (XmlAgentImpl) getMetisResponse(AGENT_DA_VINCI_XML);
    Agent agentExternalEntity = xmlEntity.toEntityModel();
    Agent agentInternalEntity =
        objectMapper.readValue(loadFile(AGENT_REGISTER_DAVINCI_JSON), Agent.class);
    Agent mergedAgentEntity =
        (Agent) entityRecordService.mergeEntities(agentInternalEntity, agentExternalEntity);

    SchemaOrgEntity<?> schemaOrgAgent =
        EntityObjectFactory.createSchemaOrgEntity(mergedAgentEntity);

    ObjectMapper mapperSchemaorg = objectMapper.copy();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Text.class, new EMTextSerializer(Text.class));
    mapperSchemaorg.registerModule(module);

    JsonNode schemaOrgAgentJson = mapperSchemaorg.valueToTree(schemaOrgAgent.get());

    /*
     * testing the jobTitle field of the schemaorg entity
     */
    if (agentExternalEntity.getProfessionOrOccupation() != null
        && agentExternalEntity.getProfessionOrOccupation().size() > 0) {
      JsonNode jobTitleNode = schemaOrgAgentJson.findValue(SchemaOrgConstants.PROPERTY_JOB_TITLE);
      assertTrue(jobTitleNode.isArray());
      assertTrue(
          agentExternalEntity.getProfessionOrOccupation().contains(jobTitleNode.get(0).asText()));
    }
  }

  private XmlBaseEntityImpl<?> getMetisResponse(String metisResponse) throws JAXBException {
    InputStream is = getClass().getResourceAsStream(metisResponse);
    JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    EnrichmentResultList resultList = (EnrichmentResultList) unmarshaller.unmarshal(is);

    assertNotNull(resultList);
    assertEquals(1, resultList.getEnrichmentBaseResultWrapperList().size());

    // get unmarshalled object
    return resultList.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0);
  }

  @Test
  public void performGlobalReferentialIntegrity() throws Exception {
    entityRecordService.dropRepository();
    // read the test data from the file
    Agent agent1 = objectMapper.readValue(loadFile(AGENT1_REFERENTIAL_INTEGRITY_JSON), Agent.class);
    EntityRecord entityRecord1 = new EntityRecord();
    entityRecord1.setEntity(agent1);
    entityRecord1.setEntityId(agent1.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord1);

    Agent agent2 = objectMapper.readValue(loadFile(AGENT2_REFERENTIAL_INTEGRITY_JSON), Agent.class);
    EntityRecord entityRecord2 = new EntityRecord();
    entityRecord2.setEntity(agent2);
    entityRecord2.setEntityId(agent2.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord2);

    Place place = objectMapper.readValue(loadFile(PLACE_REFERENTIAL_INTEGRITY_JSON), Place.class);
    EntityRecord entityRecord3 = new EntityRecord();
    entityRecord3.setEntity(place);
    entityRecord3.setEntityId(place.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord3);

    entityRecordService.performReferentialIntegrity(agent1);

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
        objectMapper.readValue(loadFile(AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON), Agent.class);
    EntityRecord entityRecord1 = new EntityRecord();
    entityRecord1.setEntity(agentDaVinci);
    entityRecord1.setEntityId(agentDaVinci.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord1);

    // create records for all references entities, xml files available in
    // resources/ref-integrity/references
    String metisResponse = AGENT_FLORENCE_REFERENTIAL_INTEGRTITY;
    Entity entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord2 = new EntityRecord();
    entityRecord2.setEntity(entityFromMetisResponse);
    entityRecord2.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord2);

    metisResponse = AGENT_SALAI_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord3 = new EntityRecord();
    entityRecord3.setEntity(entityFromMetisResponse);
    entityRecord3.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord3);

    metisResponse = CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord4 = new EntityRecord();
    entityRecord4.setEntity(entityFromMetisResponse);
    entityRecord4.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord4);

    metisResponse = PLACE_AMBOISE_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord5 = new EntityRecord();
    entityRecord5.setEntity(entityFromMetisResponse);
    entityRecord5.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord5);

    metisResponse = PLACE_FLORENCE_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord6 = new EntityRecord();
    entityRecord6.setEntity(entityFromMetisResponse);
    entityRecord6.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord6);

    metisResponse = PLACE_FRANCE_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord7 = new EntityRecord();
    entityRecord7.setEntity(entityFromMetisResponse);
    entityRecord7.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord7);

    metisResponse = PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    EntityRecord entityRecord8 = new EntityRecord();
    entityRecord8.setEntity(entityFromMetisResponse);
    entityRecord8.setEntityId(entityFromMetisResponse.getEntityId());
    entityRecordService.saveEntityRecord(entityRecord8);

    metisResponse = TIMESPAN_15_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    entityFromMetisResponse.getSameReferenceLinks().add(entityFromMetisResponse.getEntityId());
    EntityRecord entityRecord9 = new EntityRecord();
    entityRecord9.setEntity(entityFromMetisResponse);
    entityRecord9.setEntityId("http://data.europeana.eu/timespan/15");
    entityRecordService.saveEntityRecord(entityRecord9);

    metisResponse = TIMESPAN_16_REFERENTIAL_INTEGRTITY;
    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
    entityFromMetisResponse.getSameReferenceLinks().add(entityFromMetisResponse.getEntityId());
    EntityRecord entityRecord10 = new EntityRecord();
    entityRecord10.setEntity(entityFromMetisResponse);
    entityRecord10.setEntityId("http://data.europeana.eu/timespan/16");
    entityRecordService.saveEntityRecord(entityRecord10);

    // perform referential integrity processing for da vinci record
    entityRecordService.performReferentialIntegrity(agentDaVinci);

    // compare (all fields) of the updated da vinci entity against the expected result available in
    // given file
    Agent agentDaVinciForChecking =
        objectMapper.readValue(
            loadFile(AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON), Agent.class);
    EntityComparator entityComparator = new EntityComparator();
    assertEquals(0, entityComparator.compare(agentDaVinciForChecking, agentDaVinci));
  }
}
