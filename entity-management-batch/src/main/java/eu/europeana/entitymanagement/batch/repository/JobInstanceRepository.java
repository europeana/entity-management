package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.batch.entity.JobInstanceEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.experimental.filters.Filters.*;
import static eu.europeana.entitymanagement.batch.BatchConstants.*;

public class JobInstanceRepository extends AbstractRepository implements JobInstanceDao {

    @Autowired
    private Datastore datastore;

    @Override
    public JobInstance createJobInstance(final String jobName, final JobParameters jobParameters) {
        Assert.notNull(jobName, "Job name must not be null.");
        Assert.notNull(jobParameters, "JobParameters must not be null.");

        Assert.state(getJobInstance(jobName, jobParameters) == null,
                "JobInstance must not already exist");

        long jobId = generateSequence(JobInstanceEntity.class.getSimpleName());

        JobInstance jobInstance = new JobInstance(jobId, jobName);
        jobInstance.incrementVersion();

        JobInstanceEntity jobInstanceEntity = JobInstanceEntity.toEntity(jobInstance, jobParameters);
        getDataStore().save(jobInstanceEntity);

        return jobInstance;
    }

    @Nullable
    @Override
    public JobInstance getJobInstance(final String jobName, final JobParameters jobParameters) {
        Assert.notNull(jobName, "Job name must not be null.");
        Assert.notNull(jobParameters, "JobParameters must not be null.");

        String jobKey = JOB_KEY_GENERATOR.generateKey(jobParameters);
        List<JobInstanceEntity> instances = getJobInstanceFromDb(jobName, jobKey);

        Assert.state(instances.size() == 1, "instance count must be 1 but was " + instances.size());
        return JobInstanceEntity.fromEntity(instances.get(0));
    }

    @Nullable
    @Override
    public JobInstance getJobInstance(Long instanceId) {
        return JobInstanceEntity.fromEntity(getJobFromId(instanceId));
    }

    @Override
    public JobInstance getJobInstance(JobExecution jobExecution) {
        // get jobInstanceId for execution
        long instanceId = getJobExecutionInstanceId(jobExecution.getId());
        return getJobInstance(instanceId);
    }

    /**
     * Fetch the last job instances with the provided name, sorted backwards by
     * primary key.
     *
     * @param jobName the job name
     * @param start   the start index of the instances to return
     * @param count   the maximum number of objects to return
     * @return the job instances with this name or empty if none
     */
    @Override
    public List<JobInstance> getJobInstances(String jobName, int start, int count) {
        return queryJobInstances(eq(JOB_NAME_KEY, jobName), start, count)
                .stream()
                .map(JobInstanceEntity::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getJobNames() {
        return queryDistinctJobNames();
    }

    /**
     * Fetch the last job instances with the provided name, sorted backwards by
     * primary key, using a 'like' criteria
     *
     * @param jobName {@link String} containing the name of the job.
     * @param start   int containing the offset of where list of job instances
     *                results should begin.
     * @param count   int containing the number of job instances to return.
     * @return a list of {@link JobInstance} for the job name requested.
     */
    @Override
    public List<JobInstance> findJobInstancesByName(String jobName, final int start, final int count) {
        // create a regex pattern to match on *jobname*;
        return queryJobInstances(regex(JOB_NAME_KEY).pattern(".*" + jobName + ".*"), start, count)
                .stream()
                .map(JobInstanceEntity::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int getJobInstanceCount(String jobName) throws NoSuchJobException {
        long count = queryJobInstanceCount(jobName);

        if (count == 0) {
            throw new NoSuchJobException("No job instances were found for job name " + jobName);
        }

        return (int) count;
    }

    @Override
    Datastore getDataStore() {
        return this.datastore;
    }

    private JobInstanceEntity getJobFromId(long jobInstanceId) {
        return getDataStore().find(JobInstanceEntity.class)
                .filter(eq(JOB_INSTANCE_ID_KEY, jobInstanceId))
                .first();
    }


    /**
     * Gets JobInstanceEntities matching the provided query parameters.
     * Results are sorted with most recent first.
     * @param jobNameFilter Filter to use in query
     * @param start number of records to skip
     * @param count limit
     * @return List containing JobInstanceEntities
     */
    private List<JobInstanceEntity> queryJobInstances(Filter jobNameFilter, int start, int count) {
        return getDataStore().find(JobInstanceEntity.class).
                filter(jobNameFilter)
                .iterator(new FindOptions()
                        .sort(descending(JOB_INSTANCE_ID_KEY))
                        .skip(start)
                        .limit(count)).toList();
    }

    private List<JobInstanceEntity> getJobInstanceFromDb(String jobName, String jobKey) {
        final Query<JobInstanceEntity> query = getDataStore().find(JobInstanceEntity.class);

        // if jobKey is empty, then return only jobs with an empty key
        if (StringUtils.hasLength(jobKey)) {
            query.filter(eq(JOB_KEY_KEY, jobKey));
        } else {
            query.filter(or(
                    eq(JOB_KEY_KEY, jobKey),
                    eq(JOB_KEY_KEY, null)
                    )
            );
        }

        return query.filter(eq(JOB_NAME_KEY, jobName)).iterator().toList();
    }


    /**
     * Gets all unique job names in database.
     * TODO: use "distinct" or some other more efficient way
     * See https://github.com/MorphiaOrg/morphia/issues/219
     *
     * @return
     */
    private List<String> queryDistinctJobNames() {

//        Set<String> result = new HashSet<>();
//        MorphiaCursor<JobInstanceEntity> iterator = getDataStore().find(JobInstanceEntity.class)
//                .iterator(new FindOptions()
//                        .projection().include(JOB_NAME_KEY)
//                );
//
//        while (iterator.hasNext()) {
//            result.add(iterator.next().getJobName());
//        }
//
//        return new ArrayList<>(result);

        return queryDistinctStringValues(JobInstanceEntity.class, JOB_NAME_KEY);
    }


    private long queryJobInstanceCount(String jobName) {
        return getDataStore().find(JobInstanceEntity.class)
                .filter(eq(JOB_NAME_KEY, jobName))
                .count();
    }
}
