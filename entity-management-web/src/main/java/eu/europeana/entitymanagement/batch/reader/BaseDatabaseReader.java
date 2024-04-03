package eu.europeana.entitymanagement.batch.reader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public abstract class BaseDatabaseReader<T> extends AbstractPaginatedDataItemReader<T> {

  private final int readerPageSize;

  protected BaseDatabaseReader(int pageSize) {
    this.readerPageSize = pageSize;
  }

  abstract String getClassName();

  @Override
  protected void doOpen() throws Exception {
    super.doOpen();
    // Non-restartable, as we expect this to run in multi-threaded steps.
    // see: https://stackoverflow.com/a/20002493
    setSaveState(false);
    setPageSize(readerPageSize);
    setName(getClassName());
  }
  
  List<BatchEntityRecord> toBatchEntityRecords(List<EntityRecord> result, ScheduledTaskType scheduledTaskType) {
    return
        result.stream()
            .map(rec -> new BatchEntityRecord(rec, scheduledTaskType))
            .collect(Collectors.toList());
  }
  
  List<BatchEntityRecord> toBatchEntityRecords(List<EntityRecord> result, List<ScheduledTask> scheduledTasks) {
    // Use EntityId - ScheduledTaskType map for quick lookup
    Map<String, ScheduledTaskType> taskTypeMap =
        scheduledTasks.stream()
            .collect(Collectors.toMap(ScheduledTask::getEntityId, ScheduledTask::getUpdateType));
    
    return result.stream()
            .map(rec -> new BatchEntityRecord(rec, taskTypeMap.get(rec.getEntityId())))
            .collect(Collectors.toList());
    
  }
}
