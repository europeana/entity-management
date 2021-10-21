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

  // default values saved if they're not overwritten
  private String errorMessage = "No error message";
  private String stackTrace = "No stacktrace";

  /* Created is not explicitly set on instantiation.
   * During upserts, we use the "modified" value if the record doesn't already exist.
   */
  private Instant created;
  private Instant modified;

  private FailedTask() {
    // default constructor
  }

  public FailedTask(String entityId, Instant modified, String errorMessage, String stackTrace) {
    this.entityId = entityId;
    this.modified = modified;
    this.errorMessage = errorMessage;
    this.stackTrace = stackTrace;
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

  public static class Builder {

    private final String entityId;

    private Instant modified;
    private String errorMessage;
    private String stackTrace;

    public Builder(String entityId) {
      this.entityId = entityId;
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
      return new FailedTask(entityId, modified, errorMessage, stackTrace);
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
