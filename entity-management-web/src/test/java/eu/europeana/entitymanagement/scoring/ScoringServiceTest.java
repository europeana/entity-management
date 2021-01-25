package eu.europeana.entitymanagement.scoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import eu.europeana.entitymanagement.definitions.model.impl.BaseAgent;
import eu.europeana.entitymanagement.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.scoring.ScoringService;
import eu.europeana.entitymanagement.scoring.model.EntityMetrics;
import eu.europeana.entitymanagement.scoring.model.MaxEntityMetrics;

/**
 * JUnit test for testing the EMControllerTest class
 */
@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest
//@MockBean()
public class ScoringServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ScoringService scoringService;

    @Test
    public void testComputeMetrics() throws Exception {

	BaseAgent agent = new BaseAgent();
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
	// value may increase in time, currently 750
	assertTrue(metrics.getEnrichmentCount() >= 750);
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
