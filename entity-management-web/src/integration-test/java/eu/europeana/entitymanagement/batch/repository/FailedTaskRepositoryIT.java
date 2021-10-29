package eu.europeana.entitymanagement.batch.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask.Builder;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureMockMvc
class FailedTaskRepositoryIT extends AbstractIntegrationTest {

  @Autowired private FailedTaskRepository repository;

  private static final String entityId1 = "http://data.europeana.eu/agent/1";
  private static final String entityId2 = "http://data.europeana.eu/agent/2";

  private final FailedTask failedTask1 = new Builder(entityId1).modified(Instant.now()).build();
  private final FailedTask failedTask2 = new Builder(entityId2).modified(Instant.now()).build();

  @BeforeEach
  public void setup() {
    repository.dropCollection();
  }

  @Test
  void shouldInsertNewFailure() {

    UpdateResult updateResult = repository.upsert(failedTask1);

    assertEquals(0, updateResult.getMatchedCount());
    assertEquals(0, updateResult.getModifiedCount());
    assertNotNull(updateResult.getUpsertedId());
  }

  @Test
  void shouldInsertNewBulkFailures() {
    repository.upsertBulk(List.of(failedTask1, failedTask2));

    // check that entries were inserted
    List<FailedTask> savedRecords = repository.getFailures(List.of(entityId1, entityId2));
    assertEquals(2, savedRecords.size());
  }

  @Test
  void shouldUpdateExistingFailure() {
    // first insert failure
    repository.upsert(failedTask1);

    // then update it
    repository.upsert(failedTask1);

    // also check that failure count is incremented
    List<FailedTask> saved = repository.getFailures(List.of(entityId1));
    assertEquals(1, saved.size());
    assertEquals(2, saved.get(0).getFailureCount());
  }

  @Test
  void shouldUpdateExistingFailureBulk() {
    // pre-insert only 1 task
    repository.upsert(failedTask1);

    BulkWriteResult bulkWriteResult = repository.upsertBulk(List.of(failedTask1, failedTask2));
    // matches on pre-inserted entry
    assertEquals(1, bulkWriteResult.getMatchedCount());
    assertEquals(1, bulkWriteResult.getModifiedCount());

    // also check that failure count is incremented
    List<FailedTask> saved = repository.getFailures(List.of(entityId1));
    assertEquals(2, saved.get(0).getFailureCount());
  }
}
