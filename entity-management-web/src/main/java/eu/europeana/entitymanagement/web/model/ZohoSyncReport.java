package eu.europeana.entitymanagement.web.model;

import java.util.HashMap;
import java.util.Map;

public class ZohoSyncReport {

  long created = 0l;
  long updated = 0l;
  long deprecated = 0l;
  long deleted = 0l;
  Map<String, String> skippedDupplicates;
  
  private String executionStatus;
  private Throwable error;
  public static final String STATUS_COMPLETED = "completed";
  public static final String STATUS_INTERUPTED = "interuppted";
  
  
  public long getCreated() {
    return created;
  }
  public void setCreated(long created) {
    this.created = created;
  }
  
  public void increaseCreated(long created) {
    this.created += created;
  }
  
  public long getUpdated() {
    return updated;
  }
  public void setUpdated(long updated) {
    this.updated = updated;
  }
  public void increaseUpdated(long updated) {
    this.updated += updated;
  }
  public long getDeprecated() {
    return deprecated;
  }
  public void setDeprecated(long deprecated) {
    this.deprecated = deprecated;
  }
  public void increaseDeprecated(long deprecated) {
    this.deprecated += deprecated;
  }
  public long getDeleted() {
    return deleted;
  }
  public void setDeleted(long deleted) {
    this.deleted = deleted;
  }
  public void increaseDeleted(long deleted) {
    this.deleted += deleted;
  }
  public String getExecutionStatus() {
    return executionStatus;
  }
  public void setExecutionStatus(String executionStatus) {
    this.executionStatus = executionStatus;
  }
  
  @Override
  public String toString() {
    return String.format("created: %d,\n updated: %d,\n deprecated: %d,\n deleted: %d,\n executionStatus: %d", getCreated(), getUpdated(), getDeprecated(), getDeleted(), getExecutionStatus());
  }
  public Throwable getError() {
    return error;
  }
  
  void setError(Throwable error) {
    this.error = error;
  }
  
  public boolean isExecutionIntrerrupted() {
    return STATUS_INTERUPTED.equals(getExecutionStatus());
  }
  
  public void updateExecutionStatus(Throwable error) {
    if(error != null) {
      setError(error);
      setExecutionStatus(executionStatus);
    }
  }
  public Map<String, String> getSkippedDupplicate() {
    return skippedDupplicates;
  }
  public void addSkippedDupplicate(String creationRequestId, String existingEntity) {
    if(skippedDupplicates == null) {
      skippedDupplicates = new HashMap<String, String>();
    }
    skippedDupplicates.put(creationRequestId, existingEntity);
  }
}
