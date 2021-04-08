package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JOB_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_STEP_EXECUTOR;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_TASK_EXECUTOR;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {

  private final EntityManagementConfiguration emConfig;

  public TaskExecutorConfig(
      EntityManagementConfiguration emConfig) {
    this.emConfig = emConfig;
  }

  @Bean(BEAN_JOB_EXECUTOR)
  public TaskExecutor jobLauncherExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchJobExecutorCorePool());
    taskExecutor.setMaxPoolSize(emConfig.getBatchJobExecutorMaxPool());
    taskExecutor.setQueueCapacity(emConfig.getBatchJobExecutorQueueSize());
    return taskExecutor;
  }


  @Bean(BEAN_STEP_EXECUTOR)
  public TaskExecutor stepExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchStepExecutorCorePool());
    taskExecutor.setMaxPoolSize(emConfig.getBatchStepExecutorMaxPool());
    taskExecutor.setQueueCapacity(emConfig.getBatchStepExecutorQueueSize());

    return taskExecutor;
  }

  @Bean(SYNC_TASK_EXECUTOR)
  public TaskExecutor synchronousExecutor(){
    return new SyncTaskExecutor();
  }
}
