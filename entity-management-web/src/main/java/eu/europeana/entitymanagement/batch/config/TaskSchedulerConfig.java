package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.PERIODIC_REMOVALS_SCHEDULER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.PERIODIC_UPDATES_SCHEDULER;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configures TaskSchedulers for triggering periodic ScheduledTask handling. Multiple schedulers are
 * used, so long-running updates do not block the execution of deletions / deprecations
 */
@Configuration
public class TaskSchedulerConfig {

  @Bean(PERIODIC_UPDATES_SCHEDULER)
  public TaskScheduler asyncTaskScheduler() {
    return new ThreadPoolTaskScheduler();
  }

  @Bean(PERIODIC_REMOVALS_SCHEDULER)
  public TaskScheduler deletionsTaskScheduler() {
    return new ThreadPoolTaskScheduler();
  }
}
