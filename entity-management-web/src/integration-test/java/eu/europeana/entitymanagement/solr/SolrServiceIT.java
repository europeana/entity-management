package eu.europeana.entitymanagement.solr;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.solr.model.*;
import eu.europeana.entitymanagement.solr.service.SolrService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;

@SpringBootTest
public class SolrServiceIT extends AbstractIntegrationTest {

	@Qualifier(AppConfig.BEAN_EM_SOLR_SERVICE)
	@Autowired
	private SolrService emSolrService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;


	@BeforeEach
	void setUp() throws Exception {
		emSolrService.deleteAllDocuments();
	}

	@Test
    public void storeAgentInSolr() throws Exception {
    	
    	Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(agent));
		SolrAgent storedAgent = emSolrService.searchById(SolrAgent.class, agent.getEntityId());
		Assertions.assertNotNull(storedAgent);
    	Assertions.assertEquals(agent.getEntityId(), storedAgent.getEntityId());

    }
    
    @Test
    public void storeOrganizationInSolr() throws Exception {
 
    	Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(organization));
     	SolrOrganization storedOrganization = emSolrService.searchById(SolrOrganization.class, organization.getEntityId());
    	Assertions.assertNotNull(storedOrganization);
    	Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());
    }
    
    @Test
    public void storeTimespanInSolr() throws Exception {
    	
    	Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(timespan));
    	SolrTimespan storedTimespan = emSolrService.searchById(SolrTimespan.class, timespan.getEntityId());
    	Assertions.assertNotNull(storedTimespan);
    	Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());

    }
    
    @Test
    public void storeConceptInSolr() throws Exception {
    	
    	Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(concept));
    	SolrConcept storedConcept = emSolrService.searchById(SolrConcept.class, concept.getEntityId());
    	Assertions.assertNotNull(storedConcept);
    	Assertions.assertEquals(concept.getEntityId(), storedConcept.getEntityId());

    }
    
    @Test
    public void storePlaceInSolr() throws Exception {
    	Place place = objectMapper.readValue(loadFile(PLACE_JSON), Place.class);
    	emSolrService.storeEntity(SolrUtils.createSolrEntity(place));
    	SolrPlace storedPlace = emSolrService.searchById(SolrPlace.class, place.getEntityId());
    	Assertions.assertNotNull(storedPlace);
    	Assertions.assertEquals(place.getEntityId(), storedPlace.getEntityId());
    }
}