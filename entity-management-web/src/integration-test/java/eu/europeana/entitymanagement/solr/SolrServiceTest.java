package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimespan;
import eu.europeana.entitymanagement.solr.model.SolrUtils;
import eu.europeana.entitymanagement.solr.service.SolrService;

/**
 * JUnit test for testing the EMControllerTest class
 */
//@ContextConfiguration(classes = { EntityManagementApp.class})
@SpringBootTest
public class SolrServiceTest {

    @Resource(name=AppConfig.BEAN_EM_SOLR_SERVICE)
	SolrService emSolrService;
    
    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    JsonLdSerializer emJsonldSerializer;

    //@Test
    public void storeAgentInSolr() throws Exception {
    	
    	Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(agent), true);
		SolrAgent storedAgent = emSolrService.searchById(SolrAgent.class, agent.getEntityId());
		Assertions.assertNotNull(storedAgent);
    	Assertions.assertEquals(agent.getEntityId(), storedAgent.getEntityId());

    }
    
    //@Test
    public void storeOranizationInSolr() throws Exception {
 
    	Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(organization), true);
     	SolrOrganization storedOrganization = emSolrService.searchById(SolrOrganization.class, organization.getEntityId());
    	Assertions.assertNotNull(storedOrganization);
    	Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());
    }
    
    //@Test
    public void storeTimespanInSolr() throws Exception {
    	
    	Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(timespan), true);
    	SolrTimespan storedTimespan = emSolrService.searchById(SolrTimespan.class, timespan.getEntityId());
    	Assertions.assertNotNull(storedTimespan);
    	Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());

    }
    
    //@Test
    public void storeConceptInSolr() throws Exception {
    	
    	Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(concept), true);
    	SolrConcept storedConcept = emSolrService.searchById(SolrConcept.class, concept.getEntityId());
    	Assertions.assertNotNull(storedConcept);
    	Assertions.assertEquals(concept.getEntityId(), storedConcept.getEntityId());

    }
    
    //@Test
    public void storePlaceInSolr() throws Exception {
    	
    	Place place = objectMapper.readValue(loadFile(PLACE_JSON), Place.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(place), true);
    	SolrPlace storedPlace = emSolrService.searchById(SolrPlace.class, place.getEntityId());
    	Assertions.assertNotNull(storedPlace);
    	Assertions.assertEquals(place.getEntityId(), storedPlace.getEntityId());

    }
  
  
}