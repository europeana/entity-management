package eu.europeana.entitymanagement.service;

import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class FailedTaskServiceIT extends AbstractIntegrationTest {

  @Autowired private FailedTaskService service;

  private static final String entityId1 = "http://data.europeana.eu/agent/1";
  private static final String entityId2 = "http://data.europeana.eu/agent/2";

  private static final Exception testException = new Exception("Exception Message");
  private static final Exception secondException = new Exception("Second Exception Message");

  private static final ScheduledTaskType testUpdateType = PERMANENT_DELETION;

  @BeforeEach
  public void setup() {
    service.dropCollection();
  }

  @Test
  void shouldPersistSingleFailure() {

    service.persistFailure(entityId1, testUpdateType, testException);

    Optional<FailedTask> failure = service.getFailure(entityId1);

    assertTrue(failure.isPresent());

    assertEquals(1, failure.get().getFailureCount());
    assertEquals(PERMANENT_DELETION, failure.get().getUpdateType());
  }

  @Test
  void shouldPersistMultipleFailures() {
    service.persistFailureBulk(List.of(entityId1, entityId2), testUpdateType, testException);

    // check that entries were inserted
    List<FailedTask> savedRecords = service.getFailures(List.of(entityId1, entityId2));
    assertEquals(2, savedRecords.size());
  }

  @Test
  void shouldUpdateExistingFailure() {
    // first insert failure
    service.persistFailure(entityId1, testUpdateType, testException);

    // then insert it again
    service.persistFailure(entityId1, testUpdateType, secondException);

    // also check that failure count is incremented
    Optional<FailedTask> saved = service.getFailure(entityId1);

    assertTrue(saved.isPresent());
    assertEquals(2, saved.get().getFailureCount());
    assertEquals(secondException.getMessage(), saved.get().getErrorMessage());
  }

  @Test
  void shouldUpdateExistingFailureBulk() {
    // pre-insert only 1 failure
    service.persistFailure(entityId1, testUpdateType, testException);

    // insert two failures - with one matching pre-inserted
    service.persistFailureBulk(List.of(entityId1, entityId2), testUpdateType, testException);

    // also check that failure count is incremented
    Optional<FailedTask> saved = service.getFailure(entityId1);

    assertTrue(saved.isPresent());
    assertEquals(2, saved.get().getFailureCount());
  }
}
