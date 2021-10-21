package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_DELETIONS_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_UPDATE_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
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
  private final JobLauncher entityUpdateJobLauncher;
  private final JobLauncher entityDeletionsJobLauncher;
  private final EntityUpdateJobConfig updateJobConfig;

  /**
   * Inject batch scheduling configs so they can be logged. Variables can't be used in @Scheduled
   * annotation though.
   */
  @Value("${batch.scheduling.metrics.initialDelayMillis}")
  private long updateMetricsInitialDelay;

  @Value("${batch.scheduling.full.initialDelayMillis}")
  private long updateFullInitialDelay;

  @Value("${batch.scheduling.deletion.initialDelayMillis}")
  private long removalDeletionInitialDelay;

  @Value("${batch.scheduling.deprecation.initialDelayMillis}")
  private long removalDeprecationInitialDelay;

  @Value("${batch.scheduling.fixedDelayMillis}")
  private long fixedDelay;

  public EntityUpdateSchedulingConfig(
      @Qualifier(ENTITY_UPDATE_JOB_LAUNCHER) JobLauncher entityUpdateJobLauncher,
      @Qualifier(ENTITY_DELETIONS_JOB_LAUNCHER) JobLauncher entityDeletionsJobLauncher,
      EntityUpdateJobConfig batchUpdateConfig) {
    this.entityUpdateJobLauncher = entityUpdateJobLauncher;
    this.entityDeletionsJobLauncher = entityDeletionsJobLauncher;
    this.updateJobConfig = batchUpdateConfig;
  }

  @Override
  public void afterPropertiesSet() {
    logger.info(
        "Batch scheduling initialized – updateMetricsInitialDelay: {}; updateFullInitialDelay: {}; "
            + "removalDeletionInitialDelay: {}; removalDeprecationInitialDelay: {}"
            + "interval: {}",
        toMinutesAndSeconds(updateMetricsInitialDelay),
        toMinutesAndSeconds(updateFullInitialDelay),
        toMinutesAndSeconds(removalDeletionInitialDelay),
        toMinutesAndSeconds(removalDeprecationInitialDelay),
        toMinutesAndSeconds(fixedDelay));
  }

  /** Periodically run full entity updates. */
  @Scheduled(
      initialDelayString = "${batch.scheduling.full.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledFullUpdate() throws Exception {
    logger.info("Triggering scheduled full update for entities");
    entityUpdateJobLauncher.run(
        updateJobConfig.updateScheduledEntities(ScheduledUpdateType.FULL_UPDATE),
        BatchUtils.createJobParameters(
            null, Date.from(Instant.now()), ScheduledUpdateType.FULL_UPDATE));
  }

  /** Periodically run metrics updates. */
  @Scheduled(
      initialDelayString = "${batch.scheduling.metrics.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledMetricsUpdate() throws Exception {
    logger.info("Triggering scheduled metrics update for entities");
    entityUpdateJobLauncher.run(
        updateJobConfig.updateScheduledEntities(ScheduledUpdateType.METRICS_UPDATE),
        BatchUtils.createJobParameters(
            null, Date.from(Instant.now()), ScheduledUpdateType.METRICS_UPDATE));
  }

  /** Periodically run deletions */
  @Scheduled(
      initialDelayString = "${batch.scheduling.deletion.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledDeletions() throws Exception {
    logger.info("Triggering scheduled deletions for entities");
    entityDeletionsJobLauncher.run(
        updateJobConfig.removeScheduledEntities(PERMANENT_DELETION),
        BatchUtils.createJobParameters(null, Date.from(Instant.now()), PERMANENT_DELETION));
  }

  /** Periodically run deprecation */
  @Scheduled(
      initialDelayString = "${batch.scheduling.deprecation.initialDelayMillis}",
      fixedDelayString = "${batch.scheduling.fixedDelayMillis}")
  private void runScheduledDeprecation() throws Exception {
    logger.info("Triggering scheduled deprecation for entities");
    entityDeletionsJobLauncher.run(
        updateJobConfig.removeScheduledEntities(DEPRECATION),
        BatchUtils.createJobParameters(null, Date.from(Instant.now()), DEPRECATION));
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
