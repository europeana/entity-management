package eu.europeana.entitymanagement.batch.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mongodb.bulk.BulkWriteResult;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.entitymanagement.batch.repository.ScheduledTaskRepository;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;

@Service(AppConfigConstants.BEAN_BATCH_SCHEDULED_TASK_SERVICE)
public class ScheduledTaskService {

  private final ScheduledTaskRepository repository;

  private static final Logger logger = LogManager.getLogger(ScheduledTaskService.class);

  @Autowired
  public ScheduledTaskService(ScheduledTaskRepository repository) {
    this.repository = repository;
  }

  /**
   * Creates {@link ScheduledTask} instances for entity ids and their update types, and then saves
   * them to the database.
   *
   * @param entityIdsToUpdateType
   */
  public void scheduleTasksForEntities(Map<String, ScheduledTaskType> entityIdsToUpdateType) {
    List<ScheduledTask> tasks = createScheduledTasks(entityIdsToUpdateType, false);

    BulkWriteResult writeResult = repository.upsertBulk(tasks);
    logger.debug(
        "Persisted scheduled tasks to db: matched={}, modified={}, inserted={}",
        writeResult.getMatchedCount(),
        writeResult.getModifiedCount(),
        writeResult.getInsertedCount());
  }

  /**
   * Marks entities as processed.
   *
   * @param entityIdsToUpdateType
   */
  public void markAsProcessed(Map<String, ScheduledTaskType> entityIdsToUpdateType) {
    List<ScheduledTask> tasks = createScheduledTasks(entityIdsToUpdateType, true);

    BulkWriteResult writeResult = repository.markAsProcessed(tasks);
    logger.debug(
        "Marked scheduled tasks as processed: matched={}, modified={}, inserted={}",
        writeResult.getMatchedCount(),
        writeResult.getModifiedCount(),
        writeResult.getInsertedCount());
  }

  /**
   * Removes entities from the ScheduledTasks collection that have been processed
   *
   * @param updateType updateType to filter on
   */
  public void removeProcessedTasks(List<? extends ScheduledTaskType> updateType) {
    long removeCount = repository.removeProcessedTasks(updateType);
    if (removeCount > 0 && logger.isDebugEnabled()) {
      logger.debug(
          "Removed scheduled tasks from db: count={}, updateType={}",
          removeCount,
          updateType.stream().map(ScheduledTaskType::getValue).collect(Collectors.joining(",")));
    }
  }

  /**
   * Removes unprocessed entries from the ScheduledTasks collection if the FailedTasks retryCount is
   * equal or greater than the max number of retries allowed.
   *
   * <p>TODO: investigate if this can be replaced with a delete query
   */
  public void removeScheduledTasksWithFailures(
      int maxFailedTaskRetries, List<? extends ScheduledTaskType> updateType) {

    try (MorphiaCursor<ScheduledTask> cursor =
        repository.getTasksWithFailures(maxFailedTaskRetries, updateType)) {

      while (cursor.hasNext()) {
        repository.deleteScheduledTask(cursor.next().getEntityId());
      }
    }
  }

  public List<BatchEntityRecord> getEntityRecordsForTasks(
      int start, int count, Filter[] queryFilters) {
    return repository.getEntityRecordsForTasks(start, count, queryFilters);
  }

  /**
   * return the 
   * @return the number of scheduled tasks for which the execution is not complete   
   */
  public long getRunningTasksCount() {
    return repository.getRuningTasksCount();
  }
  
  
  /**
   * Gets the ScheduledTask with the given entityId from the database
   *
   * @param entityId entityId
   * @return true if exists, false otherwise
   */
  public Optional<ScheduledTask> getTask(String entityId) {
    return Optional.ofNullable(repository.getTask(entityId));
  }

  public List<ScheduledTask> getTasks(List<String> entityIds) {
    return repository.getTasks(entityIds);
  }

  /** Helper method to instantiate ScheduledTasks from list of entityIds */
  private List<ScheduledTask> createScheduledTasks(
      Map<String, ScheduledTaskType> entityIdToUpdateType, boolean hasBeenProcessed) {
    Instant now = Instant.now();

    return entityIdToUpdateType.entrySet().stream()
        .map(
            mapElem ->
                new ScheduledTask.Builder(mapElem.getKey(), mapElem.getValue())
                    .setProcessed(hasBeenProcessed)
                    .modified(now)
                    .build())
        .collect(Collectors.toList());
  }

  public void dropCollection() {
    this.repository.dropCollection();
  }
}
