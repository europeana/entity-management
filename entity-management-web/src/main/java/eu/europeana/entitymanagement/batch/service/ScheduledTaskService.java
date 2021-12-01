package eu.europeana.entitymanagement.batch.service;

import com.mongodb.bulk.BulkWriteResult;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.entitymanagement.batch.repository.ScheduledTaskRepository;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {

  private final ScheduledTaskRepository repository;

  private static final Logger logger = LogManager.getLogger(ScheduledTaskService.class);

  @Autowired
  public ScheduledTaskService(ScheduledTaskRepository repository) {
    this.repository = repository;
  }

  /**
   * Creates {@link ScheduledTask} instances for entityIds, and then saves them to the database
   *
   * @param entityIds list of entityIds
   */
  public void scheduleTasksForEntities(List<String> entityIds, ScheduledTaskType updateType) {
    List<ScheduledTask> tasks = createScheduledTasks(entityIds, updateType, false);

    BulkWriteResult writeResult = repository.upsertBulk(tasks);
    logger.info(
        "Persisted scheduled tasks to db: matched={}, modified={}, inserted={}, updateType={}",
        writeResult.getMatchedCount(),
        writeResult.getModifiedCount(),
        writeResult.getInsertedCount(),
        updateType);
  }

  /**
   * Marks entities as processed
   *
   * @param entityIds list of entityIds
   * @param updateType update type
   */
  public void markAsProcessed(List<String> entityIds, ScheduledTaskType updateType) {
    List<ScheduledTask> tasks = createScheduledTasks(entityIds, updateType, true);

    BulkWriteResult writeResult = repository.markAsProcessed(updateType, tasks);
    logger.info(
        "Marked scheduled tasks as processed: matched={}, modified={}, inserted={}, updateType={}",
        writeResult.getMatchedCount(),
        writeResult.getModifiedCount(),
        writeResult.getInsertedCount(),
        updateType);
  }

  /**
   * Removes entities from the ScheduledTasks collection that have been processed
   *
   * @param updateType updateType to filter on
   */
  public void removeProcessedTasks(ScheduledTaskType updateType) {
    long removeCount = repository.removeProcessedTasks(updateType);
    if (removeCount > 0 && logger.isDebugEnabled()) {
      logger.debug(
          "Removed scheduled tasks from db: count={}, updateType={}", removeCount, updateType);
    }
  }

  /**
   * Removes unprocessed entries from the ScheduledTasks collection if the FailedTasks retryCount is
   * equal or greater than the max number of retries allowed.
   *
   * <p>TODO: investigate if this can be replaced with a delete query
   */
  public void removeScheduledTasksWithFailures(
      int maxFailedTaskRetries, ScheduledTaskType updateType) {

    try (MorphiaCursor<ScheduledTask> cursor =
        repository.getTasksWithFailures(maxFailedTaskRetries, updateType)) {

      while (cursor.hasNext()) {
        repository.deleteScheduledTask(cursor.next().getEntityId());
      }
    }
  }

  public List<? extends EntityRecord> getEntityRecordsForTasks(
      int start, int count, Filter[] queryFilters) {
    return repository.getEntityRecordsForTasks(start, count, queryFilters);
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
      List<String> entityIds, ScheduledTaskType updateType, boolean hasBeenProcessed) {
    Instant now = Instant.now();

    return entityIds.stream()
        .map(
            entityId ->
                new ScheduledTask.Builder(entityId, updateType)
                    .setProcessed(hasBeenProcessed)
                    .modified(now)
                    .build())
        .collect(Collectors.toList());
  }

  public void dropCollection() {
    this.repository.dropCollection();
  }
}
