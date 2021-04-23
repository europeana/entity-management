package eu.europeana.entitymanagement.batch.listener;

import eu.europeana.entitymanagement.batch.errorhandling.EntityUpdateFailureService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class EntityUpdateListener extends ItemListenerSupport<EntityRecord, EntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityUpdateListener.class);

  private final EntityUpdateFailureService entityUpdateFailureService;

  @Autowired
  public EntityUpdateListener(
      EntityUpdateFailureService entityUpdateFailureService) {
    this.entityUpdateFailureService = entityUpdateFailureService;
  }

  @Override
  public void beforeRead() {
    logger.debug("beforeRead");
  }

  @Override
  public void afterRead(EntityRecord item) {
    logger.debug("afterRead: entityId={}", item.getEntityId());
  }


  @Override
  public void beforeProcess(EntityRecord item) {
    logger.debug("beforeProcess: entityId={}", item.getEntityId());
  }

  @Override
  public void afterProcess(EntityRecord item, EntityRecord result) {
    logger.debug("afterProcess: entityId={}", item.getEntityId());
  }

  @Override
  public void beforeWrite(@NonNull List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);
    logger.debug("beforeWrite: entityIds={}", Arrays.toString(entityIds));
  }


  @Override
  public void afterWrite(@NonNull  List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);
    logger.debug("afterWrite: entityIds={}", Arrays.toString(entityIds));
  }

  @Override
  public void onReadError(@NonNull Exception e) {
    // No entity linked to error, so we just log a warning
    logger.warn("onReadError", e);
  }

  @Override
  public void onProcessError(@NonNull EntityRecord entityRecord, @NonNull Exception e) {
    logger.warn("onProcessError: entityId={}", entityRecord, e);
    entityUpdateFailureService.persistFailure(entityRecord.getEntityId(), e);
  }



  @Override
  public void onWriteError(@NonNull Exception e, @NonNull  List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);

    logger.warn("onWriteError: entityIds={}", entityIds, e);
    entityUpdateFailureService.persistFailureBulk(entityRecords, e);
  }

  private String[] getEntityIds(List<? extends EntityRecord> entityRecords) {
    return entityRecords.stream().map(EntityRecord::getEntityId)
        .toArray(String[]::new);
  }

}
