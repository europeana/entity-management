package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.batch.entity.AbstractExecutionContextEntity;
import eu.europeana.entitymanagement.batch.entity.JobExecutionContextEntity;
import eu.europeana.entitymanagement.batch.entity.JobExecutionEntity;
import eu.europeana.entitymanagement.batch.entity.StepExecutionEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static eu.europeana.entitymanagement.batch.BatchConstants.BATCH_INSERT_OPTIONS;

@Repository
public class ExecutionContextRepository extends AbstractRepository implements ExecutionContextDao {

    private ExecutionContextSerializer serializer = new DefaultExecutionContextSerializer();

    @Autowired
    private Datastore datastore;


    @Override
    public ExecutionContext getExecutionContext(JobExecution jobExecution) {
        return null;
    }

    @Override
    public ExecutionContext getExecutionContext(StepExecution stepExecution) {
        Long executionId = stepExecution.getId();
        Assert.notNull(executionId, "ExecutionId must not be null.");


    }

    @Override
    public void saveExecutionContext(JobExecution jobExecution) {
        Long executionId = jobExecution.getId();
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        persistExecutionContext(JobExecutionEntity.class, executionId, executionContext);
    }


    @Override
    public void saveExecutionContext(StepExecution stepExecution) {
        Long executionId = stepExecution.getId();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        persistExecutionContext(StepExecutionEntity.class, executionId, executionContext);
    }

    @Override
    public void saveExecutionContexts(Collection<StepExecution> stepExecutions) {
        Assert.notNull(stepExecutions, "Attempt to save an null collection of step executions");
        List<JobExecutionContextEntity> ctxEntities = new ArrayList<>(stepExecutions.size());
        for (StepExecution stepExecution : stepExecutions) {
            Long executionId = stepExecution.getId();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            Assert.notNull(executionId, "ExecutionId must not be null.");
            Assert.notNull(executionContext, "The ExecutionContext must not be null.");

            ctxEntities.add(new JobExecutionContextEntity(executionId, serializeContext(executionContext)));
        }

        getDataStore().save(ctxEntities, BATCH_INSERT_OPTIONS);


    }

    @Override
    public void updateExecutionContext(final JobExecution jobExecution) {
        Long executionId = jobExecution.getId();
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        clazz(JobExecutionEntity.class, executionId, executionContext);
    }

    @Override
    public void updateExecutionContext(final StepExecution stepExecution) {
        synchronized (stepExecution) {
            Long executionId = stepExecution.getId();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            clazz(JobExecutionEntity.class, executionId, executionContext);
        }
    }


    @Override
    Datastore getDataStore() {
        return this.datastore;
    }


    private JobExecutionContextEntity findStepExecutionContext(long stepExecutionId){

    }

    private <T extends AbstractExecutionContextEntity> void persistExecutionContext(Class<T> type, Long executionId, ExecutionContext executionContext) {
        Assert.notNull(executionId, "ExecutionId must not be null.");
        Assert.notNull(executionContext, "The ExecutionContext must not be null.");

        String serializedContext = serializeContext(executionContext);


        getDataStore().save(AbstractExecutionContextEntity.toEntity(type, executionId, serializedContext));
    }

    /**
     * Reproduced from {@link org.springframework.batch.core.repository.dao.JdbcExecutionContextDao}
     */
    private String serializeContext(ExecutionContext ctx) {
        Map<String, Object> m = new HashMap<>();
        for (Map.Entry<String, Object> me : ctx.entrySet()) {
            m.put(me.getKey(), me.getValue());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String results = "";

        try {
            serializer.serialize(m, out);
            results = out.toString(StandardCharsets.ISO_8859_1);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not serialize the execution context", ioe);
        }

        return results;
    }

}
