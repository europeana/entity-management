package eu.europeana.entitymanagement.batch.writer;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/** ItemWriter for deprecating entities from Mongo */
@Component
public class EntityRecordDatabaseDeprecationWriter implements ItemWriter<BatchEntityRecord> {

  private static final List<ScheduledTaskType> supportedScheduledTasks = List.of(ScheduledRemovalType.DEPRECATION);

  private final EntityRecordService entityRecordService;

  public EntityRecordDatabaseDeprecationWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    entityRecordService.disableBulk((List<BatchEntityRecord>)BatchUtils.filterRecordsForWritters(supportedScheduledTasks, entityRecords));
  }
}
