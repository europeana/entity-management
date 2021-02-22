package eu.europeana.entitymanagement.batch.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.item.ExecutionContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Entity
public class ExecutionContextEntity {
    @Id
    private ObjectId _id;

    private long executionId;

    private String serializedContext;

    private ExecutionContextEntityType type;

    public long getExecutionId() {
        return executionId;
    }

    public String getSerializedContext() {
        return serializedContext;
    }

    public ExecutionContextEntityType getType() {
        return type;
    }

    public ExecutionContextEntity() {
        // default empty constructor
    }

    public ExecutionContextEntity(ExecutionContextEntityType type, long executionId, String serializedContext) {
        this.type = type;
        this.executionId = executionId;
        this.serializedContext = serializedContext;
    }

    public static ExecutionContextEntity toEntity(ExecutionContextEntityType type, Long executionId, String serializedContext) {
        return new ExecutionContextEntity(type, executionId, serializedContext);
    }


    public static ExecutionContext fromEntity(ExecutionContextEntity entity, ExecutionContextSerializer serializer) {
        ExecutionContext executionContext = new ExecutionContext();

        if (entity == null) {
            return executionContext;
        }

        String serializedContext = entity.getSerializedContext();

        // reproduced from JdbcExecutionContextDao (in Spring batch core)
        Map<String, Object> map;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(serializedContext.getBytes(StandardCharsets.ISO_8859_1));
            map = serializer.deserialize(in);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to deserialize the execution context", ioe);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            executionContext.put(entry.getKey(), entry.getValue());
        }
        return executionContext;
    }

}
