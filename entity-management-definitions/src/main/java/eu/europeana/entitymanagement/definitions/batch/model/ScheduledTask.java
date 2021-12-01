package eu.europeana.entitymanagement.definitions.batch.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Indexes;
import eu.europeana.entitymanagement.definitions.batch.EMBatchConstants;
import java.time.Instant;
import org.bson.types.ObjectId;

@Entity("ScheduledTasks")
@Indexes({
  @Index(
      fields = {
        @Field(value = EMBatchConstants.CREATED),
        @Field(EMBatchConstants.UPDATE_TYPE),
        @Field(EMBatchConstants.HAS_BEEN_PROCESSED)
      }),
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
