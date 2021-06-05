package eu.europeana.entitymanagement.solr;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.solr.utils.SolrEntityUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

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

    	//TODO: adapt and uncomment
 
//    	Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
//    	emSolrService.storeEntity(organization, true);
//    	Entity storedOrganization = emSolrService.searchById(organization.getType(), organization.getEntityId());
//    	Assertions.assertNotNull(storedOrganization);
//    	Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());
//
//    	Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
//    	emSolrService.storeEntity(timespan, true);
//    	Entity storedTimespan = emSolrService.searchById(timespan.getType(), timespan.getEntityId());
//    	Assertions.assertNotNull(storedTimespan);
//    	Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());
//
    }
  
  
}