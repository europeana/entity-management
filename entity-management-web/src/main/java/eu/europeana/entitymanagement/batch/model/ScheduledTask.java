package eu.europeana.entitymanagement.batch.model;

import static eu.europeana.entitymanagement.batch.EMBatchConstants.*;

import dev.morphia.annotations.*;
import java.time.Instant;
import org.bson.types.ObjectId;

@Entity("ScheduledTasks")
@Indexes({
  @Index(fields = {@Field(CREATED), @Field(UPDATE_TYPE)}),
  // only index records where hasBeenProcessed = true
  @Index(
      options = @IndexOptions(partialFilter = "{" + HAS_BEEN_PROCESSED + " : { $eq : true } }"),
      fields = {@Field(HAS_BEEN_PROCESSED)})
})
public class ScheduledTask {

  @Id private ObjectId dbId;

  @Indexed private String entityId;

  /**
   * created not explicitly set. During upserts, we use the value for modified if record doesn't
   * already exist
   */
  private Instant created;

  private Instant modified;
  private ScheduledTaskType updateType;
  private boolean hasBeenProcessed;

  private ScheduledTask() {
    // private constructor
  }

  public ScheduledTask(
      String entityId, ScheduledTaskType updateType, Instant modified, boolean hasBeenProcessed) {
    this.entityId = entityId;
    this.updateType = updateType;
    this.modified = modified;
    this.hasBeenProcessed = hasBeenProcessed;
  }

  public String getEntityId() {
    return entityId;
  }

  public Instant getCreated() {
    return created;
  }

  public Instant getModified() {
    return modified;
  }

  public ScheduledTaskType getUpdateType() {
    return updateType;
  }

  public boolean hasBeenProcessed() {
    return hasBeenProcessed;
  }

  public static class Builder {
    private final String entityId;
    private final ScheduledTaskType updateType;
    private Instant modified;
    private boolean hasBeenProcessed;

    public Builder(String entityId, ScheduledTaskType updateType) {
      this.entityId = entityId;
      this.updateType = updateType;
    }

    public Builder modified(Instant modified) {
      this.modified = modified;
      return this;
    }

    public Builder setProcessed(boolean hasBeenProcessed) {
      this.hasBeenProcessed = hasBeenProcessed;
      return this;
    }

    public ScheduledTask build() {
      return new ScheduledTask(entityId, updateType, modified, hasBeenProcessed);
    }
  }
}
