package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SCHEDULED_JOB_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_EXECUTOR;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;

@Configuration
public class TaskExecutorConfig {

  private final EntityManagementConfiguration emConfig;

  public TaskExecutorConfig(
      EntityManagementConfiguration emConfig) {
    this.emConfig = emConfig;
  }

  @Bean(SCHEDULED_JOB_EXECUTOR)
  public TaskExecutor jobLauncherExecutor() {
    // This is roughly equivalent to Executors.newSingleThreadExecutor(),
    // sharing a single thread for all tasks
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
