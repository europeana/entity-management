package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class StepExecutionRepository extends AbstractRepository implements StepExecutionDao {

    @Autowired
    private Datastore datastore;


    @Override
    public void saveStepExecution(StepExecution stepExecution) {

    }

    @Override
    public void saveStepExecutions(Collection<StepExecution> collection) {

    }

    @Override
    public void updateStepExecution(StepExecution stepExecution) {

    }

    @Override
    public StepExecution getStepExecution(JobExecution jobExecution, Long aLong) {
        return null;
    }

    @Override
    public void addStepExecutions(JobExecution jobExecution) {

    }

    @Override
    Datastore getDataStore() {
        return this.datastore;
    }
}
