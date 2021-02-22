package eu.europeana.entitymanagement.batch.entity;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.item.ExecutionContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AbstractExecutionContextEntity {



    public static ExecutionContext fromEntity(JobExecutionContextEntity entity, ExecutionContextSerializer serializer) {
        ExecutionContext executionContext = new ExecutionContext();

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


    public static  <T extends AbstractExecutionContextEntity> JobExecutionContextEntity toEntity(Class<T> type, Long executionId, String serializedContext) {
        if(type == JobExecutionContextEntity.class){
            return new JobExecutionContextEntity(executionId, serializedContext);
        } else
            return new StepExecutionContextEntity(executionId, serializedContext);


    }

}
