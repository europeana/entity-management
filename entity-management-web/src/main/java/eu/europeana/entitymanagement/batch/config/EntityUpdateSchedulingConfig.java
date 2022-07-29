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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
@EnableScheduling
public class EntityUpdateSchedulingConfig implements InitializingBean {

  private static final Logger logger = LogManager.getLogger(EntityUpdateSchedulingConfig.class);
  private final JobLauncher entityUpdateJobLauncher;
  private final JobLauncher entityDeletionsJobLauncher;
  private final EntityUpdateJobConfig updateJobConfig;

  private final TaskScheduler updatesScheduler;
  private final TaskScheduler removalsScheduler;

  @Value("${batch.scheduling.update.initialDelaySeconds}")
  private long updateInitialDelay;

  @Value("${batch.scheduling.deprecation-deletion.initialDelaySeconds}")
  private long deprecationDeletionInitialDelay;

  @Value("${batch.scheduling.intervalSeconds}")
  private long interval;

  @Value("${batch.scheduling.enabled}")
  private boolean syncEnabled;

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
    if (syncEnabled) {
      // invoke methods outside logging call
      String updateInitialDelayString = toMinutesAndSeconds(updateInitialDelay);
      String deprecationDeletionInitialDelayString =
          toMinutesAndSeconds(deprecationDeletionInitialDelay);
      String intervalString = toMinutesAndSeconds(interval);

      logger.info(
          "Batch scheduling initialized – updateInitialDelay: {}; "
              + "deprecationDeletionInitialDelay: {}"
              + "interval: {}",
          updateInitialDelayString,
          deprecationDeletionInitialDelayString,
          intervalString);

      schedulePeriodicUpdates();
      schedulePeriodicDeprecationsAndDeletions();
    } else {
      logger.warn("Batch scheduling disabled. Entities will not be automatically updated.");
    }
  }

  private void schedulePeriodicDeprecationsAndDeletions() {
    removalsScheduler.scheduleWithFixedDelay(
        this::runScheduledDeprecationsAndDeletions,
        Instant.now().plusSeconds(deprecationDeletionInitialDelay),
        Duration.ofSeconds(interval));
  }

  private void schedulePeriodicUpdates() {
    updatesScheduler.scheduleWithFixedDelay(
        this::runScheduledUpdate,
        Instant.now().plusSeconds(updateInitialDelay),
        Duration.ofSeconds(interval));
  }

  /** Periodically run full entity and metric updates (in one run). */
  @Async
  void runScheduledUpdate() {
    logger.info("Triggering scheduled full and metrics update for entities");
    try {
      entityUpdateJobLauncher.run(
          updateJobConfig.updateScheduledEntities(
              List.of(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE)),
          BatchUtils.createJobParameters(
              null,
              Date.from(Instant.now()),
              List.of(ScheduledUpdateType.FULL_UPDATE, ScheduledUpdateType.METRICS_UPDATE),
              false));
    } catch (Exception e) {
      logger.warn("Error running scheduled full and metrics update", e);
    }
  }

  /** Periodically run deprecations and deletions (in one run) */
  @Async
  void runScheduledDeprecationsAndDeletions() {
    logger.info("Triggering scheduled deprecations and deletions for entities");
    try {
      entityDeletionsJobLauncher.run(
          updateJobConfig.removeScheduledEntities(List.of(PERMANENT_DELETION, DEPRECATION)),
          BatchUtils.createJobParameters(
              null, Date.from(Instant.now()), List.of(PERMANENT_DELETION, DEPRECATION), false));
    } catch (Exception e) {
      logger.warn("Error running scheduled deprecations and deletions", e);
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
