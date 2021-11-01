package eu.europeana.entitymanagement.batch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.repository.FailedTaskRepository;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask.Builder;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
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

  @BeforeEach
  void setUp() {
    service.dropCollection();
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
    // ensure a clean FailedTasks collection
    failedTaskRepository.dropCollection();

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
    service.removeScheduledTasksWithFailures(2);

    List<ScheduledTask> tasks = service.getTasks(entityIds);

    // task for entityId1 should have been deleted
    assertEquals(1, tasks.size());
    assertEquals(entityId2, tasks.get(0).getEntityId());
  }
}
