package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.getEntityIds;

import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.lang.NonNull;

/** Listens for Read, Processing and Write operations during Entity Update steps. */
public class ScheduledTaskItemListener extends ItemListenerSupport<EntityRecord, EntityRecord> {

  private static final Logger logger = LogManager.getLogger(ScheduledTaskItemListener.class);

  private final FailedTaskService failedTaskService;
  private final ScheduledTaskService scheduledTaskService;
  private final ScheduledTaskType updateType;
  private final boolean isSynchronous;

  public ScheduledTaskItemListener(
      FailedTaskService failedTaskService,
      ScheduledTaskService scheduledTaskService,
      ScheduledTaskType updateType,
      boolean isSynchronous) {
    this.failedTaskService = failedTaskService;
    this.scheduledTaskService = scheduledTaskService;
    this.updateType = updateType;
    this.isSynchronous = isSynchronous;
  }

  @Override
  public void afterWrite(@NonNull List<? extends EntityRecord> entityRecords) {
    if (entityRecords.isEmpty()) {
      return;
    }
    String[] entityIds = getEntityIds(entityRecords);
    if (logger.isDebugEnabled()) {
      logger.debug(
          "afterWrite: entityIds={}, count={};", Arrays.toString(entityIds), entityIds.length);
    }

    // Remove entries from the FailedTask collection if exists
    failedTaskService.removeFailures(Arrays.asList(entityIds));

    // ScheduledTasks cleanup not required for synchronous execution
    if (!isSynchronous) {
      scheduledTaskService.markAsProcessed(Arrays.asList(entityIds), updateType);
    }
  }

  @Override
  public void onReadError(@NonNull Exception e) {
    // No entity linked to error, so we just log a warning
    logger.warn("onReadError", e);
  }

  @Override
  public void onProcessError(@NonNull EntityRecord entityRecord, @NonNull Exception e) {
    String entityId = entityRecord.getEntityId();
    logger.warn("onProcessError: entityId={}", entityId, e);
    failedTaskService.persistFailure(entityId, updateType, e);
  }

  @Override
  public void onWriteError(
      @NonNull Exception e, @NonNull List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);

    logger.warn("onWriteError: entityIds={}", entityIds, e);
    failedTaskService.persistFailureBulk(List.of(entityIds), updateType, e);
  }
}
