package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.SUGGEST_FILTERS;
import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.SUGGEST_FILTER_EUROPEANA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimeSpan;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SolrServiceIT extends AbstractIntegrationTest {

  //  private static final String RIGHTS_PD = "https://creativecommons.org/publicdomain/zero/1.0/";

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
    EntityRecord record = buildAgentRecord();
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrAgent storedAgent =
        emSolrService.searchById(SolrAgent.class, record.getEntity().getEntityId());
    Assertions.assertNotNull(storedAgent);
    verifyPayload(storedAgent);
    verifyIsShownBy(storedAgent.getPayload());
    Assertions.assertEquals(record.getEntity().getEntityId(), storedAgent.getEntityId());
  }

  void verifyPayload(SolrEntity<?> entity) {
    String payload = entity.getPayload();
    Assertions.assertNotNull(payload);
    // mandatory fields
    assertThat(payload, Matchers.containsString("\"prefLabel\""));
    assertThat(payload, Matchers.containsString("\"type\""));

    // for organizations verify country
    if (EntityTypes.Organization.toString().equals(entity.getType())) {
      assertThat(payload, Matchers.containsString("\"country\""));
      assertThat(payload, Matchers.containsString("\"organizationDomain\""));
    }
  }

  void verifyIsShownBy(String payload) {
    // verify isShownBy
    assertThat(payload, Matchers.containsString("\"isShownBy\""));
    assertThat(payload, Matchers.containsString("\"source\""));
    assertThat(payload, Matchers.containsString("\"thumbnail\""));
    // id and isShownBy.id
    assertEquals(2, StringUtils.countMatches(payload, "\"id\""));
  }

  EntityRecord buildAgentRecord()
      throws JsonProcessingException, JsonMappingException, IOException {
    Agent agent =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.AGENT_JSON), Agent.class);
    // metrics are set in isAggregatedBy
    Aggregation isAggregatedBy = new Aggregation();
    isAggregatedBy.setPageRank(304d);
    isAggregatedBy.setRecordCount(705);
    isAggregatedBy.setScore(970000);

    agent.setIsAggregatedBy(isAggregatedBy);

    EntityRecord record = new EntityRecord();
    record.setEntity(agent);
    record.setEntityId(agent.getEntityId());
    EntityProxy europeanaProxy = new EntityProxy();
    europeanaProxy.setProxyId(EntityRecordUtils.getEuropeanaProxyId(record.getEntityId()));
    europeanaProxy.setEntity(agent);
    Aggregation proxyIn = new Aggregation();
    europeanaProxy.setProxyIn(proxyIn);
    record.addProxy(europeanaProxy);
    return record;
  }

  @Test
  public void searchAgentInEuropeana() throws Exception {
    EntityRecord record = buildAgentRecord();
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    String searchQuery =
        SUGGEST_FILTERS
            + ":"
            + SUGGEST_FILTER_EUROPEANA
            + " AND "
            + SUGGEST_FILTERS
            + ":"
            + record.getEntity().getType();

    List<SolrEntity<?>> agents = getSolrEntities(searchQuery);
    Assertions.assertNotNull(agents);
    Assertions.assertNotNull(agents.get(0));
    Assertions.assertNotNull(agents.get(0).getPayload());
    verifyPayload(agents.get(0));
    verifyIsShownBy(agents.get(0).getPayload());

    Assertions.assertEquals(record.getEntity().getEntityId(), agents.get(0).getEntityId());
  }

  @Test
  public void storeOrganizationInSolr() throws Exception {
    Organization organization =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.ORGANIZATION_JSON),
            Organization.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(organization);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrOrganization storedOrganization =
        emSolrService.searchById(SolrOrganization.class, organization.getEntityId());
    Assertions.assertNotNull(storedOrganization);
    verifyPayload(storedOrganization);
    Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());
  }

  @Test
  public void storeTimespanInSolr() throws Exception {

    TimeSpan timespan =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.TIMESPAN_JSON), TimeSpan.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(timespan);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrTimeSpan storedTimespan =
        emSolrService.searchById(SolrTimeSpan.class, timespan.getEntityId());
    Assertions.assertNotNull(storedTimespan);
    verifyPayload(storedTimespan);
    Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());
  }

  @Test
  public void storeConceptInSolr() throws Exception {

    Concept concept =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.CONCEPT_JSON), Concept.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(concept);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrConcept storedConcept = emSolrService.searchById(SolrConcept.class, concept.getEntityId());
    Assertions.assertNotNull(storedConcept);
    verifyPayload(storedConcept);
    Assertions.assertEquals(concept.getEntityId(), storedConcept.getEntityId());
  }

  @Test
  public void storePlaceInSolr() throws Exception {
    Place place =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.PLACE_JSON), Place.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(place);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrPlace storedPlace = emSolrService.searchById(SolrPlace.class, place.getEntityId());
    Assertions.assertNotNull(storedPlace);
    verifyPayload(storedPlace);
    Assertions.assertEquals(place.getEntityId(), storedPlace.getEntityId());
  }

  @Test
  void shouldSearchByQuery() throws Exception {
    EntityRecord agentRecord = buildAgentRecord();
    Place place =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.PLACE_JSON), Place.class);
    EntityRecord placeRecord = new EntityRecord();
    placeRecord.setEntity(place);
    Concept concept =
        objectMapper.readValue(
            IntegrationTestUtils.loadFile(IntegrationTestUtils.CONCEPT_JSON), Concept.class);
    EntityRecord conceptRecord = new EntityRecord();
    conceptRecord.setEntity(concept);

    List<SolrEntity<? extends Entity>> solrEntities =
        List.of(agentRecord, placeRecord, conceptRecord).stream()
            .map(SolrUtils::createSolrEntity)
            .collect(Collectors.toList());

    emSolrService.storeMultipleEntities(solrEntities);

    List<SolrEntity<?>> results = getSolrEntities("*:*");
    assertThat(results, hasSize(3));

    results = getSolrEntities("type:Agent");
    assertThat(results, hasSize(1));
    verifyPayload(results.get(0));
    verifyIsShownBy(results.get(0).getPayload());
  }

  /** Helper method to retrieve SolrEntities via search query */
  private List<SolrEntity<?>> getSolrEntities(String searchQuery) throws Exception {
    List<SolrEntity<?>> solrEntities = new ArrayList<>();
    SolrSearchCursorIterator iterator = emSolrService.getSearchIterator(searchQuery);

    while (iterator.hasNext()) {
      solrEntities.addAll(iterator.next());
    }

    return solrEntities;
  }
}
