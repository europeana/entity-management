package eu.europeana.entitymanagement.batch.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity
public class JobExecutionContextEntity extends AbstractExecutionContextEntity{
    @Id
    private ObjectId _id;

    private long jobExecutionId;

    private String serializedContext;

    public long getJobExecutionId() {
        return jobExecutionId;
    }

    public String getSerializedContext() {
        return serializedContext;
    }

    public JobExecutionContextEntity() {
        // default empty constructor
    }

    public JobExecutionContextEntity(long jobExecutionId, String serializedContext) {
        this.jobExecutionId = jobExecutionId;
        this.serializedContext = serializedContext;
    }

    public static JobExecutionContextEntity toEntity(Long executionId, String serializedContext) {
        return new JobExecutionContextEntity(executionId, serializedContext);
    }




}
