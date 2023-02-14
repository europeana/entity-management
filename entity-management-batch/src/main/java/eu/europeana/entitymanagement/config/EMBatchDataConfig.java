package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_BATCH_DATA_STORE;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.SCHEDULED_UPDATE_TASK_EXECUTOR;

import dev.morphia.Datastore;
import eu.europeana.batch.config.MongoBatchConfigurer;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class EMBatchDataConfig {

  @Resource(name = SCHEDULED_UPDATE_TASK_EXECUTOR)
  private TaskExecutor defaultTaskExecutor;

  /**
   * Configures Spring Batch to use Mongo
   *
   * @param datastore Morphia datastore for Spring Batch
   * @return BatchConfigurer instance
   */
  @Bean
  public MongoBatchConfigurer mongoBatchConfigurer(
      @Qualifier(BEAN_BATCH_DATA_STORE) Datastore datastore) {
    return new MongoBatchConfigurer(datastore, defaultTaskExecutor);
  }
}
