package eu.europeana.entitymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.config.AppAutoconfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.MaxEntityMetrics;
import eu.europeana.entitymanagement.web.service.ScoringService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class ScoringServiceIT extends AbstractIntegrationTest {

  @Resource(name = AppAutoconfig.BEAN_EM_SCORING_SERVICE)
  ScoringService scoringService;

  // @Test
  @Disabled("Excluded from automated, the response mocking is implemented only for timespan")
  public void testComputeMetrics() throws Exception {

    Agent agent = new Agent();
    String entityId = "http://data.europeana.eu/agent/146741";
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
    //  actual value = 304.6025939567319
    assertTrue(metrics.getPageRank() == 304);
    // value may increase in time, currently 807
    assertTrue(metrics.getEnrichmentCount() >= 0);
    // value may increase in time, for provided labels it is currently 2555
    //  assertTrue(metrics.getHitCount() > 1000);

    //    assertTrue(metrics.getScore() > 970000);
  }

  @Test
  @Disabled("Does not work on server because of blocked connection to PR solr")
  public void testComputeMetricsForTimeSpan() throws Exception {

    TimeSpan entity = new TimeSpan();
    String entityId = "http://data.europeana.eu/timespan/21";
    entity.setEntityId(entityId);
    List<String> sameAs = List.of("http://www.wikidata.org/entity/Q6939");
    entity.setSameReferenceLinks(sameAs);

    Map<String, String> prefLabels = new HashMap<String, String>();
    prefLabels.put("en", "21st Century");
    entity.setPrefLabel(prefLabels);

    EntityMetrics metrics = scoringService.computeMetrics(entity);

    assertEquals(entityId, metrics.getEntityId());
    assertEquals("TimeSpan", metrics.getEntityType());
    //  actual value = 575.xxx
    assertTrue(metrics.getPageRank() == 575);
    // value may increase in time, currently 598943
    assertTrue(metrics.getEnrichmentCount() == 175);
    // value may increase in time, currently 1638965
    assertTrue(metrics.getScore() >= 1638965);
  }

  //  @Test
  @Disabled("Excluded from automated, the response mocking is implemented only for timespan")
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
    assertTrue(metrics.getEnrichmentCount() == 0);
    // value may increase in time, for provided labelts it is currently 2555
    //        assertTrue(metrics.getHitCount() > 2000000);

    //    assertTrue(metrics.getScore() > 1085);
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
    //  MaxEntityMetrics maxValues = scoringService.getMaxEntityMetrics();
    EntityMetrics maxValues = scoringService.getMaxOverallMetrics();
    assertNotNull(maxValues);
    assertEquals(24772, maxValues.getPageRank());
    assertEquals(3065416, maxValues.getEnrichmentCount());
    assertEquals(24576199, maxValues.getHitCount());
  }
}
