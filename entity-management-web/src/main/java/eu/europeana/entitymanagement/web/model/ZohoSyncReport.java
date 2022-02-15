package eu.europeana.entitymanagement.web.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.*;

@JsonPropertyOrder({LAST_SYNC_DATE, CREATED, UPDATED, DEPRECATED, DELETED, EXECUTION_STATUS,
    SKIPPED_DUPPLICATES})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ZohoSyncReport {

  final Date lastSyncDate;
  long created = 0l;
  long updated = 0l;
  long deprecated = 0l;
  long deleted = 0l;
  private String executionStatus;
  Map<String, String> skippedDupplicates;

  private Throwable error;
  public static final String STATUS_COMPLETED = "completed";
  public static final String STATUS_INTERUPTED = "interuppted";


  public ZohoSyncReport(Date lastSyncDate) {
    this.lastSyncDate = lastSyncDate;
  }

  @SuppressWarnings("unused")
  private ZohoSyncReport() {
    this(new Date(0));
  }

  
  @JsonProperty(CREATED)
  public long getCreated() {
    return created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public void increaseCreated(long created) {
    this.created += created;
  }

  @JsonProperty(UPDATED)
  public long getUpdated() {
    return updated;
  }

  public void setUpdated(long updated) {
    this.updated = updated;
  }

  public void increaseUpdated(long updated) {
    this.updated += updated;
  }

  @JsonProperty(DEPRECATED)
  public long getDeprecated() {
    return deprecated;
  }

  public void setDeprecated(long deprecated) {
    this.deprecated = deprecated;
  }

  public void increaseDeprecated(long deprecated) {
    this.deprecated += deprecated;
  }

  @JsonProperty(DELETED)
  public long getDeleted() {
    return deleted;
  }

  public void setDeleted(long deleted) {
    this.deleted = deleted;
  }

  public void increaseDeleted(long deleted) {
    this.deleted += deleted;
  }

  @JsonProperty(EXECUTION_STATUS)
  public String getExecutionStatus() {
    return executionStatus;
  }

  public void setExecutionStatus(String executionStatus) {
    this.executionStatus = executionStatus;
  }

  @Override
  public String toString() {
    return String.format(
        "lastSyncDate: %s,%n created: %d,%n updated: %d,%n deprecated: %d,%n deleted: %d,%n executionStatus: %s",
        DateUtils.convertDateToStr(getLastSyncDate()), getCreated(), getUpdated(), getDeprecated(),
        getDeleted(), getExecutionStatus());
  }

  public Throwable getError() {
    return error;
  }

  void setError(Throwable error) {
    this.error = error;
  }

  @JsonIgnore
  public boolean isExecutionIntrerrupted() {
    return STATUS_INTERUPTED.equals(getExecutionStatus());
  }

  public void updateExecutionStatus(Throwable error) {
    if (error != null) {
      setError(error);
      setExecutionStatus(executionStatus);
    }
  }

  @JsonProperty(SKIPPED_DUPPLICATES)
  public Map<String, String> getSkippedDupplicates() {
    return skippedDupplicates;
  }

  public void addSkippedDupplicate(String creationRequestId, String existingEntity) {
    if (skippedDupplicates == null) {
      skippedDupplicates = new HashMap<String, String>();
    }
    skippedDupplicates.put(creationRequestId, existingEntity);
  }


  @JsonProperty(LAST_SYNC_DATE)
  public Date getLastSyncDate() {
    return lastSyncDate;
  }
}
