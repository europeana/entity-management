package eu.europeana.entitymanagement.batch.errorhandling;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityUpdateFailureService {

  private final EntityUpdateFailureRepository failureRepository;
  private static final Logger logger = LogManager.getLogger(EntityUpdateFailureService.class);

  @Autowired
  public EntityUpdateFailureService(
      EntityUpdateFailureRepository failureRepository) {
    this.failureRepository = failureRepository;
  }

  /**
   * Creates a {@link EntityUpdateFailure} instance for this entity, and then persists it
   *
   * @param entityId entityId
   * @param e        exception
   */
  public void persistFailure(String entityId, Exception e) {
    UpdateResult result = failureRepository.upsert(
        createUpdateFailure(entityId, Instant.now(), e.getMessage(),
            ExceptionUtils.getStackTrace(e)));

    logger.debug("Persisted update failure to db. entityId={} matched={}, modified={}", entityId,
        result.getMatchedCount(), result.getModifiedCount());
  }

  /**
   * Creates {@link EntityUpdateFailure} instances for all entities, and then saves them to the
   * database
   *
   * @param entityRecords list of entity records to be saved
   * @param e exception
   */
  public void persistFailureBulk(List<? extends EntityRecord> entityRecords, Exception e) {
    String message = e.getMessage();
    String stackTrace = ExceptionUtils.getStackTrace(e);
    Instant now = Instant.now();

    // create EntityUpdateFailure instance for each entityRecord
    List<EntityUpdateFailure> failures = entityRecords.stream()
        .map(r -> createUpdateFailure(r.getEntityId(), now, message, stackTrace)).collect(
            Collectors.toList());

    BulkWriteResult writeResult = failureRepository.upsertBulk(failures);
    logger.debug("Persisted update failures to db: matched={}, modified={}, inserted={}",
        writeResult.getMatchedCount(), writeResult.getModifiedCount(),
        writeResult.getInsertedCount());
  }

  /**
   * Removes entities from the FailedTasks collection if their entityId is contained within
   * the provided entityIds
   * @param entityIds list of entityIds
   */
  public void removeFailures(List<String> entityIds) {
    long removeCount = failureRepository.removeFailures(entityIds);
    logger.debug("Removed update failures from db: count={}", removeCount);
  }

  /**
   * Helper method to instantiate {@link EntityUpdateFailure} instances
   */
  private EntityUpdateFailure createUpdateFailure(String entityId, Instant time, String message,
      String stacktrace) {
    return new EntityUpdateFailure.Builder(entityId)
        .timestamp(time)
        .message(message)
        .stackTrace(stacktrace)
        .build();
  }
}
