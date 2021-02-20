package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JobInstanceRepository extends AbstractRepository implements JobInstanceDao {

    @Autowired
    private Datastore datastore;

    @Override
    public JobInstance createJobInstance(String s, JobParameters jobParameters) {
        return null;
    }

    @Override
    public JobInstance getJobInstance(String s, JobParameters jobParameters) {
        return null;
    }

    @Override
    public JobInstance getJobInstance(Long aLong) {
        return null;
    }

    @Override
    public JobInstance getJobInstance(JobExecution jobExecution) {
        return null;
    }

    @Override
    public List<JobInstance> getJobInstances(String s, int i, int i1) {
        return null;
    }

    @Override
    public List<String> getJobNames() {
        return null;
    }

    @Override
    public List<JobInstance> findJobInstancesByName(String s, int i, int i1) {
        return null;
    }

    @Override
    public int getJobInstanceCount(String s) throws NoSuchJobException {
        return 0;
    }

    @Override
    Datastore getDataStore() {
        return this.datastore;
    }
}
