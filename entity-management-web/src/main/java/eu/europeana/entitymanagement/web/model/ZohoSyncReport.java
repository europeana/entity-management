package eu.europeana.entitymanagement.web.model;

import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;

/** class used for serialization of zoho sync report */
@JsonPropertyOrder({
  LAST_SYNC_DATE,
  EXECUTION_STATUS,
  NEW,
  ENABLED,
  UPDATED,
  DEPRECATED,
  DELETED,
  FAILED
})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ZohoSyncReport {

  public static final String STATUS_COMPLETED = "completed";
  public static final String STATUS_INCOMPLETE = "incomplete";

  final Date lastSyncDate;
  long created;
  long enabled;
  long updated;
  long deprecated;
  long deleted;
  private List<FailedOperation> failed;

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

  @JsonProperty(ENABLED)
  public long getEnabled() {
    return enabled;
  }

  public void setEnabled(long enabled) {
    this.enabled = enabled;
  }

  /**
   * increase the enabled counter
   *
   * @param enabled value used for increasing counter
   */
  public void increaseEnabled(long enabled) {
    this.enabled += enabled;
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
    if (getFailed() == null || getFailed().isEmpty()) {
      return STATUS_COMPLETED;
    }
    return STATUS_INCOMPLETE;
  }

  @Override
  public String toString() {
    return String.format(
        "lastSyncDate: %s,%n created: %d,%n enabled: %d, %n updated: %d,%n deprecated: %d,%n deleted: %d,%n executionStatus: %s",
        DateUtils.convertDateToStr(getLastSyncDate()),
        getCreated(),
        getEnabled(),
        getUpdated(),
        getDeprecated(),
        getDeleted(),
        getExecutionStatus());
  }

  @JsonProperty(LAST_SYNC_DATE)
  /**
   * getter method
   *
   * @return the date and time when the synchronization was run
   */
  public Date getLastSyncDate() {
    return lastSyncDate;
  }

  private void addFailedOperation(FailedOperation operation) {
    if (failed == null) {
      failed = new ArrayList<>();
    }
    failed.add(operation);
  }

  /**
   * Utility method for registering a FailedOperation
   *
   * @param id - zoho organization id
   * @param error - the label of the error
   * @param th - the exception indicating the source of the processing error
   */
  public void addFailedOperation(String id, String error, Throwable th) {
    addFailedOperation(id, error, th.getMessage(), th);
  }

  /**
   * Utility method for registering a FailedOperation
   *
   * @param id - zoho organization id
   * @param error - the label of the error
   * @param message - the message indicating the failed operations
   * @param th - the exception indicating the source of the processing error
   */
  public void addFailedOperation(String id, String error, String message, Throwable th) {
    String trace = (th == null) ? null : ExceptionUtils.getStackTrace(th);
    if (error == null && th != null) {
      error = th.getClass().getSimpleName();
    }
    FailedOperation failedOperation = new FailedOperation(id, error, message, trace);
    addFailedOperation(failedOperation);
  }

  @JsonProperty(FAILED)
  /**
   * getter method
   *
   * @return the failed operations
   */
  public List<FailedOperation> getFailed() {
    return failed;
  }
}
