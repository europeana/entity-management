package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.ScheduledTaskUtils;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.service.EntityBatchService;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemReader;
import org.springframework.lang.NonNull;

/** {@link ItemReader} that reads documents from MongoDB via a paging technique. */
public class EntityRecordDatabaseReader extends BaseDatabaseReader<BatchEntityRecord> {

  private static final Logger logger = LogManager.getLogger(EntityRecordDatabaseReader.class);
  private final EntityBatchService entityBatchService;
  private final Filter[] queryFilters;
  private final ScheduledTaskType scheduledTaskType;

  public EntityRecordDatabaseReader(
      String scheduledTaskType,
      EntityBatchService entityBatchService,
      int pageSize,
      Filter... queryFilters) {
    super(pageSize);
    this.scheduledTaskType = ScheduledTaskUtils.scheduledTaskTypeValueOf(scheduledTaskType);
    this.entityBatchService = entityBatchService;
    this.queryFilters = queryFilters;
  }

  @Override
  @NonNull
  protected Iterator<BatchEntityRecord> doPageRead() {
    // number of items to skip when reading. pageSize is incremented in parent class every time
    // this method is invoked
    int start = page * pageSize;
    List<? extends EntityRecord> result =
        entityBatchService.findEntitiesWithFilter(start, pageSize, queryFilters);

    List<BatchEntityRecord> batchEntityRecords =
        result.stream()
            .map(p -> new BatchEntityRecord(p, scheduledTaskType))
            .collect(Collectors.toList());

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
