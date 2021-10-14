package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.batch.model.ScheduledTaskType.FULL_UPDATE;
import static eu.europeana.entitymanagement.batch.model.ScheduledTaskType.METRICS_UPDATE;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SCHEDULED_JOB_LAUNCHER;

import eu.europeana.entitymanagement.batch.BatchUtils;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@PropertySources({
  @PropertySource("classpath:entitymanagement.properties"),
  @PropertySource(
      value = "classpath:entitymanagement.user.properties",
      ignoreResourceNotFound = true)
})
@ConditionalOnProperty(
    prefix = "batch.scheduling",
    value = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableScheduling
public class EntityUpdateSchedulingConfig implements InitializingBean {

  private static final Logger logger = LogManager.getLogger(EntityUpdateSchedulingConfig.class);
  private final JobLauncher scheduledJobLauncher;
  private final EntityUpdateJobConfig updateJobConfig;

  /**
   * Inject batch scheduling configs so they can be logged. Variables can't be used in @Scheduled
   * annotation though.
   */
  @Value("${batch.scheduling.metrics.initialDelayMillis}")
  private long metricsInitialDelay;

  @Value("${batch.scheduling.full.initialDelayMillis}")
  private long fullInitialDelay;

  @Value("${batch.scheduling.fixedDelayMillis}")
  private long fixedDelay;

  public EntityUpdateSchedulingConfig(
      @Qualifier(SCHEDULED_JOB_LAUNCHER) JobLauncher scheduledJobLauncher,
      EntityUpdateJobConfig batchUpdateConfig) {
    this.scheduledJobLauncher = scheduledJobLauncher;
    this.updateJobConfig = batchUpdateConfig;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    logger.info(
        "Batch scheduling initialized – metricsInitialDelay: {}; fullInitialDelay: {}; fixedDelay: {}",
        toMinutesAndSeconds(metricsInitialDelay),
        toMinutesAndSeconds(fullInitialDelay),
        toMinutesAndSeconds(fixedDelay));
  }

  /** Periodically run full entity updates. */
  @Scheduled(
      initialDelayString = "${batch.scheduling.full.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledFullUpdate() throws Exception {
    logger.info("Triggering scheduled full update for entities");
    scheduledJobLauncher.run(
        updateJobConfig.updateScheduledEntities(FULL_UPDATE),
        BatchUtils.createJobParameters(null, Date.from(Instant.now()), FULL_UPDATE));
  }

  /** Periodically run metrics updates. */
  @Scheduled(
      initialDelayString = "${batch.scheduling.metrics.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledMetricsUpdate() throws Exception {
    logger.info("Triggering scheduled metrics update for entities");
    scheduledJobLauncher.run(
        updateJobConfig.updateScheduledEntities(METRICS_UPDATE),
        BatchUtils.createJobParameters(null, Date.from(Instant.now()), METRICS_UPDATE));
  }

  /** Converts milliseconds to "x min, y sec" */
  private String toMinutesAndSeconds(long millis) {
    return String.format(
        "%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis)
            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
  }
}
