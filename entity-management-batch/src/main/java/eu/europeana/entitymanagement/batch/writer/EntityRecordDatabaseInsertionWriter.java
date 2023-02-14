package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.service.EntityBatchService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** This {@link ItemWriter} saves EntityRecords to the database. */
@Component
public class EntityRecordDatabaseInsertionWriter implements ItemWriter<BatchEntityRecord> {

  private final EntityBatchService entityBatchService;

  public EntityRecordDatabaseInsertionWriter(EntityBatchService entityBatchService) {
    this.entityBatchService = entityBatchService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> list) throws Exception {
    List<EntityRecord> entityRecords =
        list.stream().map(BatchEntityRecord::getEntityRecord).collect(Collectors.toList());

    if (!entityRecords.isEmpty()) {
      entityBatchService.saveBulkEntityRecords(entityRecords);
    }
  }
}
