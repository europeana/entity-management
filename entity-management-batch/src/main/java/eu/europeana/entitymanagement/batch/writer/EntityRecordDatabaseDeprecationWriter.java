package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.service.EntityBatchService;
import java.util.List;
import java.util.Set;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** ItemWriter for deprecating entities from Mongo */
@Component
public class EntityRecordDatabaseDeprecationWriter implements ItemWriter<BatchEntityRecord> {

  private static final Set<ScheduledTaskType> supportedScheduledTasks =
      Set.of(ScheduledRemovalType.DEPRECATION);

  private final EntityBatchService entityBatchService;

  public EntityRecordDatabaseDeprecationWriter(EntityBatchService entityBatchService) {
    this.entityBatchService = entityBatchService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    List<String> ids = BatchUtils.filterRecordsForWriters(supportedScheduledTasks, entityRecords);

    if (!ids.isEmpty()) {
      entityBatchService.disableBulk(ids);
    }
  }
}
