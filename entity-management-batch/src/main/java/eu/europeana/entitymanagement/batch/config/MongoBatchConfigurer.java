package eu.europeana.entitymanagement.batch.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.batch.repository.ExecutionContextRepository;
import eu.europeana.entitymanagement.batch.repository.JobExecutionRepository;
import eu.europeana.entitymanagement.batch.repository.JobInstanceRepository;
import eu.europeana.entitymanagement.batch.repository.SequenceRepository;
import eu.europeana.entitymanagement.batch.repository.StepExecutionRepository;

/**
 * Configures Spring Batch to use Mongo DAO implementations
 */
public class MongoBatchConfigurer implements BatchConfigurer {

    private static final Logger logger = LogManager.getLogger(MongoBatchConfigurer.class);

    private final ExecutionContextDao mongoExecutionContextDao;
    private final JobExecutionDao mongoJobExecutionDao;
    private final JobInstanceDao mongoJobInstanceDao;
    private final StepExecutionDao mongoStepExecutionDao;

    private final SequenceRepository sequenceRepository;

    private final TaskExecutor taskExecutor;

    /**
     * Instantiates the Mongo DAO implementations with the provided datastore
     *
     * @param datastore Morphia datastore to use
     */
    public MongoBatchConfigurer(Datastore datastore, TaskExecutor taskExecutor) {
        this.mongoExecutionContextDao = new ExecutionContextRepository(datastore);
        this.mongoJobExecutionDao = new JobExecutionRepository(datastore);
        this.mongoJobInstanceDao = new JobInstanceRepository(datastore);
        this.mongoStepExecutionDao = new StepExecutionRepository(datastore);
        this.sequenceRepository = new SequenceRepository(datastore);

        this.taskExecutor = taskExecutor;
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
        jobLauncher.setTaskExecutor(taskExecutor);
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


    public void clearRepository() {
        logger.debug("Clearing Batch JobRepository...");

        sequenceRepository.drop();
        // casting is safe here, as we know from the constructor what these instances are
        ((ExecutionContextRepository) mongoExecutionContextDao).drop();
        ((JobExecutionRepository) mongoJobExecutionDao).drop();
        ((JobInstanceRepository) mongoJobInstanceDao).drop();
        ((StepExecutionRepository) mongoStepExecutionDao).drop();

        logger.debug("Batch JobRepository cleared");
    }
}
