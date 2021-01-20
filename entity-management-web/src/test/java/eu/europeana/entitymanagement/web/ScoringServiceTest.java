package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import eu.europeana.entitymanagement.definitions.model.impl.BaseAgent;
import eu.europeana.entitymanagement.scoring.ScoringService;
import eu.europeana.entitymanagement.scoring.model.EntityMetrics;

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
    public void testMyControllerValidInput() throws Exception {

	BaseAgent agent = new BaseAgent();
	String entityId = "http://data.europeana.eu/agent/base/146741";
	agent.setEntityId(entityId);
	String[] sameAs = new String[] { "http://wikidata.dbpedia.org/resource/Q762",
		"http://www.wikidata.org/entity/Q762", "http://purl.org/collections/nl/am/p-10456" };
	agent.setSameAs(sameAs);
	EntityMetrics metrics = scoringService.computeMetrics(agent);
	
	assertEquals(entityId, metrics.getEntityId());
	assertEquals(1, metrics.getPageRank());
	assertEquals(1, metrics.getEnrichmentCount());
	assertEquals(1, metrics.getHitCount());
	assertEquals(1, metrics.getScore());
	
	
	
    }

}
