package eu.europeana.entitymanagement.web.model;

import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.DELETED;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.DEPRECATED;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.EXECUTION_STATUS;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.FAILED;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.LAST_SYNC_DATE;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.NEW;
import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.UPDATED;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api.commons.definitions.utils.DateUtils;

@JsonPropertyOrder({
  LAST_SYNC_DATE,
  EXECUTION_STATUS,
  NEW,
  UPDATED,
  DEPRECATED,
  DELETED,
  FAILED
})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ZohoSyncReport {

  final Date lastSyncDate;
  long created = 0l;
  long updated = 0l;
  long deprecated = 0l;
  long deleted = 0l;
  private List<FailedOperation> failed;

  public static final String STATUS_COMPLETED = "completed";
  public static final String STATUS_INCOMPLETE = "incomplete";

  public ZohoSyncReport(Date lastSyncDate) {
    this.lastSyncDate = lastSyncDate;
  }

  @SuppressWarnings("unused")
  private ZohoSyncReport() {
    this(new Date(0));
  }

  @JsonProperty(NEW)
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
    if(getFailed() == null || getFailed().isEmpty()) {
      return STATUS_COMPLETED;
    }
    return STATUS_INCOMPLETE;
  }

  @Override
  public String toString() {
    return String.format(
        "lastSyncDate: %s,%n created: %d,%n updated: %d,%n deprecated: %d,%n deleted: %d,%n executionStatus: %s",
        DateUtils.convertDateToStr(getLastSyncDate()),
        getCreated(),
        getUpdated(),
        getDeprecated(),
        getDeleted(),
        getExecutionStatus());
  }

  @JsonProperty(LAST_SYNC_DATE)
  public Date getLastSyncDate() {
    return lastSyncDate;
  }

  private void addFailedOperation(FailedOperation operation) {
    if(failed == null) {
      failed = new ArrayList<FailedOperation>();
    }
    failed.add(operation);
  }

  public void addFailedOperation(String id, String error, Throwable th) {
    addFailedOperation(id, error, th.getMessage(), th);
  }
  
  public void addFailedOperation(String id, String error, String message, Throwable th) {
    String trace = ExceptionUtils.getStackTrace(th); 
    if(error == null) {
      error = th.getClass().getSimpleName();
    }
    FailedOperation failedOperation = new FailedOperation(id, error, message, trace); 
    addFailedOperation(failedOperation);
  }
  
  
  @JsonProperty(FAILED)
  public List<FailedOperation> getFailed() {
    return failed;
  }

  public void setFailed(List<FailedOperation> failed) {
    this.failed = failed;
  }
}
