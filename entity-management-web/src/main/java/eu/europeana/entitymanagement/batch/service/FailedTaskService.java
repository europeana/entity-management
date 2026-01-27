package eu.europeana.entitymanagement.batch.service;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import eu.europeana.entitymanagement.batch.repository.FailedTaskRepository;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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
  public void persistFailure(String entityId, TaskType updateType, Exception e) {
    UpdateResult result =
        failureRepository.upsert(
            createUpdateFailure(
                entityId,
                updateType,
                Instant.now(),
                e.getMessage(),
                ExceptionUtils.getStackTrace(e)));

    logger.debug(
        "Persisted update failure to db. entityId={} matched={}, modified={}, aknowledged={}",
        entityId,
        result.getMatchedCount(),
        result.getModifiedCount(),
        result.wasAcknowledged());
  }

  /**
   * Creates {@link FailedTask} instances for all entities, and then saves them to the database
   *
   * @param entityIdsToUpdateType
   * @param e
   */
  public void persistFailureBulk(
      Map<String, TaskType> entityIdsToUpdateType, Exception e) {
    String message = e.getMessage();
    String stackTrace = ExceptionUtils.getStackTrace(e);
    Instant now = Instant.now();

    // create FailedTask instance for each entity id
    List<FailedTask> failures =
        entityIdsToUpdateType.entrySet().stream()
            .map(r -> createUpdateFailure(r.getKey(), r.getValue(), now, message, stackTrace))
            .collect(Collectors.toList());

    BulkWriteResult writeResult = failureRepository.upsertBulk(failures);
    logger.debug(
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
  public void removeFailures(@NonNull List<String> entityIds) {
    removeFailures(entityIds, null);
  }

  /**
   * Removes entities from the FailedTasks collection if their entityId is contained within the
   * provided entityIds
   * @param entityIds list of entityIds
   * @param taskType the type of failed update
   */
  public void removeFailures(List<String> entityIds, TaskType taskType) {
    if(entityIds.isEmpty()) {
      return;
    }
    
    //delete all if taskType not set or if it is full update
    TaskType updateType = (taskType == null || TaskType.full_update == taskType)? null : taskType;
    
    long removeCount = failureRepository.removeFailures(entityIds, updateType);
    if (removeCount > 0) {
      logger.debug("Removed update failures from db: count={}", removeCount);
    }
  }
  
  /**
   * The method used to retrieve entity ids from failed tasks table
   * @param taskType the type of failed update
   * @param start start counter
   * @param count nr of records
   * @return list of entity ids for which update failed
   */
  public List<String> getEntityIdsWithFailures(TaskType taskType, int start, int count) {
    return failureRepository.getEntityIdsWithFailures(taskType, start, count);
  }

  public void dropCollection() {
    failureRepository.dropCollection();
  }

  /**
   * Retrieve the failed task for an entity (note, for failed create organization operations, the external URL needs to be used)
   * @param entityId 
   * @return
   */
  public Optional<FailedTask> getFailure(String entityId) {
    return Optional.ofNullable(failureRepository.getFailure(entityId));
  }

  /**
   * Retr
   * @param entityIds
   * @return
   */
  public List<FailedTask> getFailures(List<String> entityIds) {
    return failureRepository.getFailures(entityIds);
  }

  /** Helper method to instantiate {@link FailedTask} instances */
  private FailedTask createUpdateFailure(
      String entityId,
      TaskType updateType,
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
