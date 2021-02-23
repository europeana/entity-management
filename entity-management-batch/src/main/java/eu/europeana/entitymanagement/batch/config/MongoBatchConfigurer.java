package eu.europeana.entitymanagement.batch.config;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configures Spring Batch to use Mongo DAO implementations
 */
@Configuration
public class MongoBatchConfigurer implements BatchConfigurer {

    private final ExecutionContextDao mongoExecutionContextDao;

    private final JobExecutionDao mongoJobExecutionDao;

    private final JobInstanceDao mongoJobInstanceDao;

    private final StepExecutionDao mongoStepExecutionDao;

    @Autowired
    public MongoBatchConfigurer(ExecutionContextDao mongoExecutionContextDao, JobExecutionDao mongoJobExecutionDao, JobInstanceDao mongoJobInstanceDao, StepExecutionDao mongoStepExecutionDao) {
        this.mongoExecutionContextDao = mongoExecutionContextDao;
        this.mongoJobExecutionDao = mongoJobExecutionDao;
        this.mongoJobInstanceDao = mongoJobInstanceDao;
        this.mongoStepExecutionDao = mongoStepExecutionDao;
    }

    @Override
    public JobRepository getJobRepository() throws Exception {
        return new SimpleJobRepository(mongoJobInstanceDao, mongoJobExecutionDao, mongoStepExecutionDao, mongoExecutionContextDao);
    }

    @Override
    public PlatformTransactionManager getTransactionManager() throws Exception {
        return new ResourcelessTransactionManager();
    }

    @Override
    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        // Since this will be triggered from web requests, launching needs to be done async
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return new SimpleJobExplorer(mongoJobInstanceDao,
                mongoJobExecutionDao,
                mongoStepExecutionDao,
                mongoExecutionContextDao);
    }
}
