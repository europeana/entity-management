package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.batch.entity.JobExecutionEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.NoSuchObjectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static eu.europeana.entitymanagement.batch.BatchConstants.*;

@Repository
public class JobExecutionRepository extends AbstractRepository implements JobExecutionDao {

    @Autowired
    private Datastore datastore;


    @Override
    public void saveJobExecution(JobExecution jobExecution) {
        validateJobExecution(jobExecution);
        jobExecution.incrementVersion();

        long id = generateSequence(JobExecution.class.getSimpleName());
        save(jobExecution, id);
    }

    private void save(JobExecution jobExecution, long id) {
        jobExecution.setId(id);
        JobExecutionEntity jobExecutionEntity = JobExecutionEntity.toEntity(jobExecution);
        getDataStore().save(jobExecutionEntity);
    }


    private JobExecutionEntity findJobExecutionEntity(long jobExecutionId) {
        return getDataStore().find(JobExecutionEntity.class)
                .filter(eq(JOB_EXECUTION_ID_KEY, jobExecutionId))
                .first();
    }

    @Override
    public synchronized void updateJobExecution(JobExecution jobExecution) {
        validateJobExecution(jobExecution);

        Long jobExecutionId = jobExecution.getId();
        Assert.notNull(jobExecutionId, "JobExecution ID cannot be null. JobExecution must be saved before it can be updated");
        Assert.notNull(jobExecution.getVersion(), "JobExecution version cannot be null. JobExecution must be saved before it can be updated");

        int nextVersion = jobExecution.getVersion() + 1;

        if (findJobExecutionEntity(jobExecutionId) == null) {
            throw new NoSuchObjectException(String.format("Invalid JobExecution, ID %s not found.", jobExecutionId));
        }

        getDataStore().find(JobExecutionEntity.class)
                .filter(
                        eq(JOB_EXECUTION_ID_KEY, jobExecutionId),
                        eq(VERSION_KEY, jobExecution.getVersion())
                )
                .update(
                        UpdateOperators.set(JOB_EXECUTION_ID_KEY, jobExecutionId),
                        UpdateOperators.set(VERSION_KEY, nextVersion),
                        UpdateOperators.set(JOB_INSTANCE_ID_KEY, jobExecution.getJobInstance()),
                        UpdateOperators.set(START_TIME_KEY, jobExecution.getStartTime()),
                        UpdateOperators.set(END_TIME_KEY, jobExecution.getEndTime()),
                        UpdateOperators.set(STATUS_KEY, jobExecution.getStatus().toString()),
                        UpdateOperators.set(EXIT_CODE_KEY, jobExecution.getExitStatus().getExitCode()),
                        UpdateOperators.set(EXIT_MESSAGE_KEY, jobExecution.getExitStatus().getExitDescription()),
                        UpdateOperators.set(CREATE_TIME_KEY, jobExecution.getCreateTime()),
                        UpdateOperators.set(LAST_UPDATED_KEY, jobExecution.getLastUpdated())
                ).execute();
    }

    @Override
    public List<JobExecution> findJobExecutions(JobInstance jobInstance) {
        return null;
    }

    @Override
    public JobExecution getLastJobExecution(JobInstance jobInstance) {
        return null;
    }

    @Override
    public Set<JobExecution> findRunningJobExecutions(String s) {
        return null;
    }

    @Override
    public JobExecution getJobExecution(Long jobExecutionId) {
        return JobExecutionEntity.fromEntity(findJobExecutionEntity(jobExecutionId));
    }

    @Override
    public void synchronizeStatus(JobExecution jobExecution) {

    }

    private void validateJobExecution(JobExecution jobExecution) {
        Assert.notNull(jobExecution, "JobExecution cannot be null.");
        Assert.notNull(jobExecution.getJobId(), "JobExecution Job-Id cannot be null.");
        Assert.notNull(jobExecution.getStatus(), "JobExecution status cannot be null.");
        Assert.notNull(jobExecution.getCreateTime(), "JobExecution create time cannot be null");
    }

    @Override
    Datastore getDataStore() {
        return this.datastore;
    }
}
