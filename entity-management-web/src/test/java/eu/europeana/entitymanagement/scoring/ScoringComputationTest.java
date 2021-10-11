package eu.europeana.entitymanagement.scoring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.scoring.EntityMetrics;
import eu.europeana.entitymanagement.web.model.scoring.MaxEntityMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** JUnit test for testing the EMControllerTest class */
@SpringBootTest(classes = SerializationConfig.class)
public class ScoringComputationTest {

  /** Use configured ObjectMapper so we know the output from this test matches the real thing */
  @Autowired private ObjectMapper objectMapper;

  @Test
  public void testSerializeMaxMetricsValues() throws Exception {
    MaxEntityMetrics maxMetrics = new MaxEntityMetrics();

    maxMetrics.addMetrics(createMetricsObject(EntityTypes.Agent.name(), 1204, 31734, 2297502));
    maxMetrics.addMetrics(createMetricsObject(EntityTypes.Place.name(), 24772, 3065416, 24576199));
    maxMetrics.addMetrics(createMetricsObject(EntityTypes.Concept.name(), 4055, 1448506, 8106790));
    maxMetrics.addMetrics(createMetricsObject(EntityTypes.Organization.name(), 244, 1, 8977503));
    maxMetrics.addMetrics(createMetricsObject(EntityTypes.Timespan.name(), 3912, 1, 8977503));

    String maxMetricsXml = objectMapper.writeValueAsString(maxMetrics);
    System.out.println(maxMetricsXml);

    assertNotNull(maxMetricsXml);
    assertTrue(maxMetricsXml.contains(EntityTypes.Agent.name()));
    assertTrue(maxMetricsXml.contains(EntityTypes.Place.name()));
    assertTrue(maxMetricsXml.contains(EntityTypes.Concept.name()));
    assertTrue(maxMetricsXml.contains(EntityTypes.Organization.name()));
    assertTrue(maxMetricsXml.contains(EntityTypes.Timespan.name()));
  }

  @Test
  public void testParseMaxMetricsValues() throws Exception {
    String serializedMaxValues =
        "<metrics><maxValues><enrichmentCount>31734</enrichmentCount><hitCount>2297502</hitCount><pageRank>1204</pageRank><entityId>Agent</entityId><entityType>Agent</entityType></maxValues><maxValues><enrichmentCount>3065416</enrichmentCount><hitCount>24576199</hitCount><pageRank>24772</pageRank><entityId>Place</entityId><entityType>Place</entityType></maxValues><maxValues><enrichmentCount>1448506</enrichmentCount><hitCount>8106790</hitCount><pageRank>4055</pageRank><entityId>Concept</entityId><entityType>Concept</entityType></maxValues><maxValues><enrichmentCount>1</enrichmentCount><hitCount>8977503</hitCount><pageRank>244</pageRank><entityId>Organization</entityId><entityType>Organization</entityType></maxValues><maxValues><enrichmentCount>1</enrichmentCount><hitCount>8977503</hitCount><pageRank>3912</pageRank><entityId>Timespan</entityId><entityType>Timespan</entityType></maxValues></metrics>";
    XmlMapper xmlMapper = new XmlMapper();
    MaxEntityMetrics maxMetrics = xmlMapper.readValue(serializedMaxValues, MaxEntityMetrics.class);

    //	MaxEntityMetrics maxMetrics;
    //	XmlMapper xmlMapper = new XmlMapper();
    //	try (InputStream inputStream = getClass().getResourceAsStream("/max-entity-metrics.xml");
    //		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
    //	    String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    //	    maxMetrics = xmlMapper.readValue(contents, MaxEntityMetrics.class);
    //	}

    assertNotNull(maxMetrics);
    assertNotNull(maxMetrics.maxValues(EntityTypes.Agent));
    assertNotNull(maxMetrics.maxValues(EntityTypes.Place));
    assertNotNull(maxMetrics.maxValues(EntityTypes.Concept));
    assertNotNull(maxMetrics.maxValues(EntityTypes.Organization));
    assertNotNull(maxMetrics.maxValues(EntityTypes.Timespan));
  }

  private EntityMetrics createMetricsObject(
      String type, int pageRank, int enrichmentCount, int hitCount) {
    EntityMetrics metrics = new EntityMetrics(type);
    metrics.setEntityType(type);
    metrics.setPageRank(pageRank);
    metrics.setEnrichmentCount(enrichmentCount);
    metrics.setHitCount(hitCount);

    return metrics;
  }
}
