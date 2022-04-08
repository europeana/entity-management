package eu.europeana.entitymanagement.batch.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_REMOVALS_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.ENTITY_UPDATE_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.PERIODIC_REMOVALS_SCHEDULER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.PERIODIC_UPDATES_SCHEDULER;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;

import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import java.time.Duration;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@PropertySources({
  @PropertySource("classpath:entitymanagement.properties"),
  @PropertySource(
      value = "classpath:entitymanagement.user.properties",
      ignoreResourceNotFound = true)
})
//EA-2992
//@ConditionalOnProperty(
//    prefix = "batch.scheduling",
//    value = "enabled",
//    havingValue = "true",
//    matchIfMissing = true)
@EnableScheduling
public class EntityUpdateSchedulingConfig implements InitializingBean {

  private static final Logger logger = LogManager.getLogger(EntityUpdateSchedulingConfig.class);
  private final JobLauncher entityUpdateJobLauncher;
  private final JobLauncher entityDeletionsJobLauncher;
  private final EntityUpdateJobConfig updateJobConfig;

  private final TaskScheduler updatesScheduler;
  private final TaskScheduler removalsScheduler;

  @Value("${batch.scheduling.metrics.initialDelaySeconds}")
  private long metricsUpdateInitialDelay;

  @Value("${batch.scheduling.full.initialDelaySeconds}")
  private long fullUpdateInitialDelay;

  @Value("${batch.scheduling.deletion.initialDelaySeconds}")
  private long deletionInitialDelay;

  @Value("${batch.scheduling.deprecation.initialDelaySeconds}")
  private long deprecationInitialDelay;

  @Value("${batch.scheduling.intervalSeconds}")
  private long interval;

  public EntityUpdateSchedulingConfig(
      @Qualifier(ENTITY_UPDATE_JOB_LAUNCHER) JobLauncher entityUpdateJobLauncher,
      @Qualifier(ENTITY_REMOVALS_JOB_LAUNCHER) JobLauncher entityDeletionsJobLauncher,
      EntityUpdateJobConfig batchUpdateConfig,
      @Qualifier(PERIODIC_UPDATES_SCHEDULER) TaskScheduler updatesScheduler,
      @Qualifier(PERIODIC_REMOVALS_SCHEDULER) TaskScheduler removalsScheduler) {
    this.entityUpdateJobLauncher = entityUpdateJobLauncher;
    this.entityDeletionsJobLauncher = entityDeletionsJobLauncher;
    this.updateJobConfig = batchUpdateConfig;
    this.updatesScheduler = updatesScheduler;
    this.removalsScheduler = removalsScheduler;
  }

  @Override
  public void afterPropertiesSet() {
    logger.info(
        "Batch scheduling initialized – metricsUpdateInitialDelay: {}; fullUpdateInitialDelay: {}; "
            + "deletionInitialDelay: {}; deprecationInitialDelay: {}"
            + "interval: {}",
        toMinutesAndSeconds(metricsUpdateInitialDelay),
        toMinutesAndSeconds(fullUpdateInitialDelay),
        toMinutesAndSeconds(deletionInitialDelay),
        toMinutesAndSeconds(deprecationInitialDelay),
        toMinutesAndSeconds(interval));

    schedulePeriodicUpdates();
    schedulePeriodicDeletions();
  }

  private void schedulePeriodicDeletions() {
    removalsScheduler.scheduleWithFixedDelay(
        this::runScheduledDeprecation,
        Instant.now().plusSeconds(deprecationInitialDelay),
        Duration.ofSeconds(interval));

    removalsScheduler.scheduleWithFixedDelay(
        this::runScheduledDeletions,
        Instant.now().plusSeconds(deletionInitialDelay),
        Duration.ofSeconds(interval));
  }

  private void schedulePeriodicUpdates() {
    updatesScheduler.scheduleWithFixedDelay(
        this::runScheduledFullUpdate,
        Instant.now().plusSeconds(fullUpdateInitialDelay),
        Duration.ofSeconds(interval));

    updatesScheduler.scheduleWithFixedDelay(
        this::runScheduledMetricsUpdate,
        Instant.now().plusSeconds(metricsUpdateInitialDelay),
        Duration.ofSeconds(interval));
  }

  /** Periodically run full entity updates. */
  @Async
  void runScheduledFullUpdate() {
    logger.info("Triggering scheduled full update for entities");
    try {
      entityUpdateJobLauncher.run(
          updateJobConfig.updateScheduledEntities(ScheduledUpdateType.FULL_UPDATE),
          BatchUtils.createJobParameters(
              null, Date.from(Instant.now()), ScheduledUpdateType.FULL_UPDATE, false));
    } catch (Exception e) {
      logger.warn("Error running scheduled full update", e);
    }
  }

  /** Periodically run metrics updates. */
  @Async
  void runScheduledMetricsUpdate() {
    logger.info("Triggering scheduled metrics update for entities");
    try {
      entityUpdateJobLauncher.run(
          updateJobConfig.updateScheduledEntities(ScheduledUpdateType.METRICS_UPDATE),
          BatchUtils.createJobParameters(
              null, Date.from(Instant.now()), ScheduledUpdateType.METRICS_UPDATE, false));
    } catch (Exception e) {
      logger.warn("Error running scheduled metrics update", e);
    }
  }

  /** Periodically run deletions */
  @Async
  void runScheduledDeletions() {
    logger.info("Triggering scheduled deletions for entities");
    try {
      entityDeletionsJobLauncher.run(
          updateJobConfig.removeScheduledEntities(PERMANENT_DELETION),
          BatchUtils.createJobParameters(
              null, Date.from(Instant.now()), PERMANENT_DELETION, false));
    } catch (Exception e) {
      logger.warn("Error running scheduled deletion", e);
    }
  }

  /** Periodically run deprecation */
  @Async
  void runScheduledDeprecation() {
    logger.info("Triggering scheduled deprecation for entities");
    try {
      entityDeletionsJobLauncher.run(
          updateJobConfig.removeScheduledEntities(DEPRECATION),
          BatchUtils.createJobParameters(null, Date.from(Instant.now()), DEPRECATION, false));
    } catch (Exception e) {
      logger.warn("Error running scheduled deprecation", e);
    }
  }

  /** Converts Seconds to "x min, y sec" */
  private String toMinutesAndSeconds(long seconds) {
    return String.format(
        "%d min, %d sec",
        TimeUnit.SECONDS.toMinutes(seconds),
        seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
  }
}
