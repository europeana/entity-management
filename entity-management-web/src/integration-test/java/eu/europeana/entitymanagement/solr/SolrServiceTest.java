package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
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
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrTimespan;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.solr.utils.SolrEntityUtils;

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
    public void storeEntitiesInSolr() throws Exception {
    	
    	Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
    	emSolrService.storeEntity(SolrEntityUtils.createSolrEntity(agent), true);
		SolrAgent storedAgent = emSolrService.searchById(SolrAgent.class, agent.getEntityId());
		Assertions.assertNotNull(storedAgent);
    	Assertions.assertEquals(agent.getEntityId(), storedAgent.getEntityId());
 
    	Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
    	emSolrService.storeEntity(SolrEntityUtils.createSolrEntity(organization), true);
     	SolrOrganization storedOrganization = emSolrService.searchById(SolrOrganization.class, organization.getEntityId());
    	Assertions.assertNotNull(storedOrganization);
    	Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());

    	Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
    	emSolrService.storeEntity(SolrEntityUtils.createSolrEntity(timespan), true);
    	SolrTimespan storedTimespan = emSolrService.searchById(SolrTimespan.class, timespan.getEntityId());
    	Assertions.assertNotNull(storedTimespan);
    	Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());

    }
  
  
}