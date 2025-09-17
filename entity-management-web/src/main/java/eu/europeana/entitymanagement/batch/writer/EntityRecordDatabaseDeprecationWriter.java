package eu.europeana.entitymanagement.batch.writer;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_RECORD_DB_DEPRECATION_WRITER;

/** ItemWriter for deprecating entities from Mongo */
@Component(ENTITY_RECORD_DB_DEPRECATION_WRITER)
public class EntityRecordDatabaseDeprecationWriter implements ItemWriter<BatchEntityRecord> {

  private final EntityRecordService entityRecordService;

  public EntityRecordDatabaseDeprecationWriter(EntityRecordService entityRecordService) {
    this.entityRecordService = entityRecordService;
  }

  @Override
  public void write(@NonNull List<? extends BatchEntityRecord> entityRecords) throws Exception {
    List<String> ids = BatchUtils.filterRecordsForWriters(entityRecords);

    if (!ids.isEmpty()) {
      entityRecordService.disableBulk(ids);
    }
  }
}
