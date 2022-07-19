package eu.europeana.entitymanagement.definitions.batch.model;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public class BatchEntityRecord {

  private EntityRecord entityRecord;
  private ScheduledTaskType scheduledTaskType;
  
  public BatchEntityRecord(EntityRecord entityRecord, ScheduledTaskType scheduledTaskType) {
    this.entityRecord = entityRecord;
    this.scheduledTaskType = scheduledTaskType;
  }
  public EntityRecord getEntityRecord() {
    return entityRecord;
  }
  public ScheduledTaskType getScheduledTaskType() {
    return scheduledTaskType;
  }
  
}
