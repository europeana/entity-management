package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_JOB_EXECUTOR;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {

  @Bean(BEAN_JOB_EXECUTOR)
  public TaskExecutor jobLauncherExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(10);
    taskExecutor.setMaxPoolSize(100);
    taskExecutor.setQueueCapacity(50);
    return taskExecutor;
  }
}
