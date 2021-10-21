package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** ItemWriter for deprecating entities from Mongo */
@Component
public class EntityRecordDatabaseDeprecationWriter implements ItemWriter<EntityRecord> {

  private final EntityRecordService entityRecordService;

  public EntityRecordDatabaseDeprecationWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends EntityRecord> entityRecords) throws Exception {
    entityRecordService.disableBulk(entityRecords);
  }
}
