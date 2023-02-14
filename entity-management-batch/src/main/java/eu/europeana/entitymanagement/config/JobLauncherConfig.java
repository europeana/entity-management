package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;

import eu.europeana.batch.config.MongoBatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class JobLauncherConfig {

  private final MongoBatchConfigurer mongoBatchConfigurer;
  private final TaskExecutor synchronousWebRequestExecutor;
  private final TaskExecutor deletionsTaskExecutor;

  public JobLauncherConfig(
      MongoBatchConfigurer mongoBatchConfigurer,
      @Qualifier(WEB_REQUEST_JOB_EXECUTOR) TaskExecutor webRequestJobExecutor,
      @Qualifier(SCHEDULED_REMOVAL_TASK_EXECUTOR) TaskExecutor deletionsTaskExecutor) {
    this.mongoBatchConfigurer = mongoBatchConfigurer;
    this.synchronousWebRequestExecutor = webRequestJobExecutor;
    this.deletionsTaskExecutor = deletionsTaskExecutor;
  }

  @Bean(name = ENTITY_UPDATE_JOB_LAUNCHER)
  @Primary
  public JobLauncher defaultJobLauncher() throws Exception {
    return mongoBatchConfigurer.getJobLauncher();
  }

  @Bean(ENTITY_REMOVALS_JOB_LAUNCHER)
  public JobLauncher entityDeletionJobLauncher() throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(mongoBatchConfigurer.getJobRepository());
    jobLauncher.setTaskExecutor(deletionsTaskExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

  @Bean(name = SYNC_WEB_REQUEST_JOB_LAUNCHER)
  public JobLauncher synchronousJobLauncher() throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(mongoBatchConfigurer.getJobRepository());
    jobLauncher.setTaskExecutor(synchronousWebRequestExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
}
