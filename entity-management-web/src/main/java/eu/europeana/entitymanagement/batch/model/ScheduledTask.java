package eu.europeana.entitymanagement.batch.model;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.time.Instant;

import static eu.europeana.entitymanagement.batch.EMBatchConstants.CREATED;
import static eu.europeana.entitymanagement.batch.EMBatchConstants.UPDATE_TYPE;


@Entity("ScheduledTasks")
@Indexes({
        @Index(fields = {@Field(CREATED), @Field(UPDATE_TYPE)})
})
public class ScheduledTask {

    @Id
    private ObjectId dbId;

    @Indexed
    private String entityId;

    /**
     * created not explicitly set.
     * During upserts, we use the value for modified if record doesn't already exist
     */
    private Instant created;
    private Instant modified;
    private BatchUpdateType updateType;

    private ScheduledTask() {
        // private constructor
    }

    public ScheduledTask(String entityId, BatchUpdateType updateType, Instant modified) {
        this.entityId = entityId;
        this.updateType = updateType;
        this.modified = modified;
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

    public BatchUpdateType getUpdateType() {
        return updateType;
    }

    public static class Builder {
        private final String entityId;
        private final BatchUpdateType updateType;
        private Instant modified;

        public Builder(String entityId, BatchUpdateType updateType) {
            this.entityId = entityId;
            this.updateType = updateType;
        }

        public Builder modified(Instant modified) {
            this.modified = modified;
            return this;
        }

        public ScheduledTask build() {
            return new ScheduledTask(entityId, updateType, modified);
        }
    }
}
