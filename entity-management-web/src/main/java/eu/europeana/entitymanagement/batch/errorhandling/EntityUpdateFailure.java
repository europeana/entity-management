package eu.europeana.entitymanagement.batch.errorhandling;


import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import java.time.Instant;
import org.bson.types.ObjectId;

@Entity("FailedTasks")
public class EntityUpdateFailure {

  @Id
  private ObjectId dbId;

  @Indexed
  private String entityId;

  private EntityUpdateFailure() {
    // default constructor
  }

  public EntityUpdateFailure(String entityId, Instant created, String errorMessage,
      String stackTrace) {
    this.entityId = entityId;
    this.created = created;
    this.errorMessage = errorMessage;
    this.stackTrace = stackTrace;
  }

  private Instant created;
  private String errorMessage;
  private String stackTrace;


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


  public static class Builder {

    private String entityId;

    private Instant created;
    private String errorMessage;
    private String stackTrace;

    public Builder(String entityId){
      this.entityId = entityId;
    }


    public Builder created(Instant created) {
      this.created = created;
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

    public EntityUpdateFailure build() {
      return new EntityUpdateFailure(entityId, created, errorMessage, stackTrace);
    }
  }

  @Override
  public String toString() {
    return "EntityUpdateFailure{" +
        "dbId=" + dbId +
        ", entityId='" + entityId + '\'' +
        ", created=" + created +
        ", errorMessage='" + errorMessage + '\'' +
        ", stackTrace='" + stackTrace + '\'' +
        '}';
  }
}
