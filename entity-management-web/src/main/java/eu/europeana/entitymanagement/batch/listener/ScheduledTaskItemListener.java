package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.getEntityIds;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.lang.NonNull;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;

/** Listens for Read, Processing and Write operations during Entity Update steps. */
public class ScheduledTaskItemListener extends ItemListenerSupport<BatchEntityRecord, BatchEntityRecord> {

  private static final Logger logger = LogManager.getLogger(ScheduledTaskItemListener.class);

  private final FailedTaskService failedTaskService;
  private final ScheduledTaskService scheduledTaskService;
  private final boolean isSynchronous;

  public ScheduledTaskItemListener(
      FailedTaskService failedTaskService,
      ScheduledTaskService scheduledTaskService,
      boolean isSynchronous) {
    this.failedTaskService = failedTaskService;
    this.scheduledTaskService = scheduledTaskService;
    this.isSynchronous = isSynchronous;
  }

  @Override
  public void afterWrite(@NonNull List<? extends BatchEntityRecord> entityRecords) {
    if (entityRecords.isEmpty()) {
      return;
    }
    String[] entityIds = getEntityIds((List<BatchEntityRecord>) entityRecords);
    if (logger.isDebugEnabled()) {
      logger.debug(
          "afterWrite: entityIds={}, count={};", Arrays.toString(entityIds), entityIds.length);
    }

    // Remove entries from the FailedTask collection if exists
    failedTaskService.removeFailures(Arrays.asList(entityIds));
    
    // ScheduledTasks cleanup not required for synchronous execution
    if (!isSynchronous) {
      scheduledTaskService.markAsProcessed(entityRecords.stream().collect(Collectors.toMap(p -> p.getEntityRecord().getEntityId(), p -> p.getScheduledTaskType())));
    }
  }

  @Override
  public void onReadError(@NonNull Exception e) {
    // No entity linked to error, so we just log a warning
    logger.warn("onReadError", e);
  }

  @Override
  public void onProcessError(@NonNull BatchEntityRecord entityRecord, @NonNull Exception e) {
    String entityId = entityRecord.getEntityRecord().getEntityId();
    logger.warn("onProcessError: entityId={}", entityId, e);
    failedTaskService.persistFailure(entityId, entityRecord.getScheduledTaskType(), e);
  }

  @Override
  public void onWriteError(
      @NonNull Exception e, @NonNull List<? extends BatchEntityRecord> entityRecords) {
    String[] entityIds = getEntityIds((List<BatchEntityRecord>) entityRecords);

    logger.warn("onWriteError: entityIds={}", entityIds, e);
    failedTaskService.persistFailureBulk(entityRecords.stream().collect(Collectors.toMap(r -> r.getEntityRecord().getEntityId(), r -> r.getScheduledTaskType())), e);
  }
}
