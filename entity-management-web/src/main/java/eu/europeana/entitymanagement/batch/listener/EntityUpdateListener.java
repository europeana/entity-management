package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.BatchUtils.getEntityIds;

import eu.europeana.entitymanagement.batch.errorhandling.FailedTaskService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Listens for Read, Processing and Write operations during Entity Update steps.
 */
@Component
public class EntityUpdateListener extends ItemListenerSupport<EntityRecord, EntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityUpdateListener.class);

  private final FailedTaskService failedTaskService;

  @Autowired
  public EntityUpdateListener(
      FailedTaskService failedTaskService) {
    this.failedTaskService = failedTaskService;
  }

  @Override
  public void afterRead(EntityRecord item) {
      if(logger.isDebugEnabled()) {
          logger.debug("afterRead: entityId={}", item.getEntityId());
      }
  }


  @Override
  public void beforeProcess(EntityRecord item) {
      if(logger.isDebugEnabled()) {
          logger.debug("beforeProcess: entityId={}", item.getEntityId());
      }
  }

  @Override
  public void afterProcess(EntityRecord item, EntityRecord result) {
      logger.debug("afterProcess: entityId={}", item.getEntityId());
  }

  @Override
  public void beforeWrite(@NonNull List<? extends EntityRecord> entityRecords) {
      if(logger.isDebugEnabled()) {
          String[] entityIds = getEntityIds(entityRecords);
          logger.debug("beforeWrite: entityIds={}", Arrays.toString(entityIds));
      }
  }


  @Override
  public void afterWrite(@NonNull  List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);
    logger.info("afterWrite: entityIds={}, count={}", Arrays.toString(entityIds), entityIds.length);

    // Remove entries from the FailedTask collection if exists
    failedTaskService.removeFailures(Arrays.asList(entityIds));
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
