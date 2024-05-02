package eu.europeana.entitymanagement.batch.reader;

import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import dev.morphia.query.filters.Filter;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/**
 * Reads scheduled entities from the ScheduledTask collection, then retrieves the matching
 * EntityRecords
 */
public class ScheduledTaskDatabaseReader extends BaseDatabaseReader<BatchEntityRecord> {

  private static final Logger logger = LogManager.getLogger(ScheduledTaskDatabaseReader.class);
  private final ScheduledTaskService scheduledTaskService;
  private final EntityRecordService entityRecordService;

  private final Filter[] scheduledTaskFilter;

  public ScheduledTaskDatabaseReader(
      ScheduledTaskService scheduledTaskService, EntityRecordService entityRecordService, int pageSize, Filter... scheduledTaskFilter) {
    super(pageSize);
    this.scheduledTaskService = scheduledTaskService;
    this.entityRecordService = entityRecordService;
    this.scheduledTaskFilter = scheduledTaskFilter;
  }

  @Override
  @NonNull
  protected Iterator<BatchEntityRecord> doPageRead() {
    int start = page * pageSize;
    
    List<ScheduledTask> scheduledTasks =
        scheduledTaskService.getTasks(start, pageSize, scheduledTaskFilter);
    
    List<String> entityIds = scheduledTasks.stream().map(st -> st.getEntityId()).toList();
    //no need to use the dereference profile here as the consolidation takes care of processing references
    List<EntityRecord> records = entityRecordService.retrieveMultipleByEntityIds(entityIds, false, true, EntityProfile.dereference.name());
    List<BatchEntityRecord> batchRecords = toBatchEntityRecords(records, scheduledTasks);

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved {} scheduled entities from database. skip={}, limit={}, entityIds={}",
          batchRecords.size(),
          start,
          pageSize,
          entityIds);
    }
    return batchRecords.iterator();
  }

  @Override
  String getClassName() {
    return ScheduledTaskDatabaseReader.class.getSimpleName();
  }
}
