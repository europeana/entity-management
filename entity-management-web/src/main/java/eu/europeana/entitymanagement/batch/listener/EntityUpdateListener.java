package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.BatchUtils.getEntityIds;

import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.lang.NonNull;

/**
 * Listens for Read, Processing and Write operations during Entity Update steps.
 */
public class EntityUpdateListener extends ItemListenerSupport<EntityRecord, EntityRecord> {

    private static final Logger logger = LogManager.getLogger(EntityUpdateListener.class);

    private final FailedTaskService failedTaskService;
    private final ScheduledTaskService scheduledTaskService;
    private final BatchUpdateType updateType;

  public EntityUpdateListener(
            FailedTaskService failedTaskService, ScheduledTaskService scheduledTaskService, BatchUpdateType updateType) {
    this.failedTaskService = failedTaskService;
        this.scheduledTaskService = scheduledTaskService;
        this.updateType = updateType;
    }



  @Override
  public void afterWrite(@NonNull  List<? extends EntityRecord> entityRecords) {
        if(entityRecords.isEmpty()){
            return;
        }
      String[] entityIds = getEntityIds(entityRecords);
      if(logger.isDebugEnabled()) {
            logger.debug("afterWrite: entityIds={}, count={};", Arrays.toString(entityIds), entityIds.length);
        }

    // Remove entries from the FailedTask collection if exists
    failedTaskService.removeFailures(Arrays.asList(entityIds));

      // Also remove from ScheduledTasks (only deleted if updateType matches that of current run)
    scheduledTaskService.removeTasks(updateType, Arrays.asList(entityIds));
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
    failedTaskService.persistFailure(entityId, e);

    // Remove entityId from ScheduledTasks to prevent continuous update loop
    scheduledTaskService.removeTasks(updateType, Collections.singletonList(entityId));
  }


  @Override
  public void onWriteError(@NonNull Exception e, @NonNull  List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);

    logger.warn("onWriteError: entityIds={}", entityIds, e);
    failedTaskService.persistFailureBulk(entityRecords, e);

    scheduledTaskService.removeTasks(updateType, Arrays.asList(entityIds));
  }
}
