package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.BatchUtils.getEntityIds;

import eu.europeana.entitymanagement.batch.errorhandling.FailedTaskService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

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

    private Instant startRead, startProcess, startWrite;
    private long readDuration, processDuration, writeDuration;

    @Override
    public void beforeRead() {
        if(logger.isDebugEnabled()) {
            startRead = Instant.now();
        }
    }

    @Autowired
  public EntityUpdateListener(
      FailedTaskService failedTaskService) {
    this.failedTaskService = failedTaskService;
  }

  @Override
  public void afterRead(EntityRecord item) {
        if(logger.isDebugEnabled()) {
            readDuration = Duration.between(startRead, Instant.now()).toMillis();
            logger.debug("afterRead: entityId={}; duration={}ms", item.getEntityId(), readDuration);
        }
  }


  @Override
  public void beforeProcess(EntityRecord item) {
      if(logger.isDebugEnabled()) {
          startProcess = Instant.now();
      }
  }

  @Override
  public void afterProcess(EntityRecord item, EntityRecord result) {
        if(logger.isDebugEnabled()) {
            processDuration = Duration.between(startProcess, Instant.now()).toMillis();
            logger.debug("afterProcess: entityId={}; processDuration={}ms", item.getEntityId(), processDuration);
        }
  }

  @Override
  public void beforeWrite(@NonNull List<? extends EntityRecord> entityRecords) {
          startWrite = Instant.now();
  }


  @Override
  public void afterWrite(@NonNull  List<? extends EntityRecord> entityRecords) {
        writeDuration = Duration.between(startWrite, Instant.now()).toMillis();
        String[] entityIds = getEntityIds(entityRecords);
    logger.info("afterWrite: entityIds={}, count={}; writeDuration={}", Arrays.toString(entityIds), entityIds.length, writeDuration);

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
