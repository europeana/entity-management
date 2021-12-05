package eu.europeana.entitymanagement.batch.service;

import static eu.europeana.entitymanagement.definitions.batch.EMBatchConstants.ENTITY_ID;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.repository.FailedTaskRepository;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask.Builder;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

  @BeforeEach
  void setUp() {
    service.dropCollection();
    // ensure a clean FailedTasks collection
    failedTaskRepository.dropCollection();
    entityRecordRepository.dropCollection();
  }

  private static final String entityId1 = "http://data.europeana.eu/agent/1";
  private static final String entityId2 = "http://data.europeana.eu/agent/2";

  private static final ScheduledTaskType testUpdateType = ScheduledUpdateType.FULL_UPDATE;

  @Test
  void shouldCreateTasksForEntities() {
    List<String> entityIds = List.of(ScheduledTaskServiceIT.entityId1, entityId2);
    service.scheduleTasksForEntities(entityIds, testUpdateType);

    List<ScheduledTask> tasks = service.getTasks(entityIds);
    assertEquals(2, tasks.size());
  }

  @Test
  void shouldFetchRecordsForTasks() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    // create EntityRecord then create a ScheduledTask for it
    EntityRecord savedEntityRecord =
        createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    String entityId = savedEntityRecord.getEntityId();
    service.scheduleTasksForEntities(List.of(entityId), testUpdateType);

    List<? extends EntityRecord> retrievedRecords =
        service.getEntityRecordsForTasks(0, 1, new Filter[] {Filters.eq(ENTITY_ID, entityId)});

    assertEquals(1, retrievedRecords.size());
    EntityRecord retrievedEntityRecord = retrievedRecords.get(0);

    assertEquals(entityId, retrievedEntityRecord.getEntityId());

    // No need doing a deep comparison here. Just ensure that other fields exist besides entityId
    assertEquals(savedEntityRecord.getProxies().size(), retrievedEntityRecord.getProxies().size());
    assertEquals(
        savedEntityRecord.getCreated().getTime(), retrievedEntityRecord.getCreated().getTime());
  }

  @Test
  void shouldMarkAsProcessed() {
    List<String> entityIds = List.of(ScheduledTaskServiceIT.entityId1, entityId2);
    service.scheduleTasksForEntities(entityIds, testUpdateType);

    service.markAsProcessed(entityIds, testUpdateType);

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
    service.scheduleTasksForEntities(entityIds, testUpdateType);

    // create failures for both
    FailedTask failedTask1 = new Builder(entityId1, testUpdateType).modified(Instant.now()).build();
    FailedTask failedTask2 = new Builder(entityId2, testUpdateType).modified(Instant.now()).build();

    failedTaskRepository.upsertBulk(List.of(failedTask1, failedTask2));

    // create additional failure for entityId1. IT now has failureCount = 2
    failedTaskRepository.upsert(failedTask1);

    // remove scheduled tasks with failureCount >= 2
    service.removeScheduledTasksWithFailures(2, testUpdateType);

    List<ScheduledTask> tasks = service.getTasks(entityIds);

    // task for entityId1 should have been deleted
    assertEquals(1, tasks.size());
    assertEquals(entityId2, tasks.get(0).getEntityId());
  }
}
