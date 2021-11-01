package eu.europeana.entitymanagement.definitions.batch.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import java.time.Instant;
import org.bson.types.ObjectId;

@Entity("FailedTasks")
public class FailedTask {

  @Id private ObjectId dbId;

  @Indexed private String entityId;

  private String errorMessage;
  private String stackTrace;

  private ScheduledTaskType updateType;

  /**
   * FailureCount not explicitly set. Value is updated during Mongo upserts via the $inc operator
   */
  private int failureCount = 1;

  /* Created is not explicitly set on instantiation.
   * During upserts, we use the "modified" value if the record doesn't already exist.
   */
  private Instant created;
  private Instant modified;

  private FailedTask() {
    // default constructor
  }

  public FailedTask(
      String entityId,
      Instant modified,
      String errorMessage,
      String stackTrace,
      ScheduledTaskType updateType) {
    this.entityId = entityId;
    this.modified = modified;
    this.errorMessage = errorMessage;
    this.stackTrace = stackTrace;
    this.updateType = updateType;
  }

  public String getEntityId() {
    return entityId;
  }

  public Instant getCreated() {
    return created;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public Instant getModified() {
    return modified;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public ScheduledTaskType getUpdateType() {
    return updateType;
  }

  public static class Builder {

    private final String entityId;
    private final ScheduledTaskType updateType;

    private Instant modified;
    // default values saved if they're not overwritten
    private String errorMessage = "No error message";
    private String stackTrace = "No stacktrace";

    public Builder(String entityId, ScheduledTaskType updateType) {
      this.entityId = entityId;
      this.updateType = updateType;
    }

    public Builder modified(Instant modified) {
      this.modified = modified;
      return this;
    }

    public Builder message(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public Builder stackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public FailedTask build() {
      return new FailedTask(entityId, modified, errorMessage, stackTrace, updateType);
    }
  }

  @Override
  public String toString() {
    return "FailedTask{"
        + "dbId="
        + dbId
        + ", entityId='"
        + entityId
        + '\''
        + ", created="
        + created
        + ", modified="
        + modified
        + ", errorMessage='"
        + errorMessage
        + '\''
        + ", stackTrace='"
        + stackTrace
        + '\''
        + '}';
  }
}
