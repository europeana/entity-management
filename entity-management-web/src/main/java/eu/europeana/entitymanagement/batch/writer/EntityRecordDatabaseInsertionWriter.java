package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** This {@link ItemWriter} saves EntityRecords to the database. */
@Component
public class EntityRecordDatabaseInsertionWriter implements ItemWriter<BatchEntityRecord> {

  private final EntityRecordService entityRecordService;

  public EntityRecordDatabaseInsertionWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> list) throws Exception {
    entityRecordService.saveBulkEntityRecords(list);
  }
}
