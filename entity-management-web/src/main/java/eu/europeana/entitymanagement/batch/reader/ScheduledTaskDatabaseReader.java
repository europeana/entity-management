package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;

/**
 * Reads scheduled entities from the ScheduledTask collection, then retrieves the matching
 * EntityRecords
 */
public class ScheduledTaskDatabaseReader extends BaseDatabaseReader<EntityRecord> {

  private static final Logger logger = LogManager.getLogger(ScheduledTaskDatabaseReader.class);
  private final ScheduledTaskService scheduledTaskService;

  private final Filter[] scheduledTaskFilter;

  public ScheduledTaskDatabaseReader(
      ScheduledTaskService scheduledTaskService, int pageSize, Filter... scheduledTaskFilter) {
    super(pageSize);
    this.scheduledTaskService = scheduledTaskService;
    this.scheduledTaskFilter = scheduledTaskFilter;
  }

  @Override
  @NonNull
  @SuppressWarnings("unchecked")
  protected Iterator<EntityRecord> doPageRead() {
    int start = page * pageSize;
    List<? extends EntityRecord> entityRecords =
        scheduledTaskService.getEntityRecordsForTasks(start, pageSize, scheduledTaskFilter);

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved {} scheduled entities from database. skip={}, limit={}, entityIds={}",
          entityRecords.size(),
          start,
          pageSize,
          Arrays.toString(BatchUtils.getEntityIds(entityRecords)));
    }
    return (Iterator<EntityRecord>) entityRecords.iterator();
  }

  @Override
  String getClassName() {
    return ScheduledTaskDatabaseReader.class.getSimpleName();
  }
}
