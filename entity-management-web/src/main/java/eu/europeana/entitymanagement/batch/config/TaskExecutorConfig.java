package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;

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

  /** Returns a TaskExecutor to be used for scheduled deletions / deprecation of entities. This is a singleThreadExecutor,
   * so deletions cannot run simultaneously*/
  @Bean(SCHEDULED_REMOVAL_TASK_EXECUTOR)
  public TaskExecutor scheduledDeletionsExecutor() {
    return new SyncTaskExecutor();
  }

  /**
   * Executor used for Steps when running Scheduled Updates. The configures the concurrency settings
   * for Full and Metrics updates.
   */
  @Bean(UPDATES_STEP_EXECUTOR)
  public TaskExecutor updatesStepExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchUpdatesCorePoolSize());
    taskExecutor.setMaxPoolSize(emConfig.getBatchUpdatesMaxPoolSize());
    taskExecutor.setQueueCapacity(emConfig.getBatchUpdatesQueueSize());

    return taskExecutor;
  }

  /**
   * Executor used for Steps when running Scheduled Removals. The configures the concurrency
   * settings for Deprecations and Permanent Deletions
   */
  @Bean(REMOVALS_STEP_EXECUTOR)
  public TaskExecutor removalsStepExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(emConfig.getBatchRemovalsCorePoolSize());
    taskExecutor.setMaxPoolSize(emConfig.getBatchRemovalsMaxPoolSize());
    taskExecutor.setQueueCapacity(emConfig.getBatchRemovalsQueueSize());

    return taskExecutor;
  }

  /** Creates a TaskExecutor to be used for entity updates directly triggered from web requests. */
  @Bean(WEB_REQUEST_JOB_EXECUTOR)
  public TaskExecutor synchronousExecutor() {
    return new SyncTaskExecutor();
  }
}
