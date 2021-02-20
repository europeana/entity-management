package eu.europeana.entitymanagement.batch.repository;

import dev.morphia.Datastore;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;

@Repository
public class ExecutionContextRepository extends AbstractRepository implements ExecutionContextDao {


    @Autowired
    private Datastore datastore;


    @Override
    public ExecutionContext getExecutionContext(JobExecution jobExecution) {
        return null;
    }

    @Override
    public ExecutionContext getExecutionContext(StepExecution stepExecution) {
        return null;
    }

    @Override
    public void saveExecutionContext(JobExecution jobExecution) {

    }

    @Override
    public void saveExecutionContext(StepExecution stepExecution) {

    }

    @Override
    public void saveExecutionContexts(Collection<StepExecution> collection) {

    }

    @Override
    public void updateExecutionContext(JobExecution jobExecution) {

    }

    @Override
    public void updateExecutionContext(StepExecution stepExecution) {

    }


    @Override
    Datastore getDataStore() {
        return this.datastore;
    }
}
