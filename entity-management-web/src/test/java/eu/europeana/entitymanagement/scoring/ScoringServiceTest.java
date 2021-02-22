package eu.europeana.entitymanagement.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.MaxEntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;

/**
 * JUnit test for testing the EMControllerTest class
 */
//@SpringBootTest
//@AutoConfigureMockMvc
@ContextConfiguration(classes = { EntityManagementApp.class})
@ExtendWith(SpringExtension.class)
public class ScoringServiceTest {

    @Resource(name=AppConfig.BEAN_EM_SCORING_SERVICE)
    ScoringService scoringService;

    @Test
    public void testComputeMetrics() throws Exception {

	AgentImpl agent = new AgentImpl();
	String entityId = "http://data.europeana.eu/agent/base/146741";
	agent.setEntityId(entityId);
	String[] sameAs = new String[] { "http://wikidata.dbpedia.org/resource/Q762",
		"http://www.wikidata.org/entity/Q762", "http://purl.org/collections/nl/am/p-10456" };
	agent.setSameAs(sameAs);

	Map<String, String> prefLabels = new HashMap<String, String>();
	prefLabels.put("en", "Leonardo da Vinci");
	prefLabels.put("fr", "Léonard de Vinci");
	// not supported language to be filtered out
	prefLabels.put("zh", "cannot read it");
	agent.setPrefLabelStringMap(prefLabels);

	EntityMetrics metrics = scoringService.computeMetrics(agent);

	assertEquals(entityId, metrics.getEntityId());
	assertEquals("Agent", metrics.getEntityType());
//	actual value = 304.6025939567319
	assertTrue(metrics.getPageRank() == 304);
	// value may increase in time, currently  
	//before last reindexing was 750, let's see if the reindexing is complete 
	assertTrue(metrics.getEnrichmentCount() >= 747);
	// value may increase in time, for provided labelts it is currently 2555
	assertTrue(metrics.getHitCount() > 1000);

	assertTrue(metrics.getScore() > 975000);
    }
    
    @Test
    public void testGetMaxMetrics() throws Exception {
	MaxEntityMetrics maxValues = scoringService.getMaxEntityMetrics();
	assertNotNull(maxValues);
	assertNotNull(maxValues.maxValues(EntityTypes.Agent));
	assertNotNull(maxValues.maxValues(EntityTypes.Place));
	assertNotNull(maxValues.maxValues(EntityTypes.Concept));
	assertNotNull(maxValues.maxValues(EntityTypes.Organization));
	assertNotNull(maxValues.maxValues(EntityTypes.Timespan));
    }
    
    @Test
    public void testGetMaxOverallMetrics() throws Exception {
//	MaxEntityMetrics maxValues = scoringService.getMaxEntityMetrics();
	EntityMetrics maxValues = scoringService.getMaxOverallMetrics();
	assertNotNull(maxValues);
	assertEquals(24772, maxValues.getPageRank());
	assertEquals(3065416, maxValues.getEnrichmentCount());
	assertEquals(24576199, maxValues.getHitCount());
    }

    
  
}
