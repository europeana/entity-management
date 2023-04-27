package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;
import javax.annotation.Resource;
import eu.europeana.batch.config.MongoBatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import dev.morphia.Datastore;

@Configuration
/**
 * This class is used to configure the JobLaunchers for single (e.g. web) requests and batch requests (e.g. k8s cron jobs)
 * @author GordeaS
 *
 */
public class JobLauncherConfig {

//  private final MongoBatchConfigurer mongoBatchConfigurer;
  private final TaskExecutor synchronousWebRequestExecutor;
  private final TaskExecutor deletionsTaskExecutor;
  @Resource(name = SCHEDULED_UPDATE_TASK_EXECUTOR)
  private TaskExecutor defaultTaskExecutor;
  @Resource(name = BEAN_BATCH_MONGO_CONFIGURER)
  private MongoBatchConfigurer mongoBatchConfigurer;

  public JobLauncherConfig(
      @Qualifier(WEB_REQUEST_JOB_EXECUTOR) TaskExecutor webRequestJobExecutor,
      @Qualifier(SCHEDULED_REMOVAL_TASK_EXECUTOR) TaskExecutor deletionsTaskExecutor) {
    this.synchronousWebRequestExecutor = webRequestJobExecutor;
    this.deletionsTaskExecutor = deletionsTaskExecutor;
  }

  /**
   * returns the default job launcher, by default the launcher from the MongoBatchConfigurer is returned
   * @return the default JobLauncher
   * @throws see  mongoBatchConfigurer.getJobLauncher  
   */
  public JobLauncher defaultJobLauncher() throws Exception {
    return mongoBatchConfigurer.getJobLauncher();
  }

  /**
   * indirection method 
   * @return the JobLauncher to be used for entityUpdates
   * @throws Exception see mongoBatchConfigurer.getJobLauncher
   */
  @Bean(name = ENTITY_UPDATE_JOB_LAUNCHER)
  @Primary
  public JobLauncher entityUpdateJobLauncher() throws Exception {
   return defaultJobLauncher();
  }
  
  
  /**
   * returns the job launcher for performing entity  deletions 
   * @return JobLauncher for deletions
   * @throws Exception see mongoBatchConfigurer.getJobRepository
   */
  @Bean(ENTITY_REMOVALS_JOB_LAUNCHER)
  public JobLauncher entityDeletionJobLauncher() throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(mongoBatchConfigurer.getJobRepository());
    jobLauncher.setTaskExecutor(deletionsTaskExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

  /**
   * Instantiates the JobLauncher for synhronuous execution from web requests
   * @return JobLauncher with synchronuous task executor 
   * @throws Exception
   */
  @Bean(name = SYNC_WEB_REQUEST_JOB_LAUNCHER)
  public JobLauncher synchronousJobLauncher() throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(mongoBatchConfigurer.getJobRepository());
    jobLauncher.setTaskExecutor(synchronousWebRequestExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
  
  /**
   * Configures Spring Batch to use Mongo
   *
   * @param datastore Morphia datastore for Spring Batch
   * @return BatchConfigurer instance
   */
  @Bean(name = BEAN_BATCH_MONGO_CONFIGURER)
  public MongoBatchConfigurer mongoBatchConfigurer(
      @Qualifier(BEAN_BATCH_DATA_STORE) Datastore datastore) {
    //TODO verify if sync or async task execution should be performed by MongoBatchConfigurer 
    return new MongoBatchConfigurer(datastore, defaultTaskExecutor);
  }
}
