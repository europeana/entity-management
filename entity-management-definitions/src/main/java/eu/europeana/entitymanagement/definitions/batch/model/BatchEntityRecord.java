package eu.europeana.entitymanagement.definitions.batch.model;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public class BatchEntityRecord {

  private final EntityRecord entityRecord;
  private final TaskType scheduledTaskType;

  public BatchEntityRecord(EntityRecord entityRecord, TaskType scheduledTaskType) {
    this.entityRecord = entityRecord;
    this.scheduledTaskType = scheduledTaskType;
  }

  public EntityRecord getEntityRecord() {
    return entityRecord;
  }

  public TaskType getScheduledTaskType() {
    return scheduledTaskType;
  }
}
