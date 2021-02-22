package eu.europeana.entitymanagement.batch.repository;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.batch.entity.StepExecutionEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static eu.europeana.entitymanagement.batch.BatchConstants.*;

public class StepExecutionRepository extends AbstractRepository implements StepExecutionDao {

    @Autowired
    private Datastore datastore;


    @Override
    public void saveStepExecution(StepExecution stepExecution) {
        prepareForSaving(stepExecution);
        StepExecutionEntity stepExecutionEntity = StepExecutionEntity.toEntity(stepExecution);
        getDataStore().save(stepExecutionEntity);
    }


    @Override
    public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
        Assert.notNull(stepExecutions, "Attempt to save an null collect of step executions");

        // first generate IDs, increment versions, etc
        // TODO: ID generation requires DB lookup for each stepExecution. See if there's an efficient way of doing this
        for (StepExecution stepExecution : stepExecutions) {
            prepareForSaving(stepExecution);
        }

        List<StepExecutionEntity> entities = stepExecutions
                .stream()
                .map(StepExecutionEntity::toEntity)
                .collect(Collectors.toList());

        getDataStore().save(entities, BATCH_INSERT_OPTIONS);
    }

    @Override
    public void updateStepExecution(StepExecution stepExecution) {
        validateStepExecution(stepExecution);
        Assert.notNull(stepExecution.getId(), "StepExecution Id cannot be null. StepExecution must saved"
                + " before it can be updated.");

        synchronized (stepExecution) {
            int nextVersion = stepExecution.getVersion() + 1;
            stepExecution.setVersion(nextVersion);

            UpdateResult result = getDataStore().find(StepExecutionEntity.class)
                    .filter(
                            eq(STEP_EXECUTION_ID_KEY, stepExecution.getId()),
                            eq(VERSION_KEY, stepExecution.getVersion())
                    )
                    .update(
                            UpdateOperators.set(STEP_EXECUTION_ID_KEY, stepExecution.getId()),
                            UpdateOperators.set(STEP_NAME_KEY, stepExecution.getStepName()),
                            UpdateOperators.set(JOB_EXECUTION_ID_KEY, stepExecution.getJobExecutionId()),
                            UpdateOperators.set(START_TIME_KEY, stepExecution.getStartTime()),
                            UpdateOperators.set(END_TIME_KEY, stepExecution.getEndTime()),
                            UpdateOperators.set(STATUS_KEY, stepExecution.getStatus().toString()),
                            UpdateOperators.set(COMMIT_COUNT_KEY, stepExecution.getCommitCount()),
                            UpdateOperators.set(READ_COUNT_KEY, stepExecution.getReadCount()),
                            UpdateOperators.set(FILTER_COUT_KEY, stepExecution.getFilterCount()),
                            UpdateOperators.set(WRITE_COUNT_KEY, stepExecution.getWriteCount()),
                            UpdateOperators.set(EXIT_CODE_KEY, stepExecution.getExitStatus().getExitCode()),
                            UpdateOperators.set(EXIT_MESSAGE_KEY, stepExecution.getExitStatus().getExitDescription()),
                            UpdateOperators.set(READ_SKIP_COUNT_KEY, stepExecution.getReadSkipCount()),
                            UpdateOperators.set(WRITE_SKIP_COUNT_KEY, stepExecution.getWriteSkipCount()),
                            UpdateOperators.set(PROCESS_SKIP_COUT_KEY, stepExecution.getProcessSkipCount()),
                            UpdateOperators.set(ROLLBACK_COUNT_KEY, stepExecution.getRollbackCount()),
                            UpdateOperators.set(LAST_UPDATED_KEY, stepExecution.getLastUpdated())
                    ).execute();


            // Avoid concurrent modifications
            if (result.getModifiedCount() == 0) {
                int currentVersion = getStepExecutionVersion(stepExecution.getId());
                throw new OptimisticLockingFailureException("Attempt to update step execution id="
                        + stepExecution.getId() + " with wrong version (" + stepExecution.getVersion()
                        + "), where current version is " + currentVersion);
            }

        }
    }


    @Nullable
    @Override
    public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
        List<StepExecutionEntity> instances = getStepExecutions(jobExecution.getId(), stepExecutionId);
        Assert.state(instances.size() <= 1,
                "There can be at most one step execution with given name for single job execution");

        return StepExecutionEntity.fromEntity(instances.get(0), jobExecution);
    }

    @Override
    public void addStepExecutions(JobExecution jobExecution) {
        List<StepExecutionEntity> results = getStepExecutions(jobExecution.getId());

        for (StepExecutionEntity entity : results) {
            // this calls the constructor of StepExecution, which adds it to the jobExecution
            StepExecutionEntity.fromEntity(entity, jobExecution);
        }
    }


    @Override
    Datastore getDataStore() {
        return this.datastore;
    }

    private void validateStepExecution(StepExecution stepExecution) {
        Assert.notNull(stepExecution, "StepExecution cannot be null.");
        Assert.notNull(stepExecution.getStepName(), "StepExecution step name cannot be null.");
        Assert.notNull(stepExecution.getStartTime(), "StepExecution start time cannot be null.");
        Assert.notNull(stepExecution.getStatus(), "StepExecution status cannot be null.");
    }

    private void prepareForSaving(StepExecution stepExecution) {
        Assert.isNull(stepExecution.getId(),
                "to-be-saved (not updated) StepExecution can't already have an id assigned");
        Assert.isNull(stepExecution.getVersion(),
                "to-be-saved (not updated) StepExecution can't already have a version assigned");

        validateStepExecution(stepExecution);

        stepExecution.setId(generateSequence(StepExecutionEntity.class.getSimpleName()));
        stepExecution.incrementVersion();
    }

    /**
     * Gets the StepExecution version saved in the database.
     * <p>
     * TODO: similar to JobExecutionRepository.getJobExecutionVersion(). Refactor
     *
     * @param stepExecutionId
     * @return
     */
    private int getStepExecutionVersion(long stepExecutionId) {
        return
                getDataStore().find(StepExecutionEntity.class)
                        .filter(eq(JOB_EXECUTION_ID_KEY, stepExecutionId))
                        .iterator(new FindOptions()
                                .projection().include(VERSION_KEY)
                                .limit(1))
                        .next().getVersion();
    }

    private List<StepExecutionEntity> getStepExecutions(long jobExecutionId, long stepExecutionId) {
        return getDataStore().find(StepExecutionEntity.class)
                .filter(
                        eq(STEP_EXECUTION_ID_KEY, stepExecutionId),
                        eq(JOB_EXECUTION_ID_KEY, jobExecutionId)
                ).iterator().toList();
    }

    private List<StepExecutionEntity> getStepExecutions(long jobExecutionId) {
        return getDataStore().find(StepExecutionEntity.class)
                .filter(
                        eq(JOB_EXECUTION_ID_KEY, jobExecutionId)
                ).iterator(new FindOptions().sort(ascending(STEP_EXECUTION_ID_KEY))).toList();
    }

}
