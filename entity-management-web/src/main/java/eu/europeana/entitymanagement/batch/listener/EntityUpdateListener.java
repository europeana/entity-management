package eu.europeana.entitymanagement.batch.listener;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class EntityUpdateListener implements ItemReadListener<EntityRecord>,
    ItemWriteListener<EntityRecord>,
    ItemProcessListener<EntityRecord, EntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityUpdateListener.class);

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
  public void afterWrite(List<? extends EntityRecord> entityRecords) {
    String[] entityIds = getEntityIds(entityRecords);
    logger.debug("afterWrite: entityIds={}", Arrays.toString(entityIds));
  }


  @Override
  public void onProcessError(@NonNull EntityRecord item, @NonNull Exception e) {

  }


  @Override
  public void onReadError(@NonNull Exception e) {

  }


  @Override
  public void onWriteError(@NonNull Exception e, @NonNull  List<? extends EntityRecord> items) {

  }

  private String[] getEntityIds(List<? extends EntityRecord> items) {
    return items.stream().map(EntityRecord::getEntityId).toArray(String[]::new);
  }
}
