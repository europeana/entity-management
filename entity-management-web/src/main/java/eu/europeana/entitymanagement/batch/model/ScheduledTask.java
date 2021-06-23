package eu.europeana.entitymanagement.batch.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity("ScheduledTasks")
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
        private Instant lastModified;
    private BatchUpdateType updateType;

    private ScheduledTask(){
        // private constructor
    }

    public ScheduledTask(String entityId, BatchUpdateType updateType, Instant lastModified) {
        this.entityId = entityId;
        this.updateType = updateType;
        this.lastModified = lastModified;
    }


    public String getEntityId() {
        return entityId;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getLastModified() {
        return lastModified;
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
