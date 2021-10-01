package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;

import eu.europeana.entitymanagement.batch.config.MongoBatchConfigurer;
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
  private final TaskExecutor synchronousTaskExecutor;

  public JobLauncherConfig(
      MongoBatchConfigurer mongoBatchConfigurer,
      @Qualifier(WEB_REQUEST_JOB_EXECUTOR) TaskExecutor synchronousTaskExecutor) {
    this.mongoBatchConfigurer = mongoBatchConfigurer;
    this.synchronousTaskExecutor = synchronousTaskExecutor;
  }

  @Bean(name = SCHEDULED_JOB_LAUNCHER)
  @Primary
  public JobLauncher defaultJobLauncher() throws Exception {
    return mongoBatchConfigurer.getJobLauncher();
  }

  @Bean(name = SYNC_JOB_LAUNCHER)
  public JobLauncher synchronousJobLauncher() throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(mongoBatchConfigurer.getJobRepository());
    jobLauncher.setTaskExecutor(synchronousTaskExecutor);
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
}
