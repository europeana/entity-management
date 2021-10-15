package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.*;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {

  private final EntityManagementConfiguration emConfig;

  public TaskExecutorConfig(EntityManagementConfiguration emConfig) {
    this.emConfig = emConfig;
  }

  /**
   * Returns a TaskExecutor to be used for scheduled entity updates. This is a singleThreadExecutor,
   * so updates cannot run simultaneously.
   */
  @Bean(SCHEDULED_UPDATE_TASK_EXECUTOR)
  public TaskExecutor scheduledUpdateExecutor() {
    /*
     * launch all scheduled jobs within the Spring scheduling thread
     */
    return new SyncTaskExecutor();
  }

  /** Returns a TaskExecutor to be used for scheduled deletions / deprecation of entities. */
  @Bean(SCHEDULED_DELETION_TASK_EXECUTOR)
  public TaskExecutor scheduledDeletionsExecutor() {
    return new SyncTaskExecutor();
  }

  @Bean(STEP_EXECUTOR)
  public TaskExecutor stepExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchStepExecutorCorePool());
    taskExecutor.setMaxPoolSize(emConfig.getBatchStepExecutorMaxPool());
    taskExecutor.setQueueCapacity(emConfig.getBatchStepExecutorQueueSize());

    return taskExecutor;
  }

  /** Creates a TaskExecutor to be used for entity updates directly triggered from web requests. */
  @Bean(WEB_REQUEST_JOB_EXECUTOR)
  public TaskExecutor synchronousExecutor() {
    return new SyncTaskExecutor();
  }
}
