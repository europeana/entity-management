package eu.europeana.entitymanagement.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.SolrConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.MaxEntityMetrics;
import eu.europeana.entitymanagement.web.service.EnrichmentCountQueryService;
import eu.europeana.entitymanagement.web.service.ScoringService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Integration test for testing the ScoringService */
// TODO: create a "proper" integration test with this
@SpringBootTest(
    classes = {
      ValidatorConfig.class,
      SerializationConfig.class,
      EntityManagementConfiguration.class,
      ScoringService.class,
      SolrConfig.class,
      EnrichmentCountQueryService.class
    })
public class ScoringServiceTest {

  @Resource(name = AppConfig.BEAN_EM_SCORING_SERVICE)
  ScoringService scoringService;

  @Test
  @Disabled("Excluded from automated runs as this requires Search API")
  public void testComputeMetrics() throws Exception {

    Agent agent = new Agent();
    String entityId = "http://data.europeana.eu/agent/base/146741";
    agent.setEntityId(entityId);
    List<String> sameAs =
        List.of(
            "http://wikidata.dbpedia.org/resource/Q762",
            "http://www.wikidata.org/entity/Q762",
            "http://purl.org/collections/nl/am/p-10456");
    agent.setSameReferenceLinks(sameAs);

    Map<String, String> prefLabels = new HashMap<String, String>();
    prefLabels.put("en", "Leonardo da Vinci");
    prefLabels.put("fr", "Léonard de Vinci");
    // not supported language to be filtered out
    prefLabels.put("zh", "cannot read it");
    agent.setPrefLabel(prefLabels);

    EntityMetrics metrics = scoringService.computeMetrics(agent);

    assertEquals(entityId, metrics.getEntityId());
    assertEquals("Agent", metrics.getEntityType());
    //	actual value = 304.6025939567319
    assertTrue(metrics.getPageRank() == 304);
    // value may increase in time, currently
    // before last reindexing was 750, let's see if the reindexing is complete
    assertTrue(metrics.getEnrichmentCount() >= 747);
    // value may increase in time, for provided labelts it is currently 2555
    //	assertTrue(metrics.getHitCount() > 1000);

    assertTrue(metrics.getScore() > 975000);
  }

  @Test
  @Disabled("Excluded from automated runs as this requires Search API")
  public void testComputeMetricsForPlaces() throws Exception {

    Place agent = new Place();
    String entityId = "http://data.europeana.eu/place/base/41488";
    agent.setEntityId(entityId);
    List<String> sameAs = List.of("https://sws.geonames.org/2988507/");
    agent.setSameReferenceLinks(sameAs);

    Map<String, String> prefLabels = new HashMap<String, String>();
    prefLabels.put("en", "Paris");
    prefLabels.put("it", "Parigi");
    // not supported language to be filtered out
    prefLabels.put("ru", "Паріж");
    agent.setPrefLabel(prefLabels);

    EntityMetrics metrics = scoringService.computeMetrics(agent);

    assertEquals(entityId, metrics.getEntityId());
    assertEquals("Place", metrics.getEntityType());
    //      actual value = 304.6025939567319
    assertTrue(metrics.getPageRank() == 0);
    // value may increase in time, currently
    // before last reindexing was 750, let's see if the reindexing is complete
    assertTrue(metrics.getEnrichmentCount() >= 52000);
    // value may increase in time, for provided labelts it is currently 2555
    //        assertTrue(metrics.getHitCount() > 2000000);

    assertTrue(metrics.getScore() > 1085);
  }

  @Test
  public void testGetMaxMetrics() throws Exception {
    MaxEntityMetrics maxValues = scoringService.getMaxEntityMetrics();
    assertNotNull(maxValues);
    assertNotNull(maxValues.maxValues(EntityTypes.Agent));
    assertNotNull(maxValues.maxValues(EntityTypes.Place));
    assertNotNull(maxValues.maxValues(EntityTypes.Concept));
    assertNotNull(maxValues.maxValues(EntityTypes.Organization));
    assertNotNull(maxValues.maxValues(EntityTypes.TimeSpan));
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
