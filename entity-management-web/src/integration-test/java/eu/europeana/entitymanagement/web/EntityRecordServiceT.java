package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_DATA_RECONCELIATION_XML;
import static eu.europeana.entitymanagement.web.BaseMvcTestUtils.CONCEPT_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
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
        List<EntityProxy> proxies = new ArrayList<EntityProxy>();
        proxies.add(internalProxy);
        proxies.add(externalProxy);
        entityRecord.setProxies(proxies);
        
        entityRecordService.mergeEntity(entityRecord);
        /*
         * here the assertions are manual and are defined based on what is in put in the corresponsing proxy's entity objects
         */
        Assertions.assertNotNull(entityRecord.getEntity().getNote());
        Assertions.assertNotNull(entityRecord.getEntity().getSameAs());
        Assertions.assertTrue(((Concept)entityRecord.getEntity()).getBroader().length>1);
        Assertions.assertNotNull(entityRecord.getEntity().getPrefLabel());        
	}
	


}
