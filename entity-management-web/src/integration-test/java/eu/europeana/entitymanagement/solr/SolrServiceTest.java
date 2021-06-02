package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.web.EMController;

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

    @Test
    public void storeEntitiesInSolr() throws Exception {
    	
    	Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
    	emSolrService.storeEntity(agent, true);
    	Entity storedAgent = emSolrService.searchById(agent.getType(), agent.getEntityId());
    	Assertions.assertNotNull(storedAgent);
    	Assertions.assertEquals(agent.getEntityId(), storedAgent.getEntityId());
 
    	Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
    	emSolrService.storeEntity(organization, true);
    	Entity storedOrganization = emSolrService.searchById(organization.getType(), organization.getEntityId());
    	Assertions.assertNotNull(storedOrganization);
    	Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());

    	Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
    	emSolrService.storeEntity(timespan, true);
    	Entity storedTimespan = emSolrService.searchById(timespan.getType(), timespan.getEntityId());
    	Assertions.assertNotNull(storedTimespan);
    	Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());    	
	
    }
  
  
}