package eu.europeana.entitymanagement.web.model;

import static eu.europeana.entitymanagement.web.model.ZohoSyncReportFields.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.types.ObjectId;

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
@dev.morphia.annotations.Entity("ZohoSyncReport")
public class ZohoSyncReport {

  @Id @JsonIgnore private ObjectId dbId;

  @Indexed(options = @IndexOptions(unique = true))
  Date startDate;
  long createdItems;
  long enabledItems;
  long updatedItems;
  long deprecatedItems;
  long deletedItems;
  private List<FailedOperation> failed;

  public ZohoSyncReport(Date startDate) {
    this.startDate = startDate;
  }

  public ZohoSyncReport() {
    //implicit constructor used by for morphia
  }

  @JsonProperty(NEW)
  public long getCreatedItems() {
    return createdItems;
  }

  public void setCreatedItems(long created) {
    this.createdItems = created;
  }

  public void increaseCreated(long created) {
    this.createdItems += created;
  }

  @JsonProperty(ENABLED)
  public long getEnabledItems() {
    return enabledItems;
  }

  public void setEnabledItems(long enabled) {
    this.enabledItems = enabled;
  }

  /**
   * increase the enabled counter
   *
   * @param enabled value used for increasing counter
   */
  public void increaseEnabled(long enabled) {
    this.enabledItems += enabled;
  }

  @JsonProperty(UPDATED)
  public long getUpdatedItems() {
    return updatedItems;
  }

  public void setUpdatedItems(long updated) {
    this.updatedItems = updated;
  }

  public void increaseUpdated(long updated) {
    this.updatedItems += updated;
  }

  @JsonProperty(DEPRECATED)
  public long getDeprecatedItems() {
    return deprecatedItems;
  }

  public void setDeprecatedItems(long deprecated) {
    this.deprecatedItems = deprecated;
  }

  public void increaseDeprecated(long deprecated) {
    this.deprecatedItems += deprecated;
  }

  @JsonProperty(DELETED)
  public long getDeletedItems() {
    return deletedItems;
  }

  public void setDeletedItems(long deleted) {
    this.deletedItems = deleted;
  }

  public void increaseDeleted(long deleted) {
    this.deletedItems += deleted;
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
        DateUtils.convertDateToStr(getStartDate()),
        getCreatedItems(),
        getEnabledItems(),
        getUpdatedItems(),
        getDeprecatedItems(),
        getDeletedItems(),
        getExecutionStatus());
  }

  /**
   * getter method
   *
   * @return the date and time when the synchronization was run
   */
  @JsonProperty(LAST_SYNC_DATE)
  public Date getStartDate() {
    return startDate;
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

  public ObjectId getDbId() {
    return dbId;
  }

  public void setDbId(ObjectId dbId) {
    this.dbId = dbId;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public void setFailed(List<FailedOperation> failed) {
    this.failed = failed;
  }
}
