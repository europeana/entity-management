package eu.europeana.entitymanagement.batch.reader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.lang.NonNull;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.ScheduledTaskUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/** {@link ItemReader} that reads documents from MongoDB via a paging technique. */
public class EntityRecordDatabaseReader extends BaseDatabaseReader<BatchEntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityRecordDatabaseReader.class);
  private final EntityRecordService entityRecordService;
  private final Filter[] queryFilters;
  private final ScheduledTaskType scheduledTaskType;

  public EntityRecordDatabaseReader(
      String scheduledTaskType,
      EntityRecordService entityRecordService,
      int pageSize,
      Filter... queryFilters) {
    super(pageSize);
    this.scheduledTaskType = ScheduledTaskUtils.scheduledTaskTypeValueOf(scheduledTaskType);
    this.entityRecordService = entityRecordService;
    this.queryFilters = queryFilters;
  }

  @Override
  @NonNull
  protected Iterator<BatchEntityRecord> doPageRead() {
    // number of items to skip when reading. pageSize is incremented in parent class every time
    // this method is invoked
    int start = page * pageSize;
    //the dereference profile is not needed here as the consolidation is taking care of processing references
    List<EntityRecord> result =
        entityRecordService.findEntitiesWithFilter(start, pageSize, queryFilters, null);

    List<BatchEntityRecord> batchEntityRecords = toBatchEntityRecords(result, scheduledTaskType);

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved {} EntityRecords from database. skip={}, limit={}, entityIds={}",
          result.size(),
          start,
          pageSize,
          Arrays.toString(BatchUtils.getEntityIds(batchEntityRecords)));
    }

    return batchEntityRecords.iterator();
  }

  @Override
  String getClassName() {
    return EntityRecordDatabaseReader.class.getSimpleName();
  }
}
