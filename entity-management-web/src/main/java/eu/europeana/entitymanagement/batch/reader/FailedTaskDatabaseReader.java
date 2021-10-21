package eu.europeana.entitymanagement.batch.reader;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;

/** Reads Failures from the FailedTasks collection, then retrieves the matching EntityRecords */
public class FailedTaskDatabaseReader extends BaseDatabaseReader<EntityRecord> {

  private static final Logger logger = LogManager.getLogger(FailedTaskDatabaseReader.class);
  private final FailedTaskService failureService;

  private final Filter[] failureQueryFilter;

  public FailedTaskDatabaseReader(
      FailedTaskService failureService, int pageSize, Filter... failureQueryFilter) {
    super(pageSize);
    this.failureService = failureService;
    this.failureQueryFilter = failureQueryFilter;
  }

  @Override
  @NonNull
  @SuppressWarnings("unchecked")
  protected Iterator<EntityRecord> doPageRead() {
    int start = page * pageSize;
    List<? extends EntityRecord> failedRecords =
        failureService.getEntityRecordsForFailures(start, pageSize, failureQueryFilter);

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved {} failed EntityRecords from database. skip={}, limit={}, entityIds={}",
          failedRecords.size(),
          start,
          pageSize,
          Arrays.toString(BatchUtils.getEntityIds(failedRecords)));
    }
    return (Iterator<EntityRecord>) failedRecords.iterator();
  }

  @Override
  String getClassName() {
    return FailedTaskDatabaseReader.class.getSimpleName();
  }
}
