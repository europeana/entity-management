package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT1_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT2_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REFERENTIAL_INTEGRITY_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_DATA_RECONCELIATION_XML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;

@SpringBootTest
public class EntityRecordServiceT {

    @Autowired
    private EntityRecordService entityRecordService;
    
    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

	@Test
	public void mergeEntities() throws JAXBException, JsonMappingException, JsonProcessingException, IOException, EntityCreationException {
		/*
		 * metis deserializer -> get the entity for the external proxy
		 */
		InputStream is = getClass().getResourceAsStream(CONCEPT_DATA_RECONCELIATION_XML);
		JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		EnrichmentResultList resultList = (EnrichmentResultList) unmarshaller.unmarshal(is);

		assertNotNull(resultList);
		assertEquals(1, resultList.getEnrichmentBaseResultWrapperList().size());

		// get unmarshalled object
		XmlConceptImpl xmlEntity = (XmlConceptImpl) resultList.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0);
		
		/*
		 * read the test data from the json file -> get the entity for the internal proxy
		 */
        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_JSON), ConceptImpl.class);
        
        /*
         * creating the entity record
         */
        EntityRecord entityRecord = new EntityRecordImpl();
        EntityProxy internalProxy = new EntityProxyImpl ();
        internalProxy.setEntity(concept);
        internalProxy.setProxyId("http://data.europeana.eu/proxy1");
        EntityProxy externalProxy = new EntityProxyImpl ();
        externalProxy.setEntity(xmlEntity.toEntityModel());
        externalProxy.setProxyId("http://data.external.org/proxy1");
        entityRecord.addProxy(internalProxy);
        entityRecord.addProxy(externalProxy);

        
        entityRecordService.mergeEntity(entityRecord);
        /*
         * here the assertions are manual and are defined based on what is in put in the corresponsing proxy's entity objects
         */
        Assertions.assertNotNull(entityRecord.getEntity().getNote());
        Assertions.assertNotNull(entityRecord.getEntity().getSameAs());
        Assertions.assertTrue(((Concept)entityRecord.getEntity()).getBroader().length>1);
        Assertions.assertNotNull(entityRecord.getEntity().getPrefLabel());        
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

}
