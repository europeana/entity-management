package eu.europeana.entitymanagement.config;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;

@Configuration
public class TaskExecutorConfig {

  private final EntityManagementConfiguration emConfig;

  public TaskExecutorConfig(
      EntityManagementConfiguration emConfig) {
    this.emConfig = emConfig;
  }

  /**
   * Creates a JobLauncher to be used for scheduled entity updates.
   * This is a singleThreadExecutor, so updates cannot run simultaneously.
   */
  @Bean(SCHEDULED_JOB_EXECUTOR)
  public TaskExecutor jobLauncherExecutor() {
    /* Roughly equivalent to Executors.newSingleThreadExecutor(),
     * sharing a single thread for all tasks
     */
    return new ThreadPoolTaskExecutor();
  }


  @Bean(STEP_EXECUTOR)
  public TaskExecutor stepExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchStepExecutorCorePool());
    taskExecutor.setMaxPoolSize(emConfig.getBatchStepExecutorMaxPool());
    taskExecutor.setQueueCapacity(emConfig.getBatchStepExecutorQueueSize());

    return taskExecutor;
  }

  @Bean(SYNC_JOB_EXECUTOR)
  public TaskExecutor synchronousExecutor(){
    return new SyncTaskExecutor();
  }
}
