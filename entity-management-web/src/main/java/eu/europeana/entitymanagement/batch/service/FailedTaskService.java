package eu.europeana.entitymanagement.batch.service;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import eu.europeana.entitymanagement.batch.repository.FailedTaskRepository;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FailedTaskService {

  private final FailedTaskRepository failureRepository;
  private static final Logger logger = LogManager.getLogger(FailedTaskService.class);

  @Autowired
  public FailedTaskService(FailedTaskRepository failureRepository) {
    this.failureRepository = failureRepository;
  }

  /**
   * Creates a {@link FailedTask} instance for this entity, and then persists it
   *
   * @param entityId entityId
   * @param updateType
   * @param e exception
   */
  public void persistFailure(String entityId, ScheduledTaskType updateType, Exception e) {
    UpdateResult result =
        failureRepository.upsert(
            createUpdateFailure(
                entityId,
                updateType,
                Instant.now(),
                e.getMessage(),
                ExceptionUtils.getStackTrace(e)));

    logger.info(
        "Persisted update failure to db. entityId={} matched={}, modified={}",
        entityId,
        result.getMatchedCount(),
        result.getModifiedCount());
  }

  /**
   * Creates {@link FailedTask} instances for all entities, and then saves them to the database
   *
   * @param entityIds list of entity records to be saved
   * @param e exception
   */
  public void persistFailureBulk(
      List<String> entityIds, ScheduledTaskType updateType, Exception e) {
    String message = e.getMessage();
    String stackTrace = ExceptionUtils.getStackTrace(e);
    Instant now = Instant.now();

    // create FailedTask instance for each entityRecord
    List<FailedTask> failures =
        entityIds.stream()
            .map(entityId -> createUpdateFailure(entityId, updateType, now, message, stackTrace))
            .collect(Collectors.toList());

    BulkWriteResult writeResult = failureRepository.upsertBulk(failures);
    logger.info(
        "Persisted update failures to db: matched={}, modified={}, inserted={}",
        writeResult.getMatchedCount(),
        writeResult.getModifiedCount(),
        writeResult.getInsertedCount());
  }

  /**
   * Removes entities from the FailedTasks collection if their entityId is contained within the
   * provided entityIds
   *
   * @param entityIds list of entityIds
   */
  public void removeFailures(List<String> entityIds) {
    long removeCount = failureRepository.removeFailures(entityIds);
    if (removeCount > 0) {
      logger.info("Removed update failures from db: count={}", removeCount);
    }
  }

  public List<String> getEntityRecordsIdsForFailures(int start, int count) {
    return failureRepository.getEntityRecordsIdsForFailures(start, count);
  }

  public void dropCollection() {
    failureRepository.dropCollection();
  }

  public Optional<FailedTask> getFailure(String entityId) {
    return Optional.ofNullable(failureRepository.getFailure(entityId));
  }

  public List<FailedTask> getFailures(List<String> entityIds) {
    return failureRepository.getFailures(entityIds);
  }

  /** Helper method to instantiate {@link FailedTask} instances */
  private FailedTask createUpdateFailure(
      String entityId,
      ScheduledTaskType updateType,
      Instant modified,
      String message,
      String stacktrace) {
    return new FailedTask.Builder(entityId, updateType)
        .modified(modified)
        .message(message)
        .stackTrace(stacktrace)
        .build();
  }
}
