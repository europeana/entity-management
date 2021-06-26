package eu.europeana.entitymanagement.batch.listener;

import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static eu.europeana.entitymanagement.batch.BatchUtils.getEntityIds;

/**
 * Listens for Read, Processing and Write operations during Entity Update steps.
 */
@Component
public class EntityUpdateListener extends ItemListenerSupport<EntityRecord, EntityRecord> {

    private static final Logger logger = LogManager.getLogger(EntityUpdateListener.class);

    private final FailedTaskService failedTaskService;
    private final ScheduledTaskService scheduledTaskService;

    @Autowired
  public EntityUpdateListener(
            FailedTaskService failedTaskService, ScheduledTaskService scheduledTaskService) {
    this.failedTaskService = failedTaskService;
        this.scheduledTaskService = scheduledTaskService;
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

      // Also remove from ScheduledTasks
    scheduledTaskService.removeTasks(Arrays.asList(entityIds));
  }

  @Override
  public void onReadError(@NonNull Exception e) {
    // No entity linked to error, so we just log a warning
    logger.warn("onReadError", e);
  }

  @Override
  public void onProcessError(@NonNull EntityRecord entityRecord, @NonNull Exception e) {
    logger.warn("onProcessError: entityId={}", entityRecord.getEntityId(), e);
    failedTaskService.persistFailure(entityRecord.getEntityId(), e);
  }


  @Override
  public void onWriteError(@NonNull Exception e, @NonNull  List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);

    logger.warn("onWriteError: entityIds={}", entityIds, e);
    failedTaskService.persistFailureBulk(entityRecords, e);
  }
}
