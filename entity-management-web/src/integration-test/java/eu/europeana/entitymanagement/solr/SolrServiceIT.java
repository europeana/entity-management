package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.RIGHTS;
import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.SUGGEST_FILTERS;
import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.SUGGEST_FILTER_EUROPEANA;
import static eu.europeana.entitymanagement.vocabulary.EntitySolrFields.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
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
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SolrServiceIT extends AbstractIntegrationTest {

  private static final String RIGHTS_PD = "https://creativecommons.org/publicdomain/zero/1.0/";

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
    Assertions.assertEquals(record.getEntity().getEntityId(), storedAgent.getEntityId());
  }

  EntityRecord buildAgentRecord()
      throws JsonProcessingException, JsonMappingException, IOException {
    Agent agent = objectMapper.readValue(loadFile(AGENT_JSON), Agent.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(agent);
    record.setEntityId(agent.getEntityId());
    EntityProxy europeanaProxy = new EntityProxy();
    europeanaProxy.setProxyId(EntityRecordUtils.getEuropeanaProxyId(record.getEntityId()));
    europeanaProxy.setEntity(agent);
    Aggregation proxyIn = new Aggregation();
    proxyIn.setPageRank(304d);
    proxyIn.setRecordCount(705);
    proxyIn.setScore(970000);
    proxyIn.setRights(RIGHTS_PD);
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
    Assertions.assertEquals(record.getEntity().getEntityId(), agents.get(0).getEntityId());
  }

  @Test
  public void searchAgentWithRights() throws Exception {
    EntityRecord record = buildAgentRecord();
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    String searchQuery =
        TYPE + ":" + record.getEntity().getType() + " AND " + RIGHTS + ":\"" + RIGHTS_PD + "\"";
    List<SolrEntity<?>> agents = getSolrEntities(searchQuery);
    Assertions.assertNotNull(agents);
    Assertions.assertNotNull(agents.get(0));
    Assertions.assertEquals(record.getEntity().getEntityId(), agents.get(0).getEntityId());
  }

  @Test
  public void storeOrganizationInSolr() throws Exception {
    Organization organization =
        objectMapper.readValue(loadFile(ORGANIZATION_JSON), Organization.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(organization);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrOrganization storedOrganization =
        emSolrService.searchById(SolrOrganization.class, organization.getEntityId());
    Assertions.assertNotNull(storedOrganization);
    Assertions.assertEquals(organization.getEntityId(), storedOrganization.getEntityId());
  }

  @Test
  public void storeTimespanInSolr() throws Exception {

    TimeSpan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), TimeSpan.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(timespan);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrTimeSpan storedTimespan =
        emSolrService.searchById(SolrTimeSpan.class, timespan.getEntityId());
    Assertions.assertNotNull(storedTimespan);
    Assertions.assertEquals(timespan.getEntityId(), storedTimespan.getEntityId());
  }

  @Test
  public void storeConceptInSolr() throws Exception {

    Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(concept);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrConcept storedConcept = emSolrService.searchById(SolrConcept.class, concept.getEntityId());
    Assertions.assertNotNull(storedConcept);
    Assertions.assertEquals(concept.getEntityId(), storedConcept.getEntityId());
  }

  @Test
  public void storePlaceInSolr() throws Exception {
    Place place = objectMapper.readValue(loadFile(PLACE_JSON), Place.class);
    EntityRecord record = new EntityRecord();
    record.setEntity(place);
    emSolrService.storeEntity(SolrUtils.createSolrEntity(record));
    SolrPlace storedPlace = emSolrService.searchById(SolrPlace.class, place.getEntityId());
    Assertions.assertNotNull(storedPlace);
    Assertions.assertEquals(place.getEntityId(), storedPlace.getEntityId());
  }

  @Test
  void shouldSearchByQuery() throws Exception {
    EntityRecord agentRecord = buildAgentRecord();
    Place place = objectMapper.readValue(loadFile(PLACE_JSON), Place.class);
    EntityRecord placeRecord = new EntityRecord();
    placeRecord.setEntity(place);
    Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
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
