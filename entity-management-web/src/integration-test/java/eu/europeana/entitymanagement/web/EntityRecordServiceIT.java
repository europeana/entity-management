package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT1_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT2_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_FLORENCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_SALAI_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_AMBOISE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_FLORENCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_FRANCE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_15_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_16_REFERENTIAL_INTEGRTITY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_BATHTUB;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_CONSOLIDATED_BATHTUB;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_DATA_RECONCELIATION_XML;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_METIS_BATHTUB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.utils.EntityComparator;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;

@SpringBootTest
public class EntityRecordServiceIT extends AbstractIntegrationTest{

    @Autowired
    private EntityRecordService entityRecordService;
    
    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		entityRecordService.dropRepository();
	}

	@Test
	public void mergeEntities() throws JAXBException, JsonMappingException, JsonProcessingException, IOException,
		EntityCreationException {
	    /*
	     * metis deserializer -> get the entity for the external proxy
	     */
	    String metisResponse = CONCEPT_DATA_RECONCELIATION_XML;
	    XmlConceptImpl xmlEntity = (XmlConceptImpl) getMetisResponse(metisResponse);

	    /*
	     * read the test data from the json file -> get the entity for the internal
	     * proxy
	     */
	    ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);

	    /*
	     * creating the entity record
	     */
	    EntityRecord entityRecord = new EntityRecordImpl();
	    EntityProxy internalProxy = new EntityProxyImpl();
	    internalProxy.setEntity(concept);
	    internalProxy.setProxyId("http://data.europeana.eu/proxy1");
	    EntityProxy externalProxy = new EntityProxyImpl();
	    externalProxy.setEntity(xmlEntity.toEntityModel());
	    externalProxy.setProxyId("http://data.external.org/proxy1");
	    entityRecord.addProxy(internalProxy);
	    entityRecord.addProxy(externalProxy);

	    entityRecordService.mergeEntity(entityRecord);
	    /*
	     * here the assertions are manual and are defined based on what is in put in the
	     * corresponsing proxy's entity objects
	     */
	    Assertions.assertNotNull(entityRecord.getEntity().getNote());
	    Assertions.assertNotNull(entityRecord.getEntity().getSameAs());
	    Assertions.assertTrue(((Concept) entityRecord.getEntity()).getBroader().length > 1);
	    Assertions.assertNotNull(entityRecord.getEntity().getPrefLabel());
	}

	
	@Test
	public void mergeEntitiesBathtub() throws JAXBException, JsonMappingException, JsonProcessingException, IOException,
		EntityCreationException {
	    /*
	     * metis deserializer -> get the entity for the external proxy
	     */
	    String metisResponse = CONCEPT_METIS_BATHTUB;
	    XmlConceptImpl xmlEntity = (XmlConceptImpl) getMetisResponse(metisResponse);

	    /*
	     * read the test data from the json file -> get the entity for the internal
	     * proxy
	     */
	    ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_BATHTUB), ConceptImpl.class);

	    /*
	     * creating the entity record
	     */
	    EntityRecord entityRecord = new EntityRecordImpl();
	    EntityProxy internalProxy = new EntityProxyImpl();
	    internalProxy.setEntity(concept);
	    internalProxy.setProxyId("http://data.europeana.eu/concept/1#proxy_europeana");
	    EntityProxy externalProxy = new EntityProxyImpl();
	    externalProxy.setEntity(xmlEntity.toEntityModel());
	    externalProxy.setProxyId("http://www.wikidata.org/entity/Q1101933");
	    entityRecord.addProxy(internalProxy);
	    entityRecord.addProxy(externalProxy);

	    entityRecordService.mergeEntity(entityRecord);
	    /*
	     * here the assertions are manual and are defined based on what is in put in the
	     * corresponsing proxy's entity objects
	     */
	    ConceptImpl concept_consolidated = objectMapper.readValue(loadFile(CONCEPT_CONSOLIDATED_BATHTUB), ConceptImpl.class);
	    //TODO: temporary fix untill the merge entities is stable see EntityRecordService.UPDARTE_FIELDS_TO_IGNORE
	    concept_consolidated.setType(entityRecord.getEntity().getType());
	    //reuse the isAggregatedBy field 
	    concept_consolidated.setIsAggregatedBy(entityRecord.getEntity().getIsAggregatedBy());
	    
	    
	    EntityComparator entityComparator = new EntityComparator();
	    Assertions.assertTrue(entityComparator.compare(concept_consolidated, entityRecord.getEntity())==0);    
	    
	}
	
	private XmlBaseEntityImpl getMetisResponse(String metisResponse) throws JAXBException {
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
	public void performGlobalReferentialIntegrity () throws JsonMappingException, JsonProcessingException, IOException {
        entityRecordService.dropRepository();
		// read the test data from the file
        AgentImpl agent1 = objectMapper.readValue(loadFile(AGENT1_REFERENTIAL_INTEGRITY_JSON), AgentImpl.class);
        EntityRecord entityRecord1 = new EntityRecordImpl();
        entityRecord1.setEntity(agent1);
        entityRecord1.setEntityId(agent1.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord1);

        AgentImpl agent2 = objectMapper.readValue(loadFile(AGENT2_REFERENTIAL_INTEGRITY_JSON), AgentImpl.class);
        EntityRecord entityRecord2 = new EntityRecordImpl();
        entityRecord2.setEntity(agent2);
        entityRecord2.setEntityId(agent2.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord2);

        PlaceImpl place = objectMapper.readValue(loadFile(PLACE_REFERENTIAL_INTEGRITY_JSON), PlaceImpl.class);
        EntityRecord entityRecord3 = new EntityRecordImpl();
        entityRecord3.setEntity(place);
        entityRecord3.setEntityId(place.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord3);

        entityRecordService.performReferentialIntegrity(agent1);

        /*
         * Assertions
         */
        //Optional<EntityRecord> agent1_updated = entityRecordService.retrieveEntityRecordByUri(agent1.getEntityId());
        Assertions.assertTrue(agent1.getPlaceOfBirth().size()==1);
        Assertions.assertTrue(agent1.getPlaceOfBirth().get("").contains("http://data.europeana.eu/place/base/143914"));
        Assertions.assertNull(agent1.getProfessionOrOccupation());
        String[] isRelatedTo_agent1 = agent1.getIsRelatedTo();
        Assertions.assertTrue(isRelatedTo_agent1.length==3);
        Assertions.assertTrue(String.join(",", isRelatedTo_agent1).contains("Leonardo_da_Vinci"));
        Assertions.assertTrue(String.join(",", isRelatedTo_agent1).contains("http://data.europeana.eu/Leonardo_da_Vinci"));
	}

	@Test
	public void performReferentialIntegrity_DaVinci () throws JsonMappingException, JsonProcessingException, IOException, JAXBException, EntityCreationException {
	    //TODO: implement the following
	    //1. create record for agent-davinci-referential-integrity.json
	    //2. create records for all references entities, xml files available in resources/ref-integrity/references
	    //3. perform referential integrity processing for da vinci record
	    //4. compare (all fields) the updated da vinci entity against the expected result available in agent-davinci-integrity-performed.json
        
		entityRecordService.dropRepository();
		// create record for agent-davinci-referential-integrity.json
        AgentImpl agentDaVinci = objectMapper.readValue(loadFile(AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON), AgentImpl.class);
        EntityRecord entityRecord1 = new EntityRecordImpl();
        entityRecord1.setEntity(agentDaVinci);
        entityRecord1.setEntityId(agentDaVinci.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord1);

        // create records for all references entities, xml files available in resources/ref-integrity/references
	    String metisResponse = AGENT_FLORENCE_REFERENTIAL_INTEGRTITY;
	    Entity entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord2 = new EntityRecordImpl();
	    entityRecord2.setEntity(entityFromMetisResponse);
        entityRecord2.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord2);
        
	    metisResponse = AGENT_SALAI_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord3 = new EntityRecordImpl();
        entityRecord3.setEntity(entityFromMetisResponse);
        entityRecord3.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord3);
        
	    metisResponse = CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord4 = new EntityRecordImpl();
        entityRecord4.setEntity(entityFromMetisResponse);
        entityRecord4.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord4);

	    metisResponse = PLACE_AMBOISE_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord5 = new EntityRecordImpl();
        entityRecord5.setEntity(entityFromMetisResponse);
        entityRecord5.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord5);

	    metisResponse = PLACE_FLORENCE_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord6 = new EntityRecordImpl();
        entityRecord6.setEntity(entityFromMetisResponse);
        entityRecord6.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord6);

	    metisResponse = PLACE_FRANCE_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord7 = new EntityRecordImpl();
        entityRecord7.setEntity(entityFromMetisResponse);
        entityRecord7.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord7);

	    metisResponse = PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord8 = new EntityRecordImpl();
        entityRecord8.setEntity(entityFromMetisResponse);
        entityRecord8.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord8);

	    metisResponse = TIMESPAN_15_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord9 = new EntityRecordImpl();
        entityRecord9.setEntity(entityFromMetisResponse);
        entityRecord9.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord9);
        
	    metisResponse = TIMESPAN_16_REFERENTIAL_INTEGRTITY;
	    entityFromMetisResponse = getMetisResponse(metisResponse).toEntityModel();
	    EntityRecord entityRecord10 = new EntityRecordImpl();
        entityRecord10.setEntity(entityFromMetisResponse);
        entityRecord10.setEntityId(entityFromMetisResponse.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord10);

        // perform referential integrity processing for da vinci record
        entityRecordService.performReferentialIntegrity(agentDaVinci);
        
        //compare (all fields) of the updated da vinci entity against the expected result available in given file
        AgentImpl agentDaVinciForChecking = objectMapper.readValue(loadFile(AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON), AgentImpl.class);
	    EntityComparator entityComparator = new EntityComparator();
	    Assertions.assertTrue(entityComparator.compare(agentDaVinciForChecking, agentDaVinci)==0);
	}
	       
}
