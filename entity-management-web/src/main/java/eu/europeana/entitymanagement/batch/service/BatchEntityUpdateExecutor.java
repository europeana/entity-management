package eu.europeana.entitymanagement.batch.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_REMOVALS_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.ENTITY_UPDATE_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.DEPRECATION;
import static eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType.PERMANENT_DELETION;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import eu.europeana.entitymanagement.batch.config.EntityUpdateJobConfig;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;

//@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
//@EnableScheduling
/**
 * This class is used to trigger asynchronuous execution of scheduled jobs 
 * @author GordeaS
 *
 */
public class BatchEntityUpdateExecutor {

  private static final Logger logger = LogManager.getLogger(BatchEntityUpdateExecutor.class);
  private final JobLauncher entityUpdateJobLauncher;
  private final JobLauncher entityDeletionsJobLauncher;
  private final EntityUpdateJobConfig updateJobConfig;

  public BatchEntityUpdateExecutor(
      @Qualifier(ENTITY_UPDATE_JOB_LAUNCHER) JobLauncher entityUpdateJobLauncher,
      @Qualifier(ENTITY_REMOVALS_JOB_LAUNCHER) JobLauncher entityDeletionsJobLauncher,
      EntityUpdateJobConfig batchUpdateConfig) {
    this.entityUpdateJobLauncher = entityUpdateJobLauncher;
    this.entityDeletionsJobLauncher = entityDeletionsJobLauncher;
    this.updateJobConfig = batchUpdateConfig;
  }

  /** Periodically run full entity and metric updates (in one run). */
  @Async
  public void runScheduledUpdate() {
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
  public void runScheduledDeprecationsAndDeletions() {
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
}
