package eu.europeana.entitymanagement.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_REMOVALS_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_UPDATE_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.ENTITY_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.assertions.Assertions;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.config.EntityUpdateJobConfig;
import eu.europeana.entitymanagement.batch.repository.FailedTaskRepository;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask.Builder;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduledTaskServiceIT extends AbstractIntegrationTest {

  @Autowired private ScheduledTaskService service;

  /** Used for testing deletion based on failureCount */
  @Autowired private FailedTaskRepository failedTaskRepository;

  /** Used for testing EntityRecord retrieval via ScheduledTask */
  @Autowired private EntityRecordRepository entityRecordRepository;

  @Autowired
  @Qualifier(ENTITY_UPDATE_JOB_LAUNCHER)
  JobLauncher entityUpdateJobLauncher;

  @Autowired
  @Qualifier(ENTITY_REMOVALS_JOB_LAUNCHER)
  JobLauncher entityDeletionsJobLauncher;

  @Autowired EntityUpdateJobConfig updateJobConfig;

  @Qualifier(AppConfig.BEAN_EM_SOLR_SERVICE)
  @Autowired
  private SolrService emSolrService;

  @BeforeEach
  void setUp() throws Exception {
    service.dropCollection();
    // ensure a clean FailedTasks collection
    failedTaskRepository.dropCollection();
    entityRecordRepository.dropCollection();
    emSolrService.deleteAllDocuments();
  }

  private static final String entityId1 = "http://data.europeana.eu/agent/1";
  private static final String entityId2 = "http://data.europeana.eu/agent/2";

  private static final ScheduledTaskType testUpdateType = ScheduledUpdateType.FULL_UPDATE;

  @Test
  void shouldCreateTasksForEntities() {
    List<String> entityIds = List.of(ScheduledTaskServiceIT.entityId1, entityId2);

    Map<String, ScheduledTaskType> map =
        Map.of(
            entityId1, testUpdateType,
            entityId2, testUpdateType);
    service.scheduleTasksForEntities(map);

    List<ScheduledTask> tasks = service.getTasks(entityIds);
    assertEquals(2, tasks.size()); 
  }

  @Test
  void shouldFetchRecordsForTasks() throws Exception {
    String europeanaMetadata =
        IntegrationTestUtils.loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = IntegrationTestUtils.loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    // create EntityRecord then create a ScheduledTask for it
    EntityRecord savedEntityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    String entityId = savedEntityRecord.getEntityId();
    Map<String, ScheduledTaskType> entityIdUpdateType = Map.of(entityId, testUpdateType);
    service.scheduleTasksForEntities(entityIdUpdateType);

    List<BatchEntityRecord> retrievedRecords =
        service.getEntityRecordsForTasks(0, 1, new Filter[] {Filters.eq(ENTITY_ID, entityId)});

    assertEquals(1, retrievedRecords.size());
    EntityRecord retrievedEntityRecord = retrievedRecords.get(0).getEntityRecord();

    assertEquals(entityId, retrievedEntityRecord.getEntityId());

    // No need doing a deep comparison here. Just ensure that other fields exist besides entityId
    assertEquals(savedEntityRecord.getProxies().size(), retrievedEntityRecord.getProxies().size());
    assertEquals(
        savedEntityRecord.getCreated().getTime(), retrievedEntityRecord.getCreated().getTime());
  }

  @Test
  void shouldMarkAsProcessed() {
    List<String> entityIds = List.of(entityId1, entityId2);
    Map<String, ScheduledTaskType> map =
        Map.of(
            entityId1, testUpdateType,
            entityId2, testUpdateType);
    service.scheduleTasksForEntities(map);

    service.markAsProcessed(map);

    List<ScheduledTask> tasks = service.getTasks(entityIds);

    assertEquals(2, tasks.size());
    assertTrue(
        tasks.stream().allMatch(ScheduledTask::hasBeenProcessed),
        "Not all tasks marked as processed");
  }

  @Test
  void shouldRemoveScheduledTasksWithFailures() {

    // create scheduledTasks for entityId1 and entityId2
    List<String> entityIds = List.of(ScheduledTaskServiceIT.entityId1, entityId2);
    Map<String, ScheduledTaskType> map =
        Map.of(
            entityId1, testUpdateType,
            entityId2, testUpdateType);

    service.scheduleTasksForEntities(map);

    // create failures for both
    FailedTask failedTask1 = new Builder(entityId1, testUpdateType).modified(Instant.now()).build();
    FailedTask failedTask2 = new Builder(entityId2, testUpdateType).modified(Instant.now()).build();

    failedTaskRepository.upsertBulk(List.of(failedTask1, failedTask2));

    // create additional failure for entityId1. IT now has failureCount = 2
    failedTaskRepository.upsert(failedTask1);

    // remove scheduled tasks with failureCount >= 2
    service.removeScheduledTasksWithFailures(2, List.of(testUpdateType));

    List<ScheduledTask> tasks = service.getTasks(entityIds);

    // task for entityId1 should have been deleted
    assertEquals(1, tasks.size());
    assertEquals(entityId2, tasks.get(0).getEntityId());
  }

  @Test
  void updateScheduledTasksExecution() throws Exception {
    // create first entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);
    String entityId1 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI)
            .getEntityId();

    // create second entity
    europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_STALIN_JSON);
    metisResponse = loadFile(IntegrationTestUtils.AGENT_STALIN_XML);
    String entityId2 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_STALIN_URI)
            .getEntityId();

    // create scheduledTasks for entityId1 and entityId2
    Map<String, ScheduledTaskType> map =
        Map.of(
            entityId1, ScheduledUpdateType.FULL_UPDATE,
            entityId2, ScheduledUpdateType.METRICS_UPDATE);
    service.scheduleTasksForEntities(map);

    //check the count method
    long runningTasks = service.getRunningTasksCount();
    assertEquals(map.size(), runningTasks);
    
    Date dateBeforeRun = new Date();

    entityUpdateJobLauncher.run(
        updateJobConfig.updateScheduledEntities(
            List.of(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE)),
        BatchUtils.createJobParameters(
            null,
            Date.from(Instant.now()),
            List.of(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE),
            false));

    Optional<EntityRecord> entityRecord1Updated = retrieveEntity(entityId1);
    Optional<EntityRecord> entityRecord2Updated = retrieveEntity(entityId2);

    /*
     * check the modification date for the external proxies (updated in case of the dereferce processor which is
     * called during the FULL_UPDATE but not during the METRICS_UPDATE.
     */
    Assertions.assertFalse(
        entityRecord1Updated.get().getExternalProxies().stream()
            .map(p -> p.getProxyIn().getModified().compareTo(dateBeforeRun) > 0)
            .collect(Collectors.toList())
            .contains(false));
    Assertions.assertFalse(
        entityRecord2Updated.get().getExternalProxies().stream()
            .map(p -> p.getProxyIn().getModified().compareTo(dateBeforeRun) > 0)
            .collect(Collectors.toList())
            .contains(true));

    /*
     * check the modified field in the aggregation which is updated during the METRICS_UPDATE
     */
    Assertions.assertTrue(
        entityRecord1Updated
                .get()
                .getEntity()
                .getIsAggregatedBy()
                .getModified()
                .compareTo(dateBeforeRun)
            > 0);
    Assertions.assertTrue(
        entityRecord2Updated
                .get()
                .getEntity()
                .getIsAggregatedBy()
                .getModified()
                .compareTo(dateBeforeRun)
            > 0);

    List<String> entityIds = List.of(entityId1, entityId2);
    List<ScheduledTask> tasks = service.getTasks(entityIds);
    // check that the scheduled tasks has been processed
    Assertions.assertFalse(
        tasks.stream().map(p -> p.hasBeenProcessed()).collect(Collectors.toList()).contains(false));

    //check that not running tasks exist in the database
    runningTasks = service.getRunningTasksCount();
    assertEquals(0, runningTasks);
    
    
    // check that no failed tasks are created
    Assertions.assertTrue(failedTaskRepository.getFailures(entityIds).size() == 0);
  }

  @Test
  void deprecationAndDeletionScheduledTasksExecution() throws Exception {
    // create first entity
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    String entityId1 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();
    // check that the solr entity is created
    SolrConcept solrConcept = emSolrService.searchById(SolrConcept.class, entityId1);
    Assertions.assertNotNull(solrConcept);

    // create second entity
    europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);
    String entityId2 =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();
    // check that the solr entity is created
    solrConcept = emSolrService.searchById(SolrConcept.class, entityId2);
    Assertions.assertNotNull(solrConcept);

    // create scheduledTasks for entityId1 and entityId2
    Map<String, ScheduledTaskType> map =
        Map.of(
            entityId1, ScheduledRemovalType.DEPRECATION,
            entityId2, ScheduledRemovalType.PERMANENT_DELETION);
    service.scheduleTasksForEntities(map);

    //check the count method
    long runningTasks = service.getRunningTasksCount();
    assertEquals(map.size(), runningTasks);
    
    entityDeletionsJobLauncher.run(
        updateJobConfig.removeScheduledEntities(
            List.of(ScheduledRemovalType.DEPRECATION, ScheduledRemovalType.PERMANENT_DELETION)),
        BatchUtils.createJobParameters(
            null,
            Date.from(Instant.now()),
            List.of(ScheduledRemovalType.DEPRECATION, ScheduledRemovalType.PERMANENT_DELETION),
            false));

    Optional<EntityRecord> entityRecord1DbUpdated = retrieveEntity(entityId1);
    Optional<EntityRecord> entityRecord2DbUpdated = retrieveEntity(entityId2);
    SolrConcept entity1Solr = emSolrService.searchById(SolrConcept.class, entityId1);
    SolrConcept entity2Solr = emSolrService.searchById(SolrConcept.class, entityId2);

    // check that the first record is disbaled and the second deleted from the db, and both solr
    // records are deleted
    Assertions.assertTrue(entityRecord1DbUpdated.get().isDisabled());
    Assertions.assertFalse(entityRecord2DbUpdated.isPresent());
    Assertions.assertNull(entity1Solr);
    Assertions.assertNull(entity2Solr);

    List<String> entityIds = List.of(entityId1, entityId2);
    List<ScheduledTask> tasks = service.getTasks(entityIds);
    // check that the scheduled tasks has been processed
    Assertions.assertFalse(
        tasks.stream().map(p -> p.hasBeenProcessed()).collect(Collectors.toList()).contains(false));

    //check the count method, should not have unprocessed tasks anymore
    runningTasks = service.getRunningTasksCount();
    assertEquals(0, runningTasks);
    
    // check that no failed tasks are created
    Assertions.assertTrue(failedTaskRepository.getFailures(entityIds).size() == 0);
  }
}
