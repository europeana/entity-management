package eu.europeana.entitymanagement.batch.writer;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_RECORD_DB_REMOVAL_WRITER;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/** ItemWriter for deleting EntityRecords from Mongo */
@Component(ENTITY_RECORD_DB_REMOVAL_WRITER)
public class EntityRecordDatabaseRemovalWriter implements ItemWriter<BatchEntityRecord> {

  private final EntityRecordService entityRecordService;

  public EntityRecordDatabaseRemovalWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    List<String> ids = BatchUtils.filterRecordsForWriters(entityRecords);

    if (!ids.isEmpty()) {
      entityRecordService.deleteBulk(ids, false);
    }
  }
}
