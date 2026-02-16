package eu.europeana.entitymanagement.batch.service;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.*;
import java.time.Instant;
import java.util.Date;

import eu.europeana.entitymanagement.batch.config.EntityUpdateJobFactory;
import eu.europeana.entitymanagement.batch.config.JobDescriptionFactory;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;

//@Configuration
@PropertySource("classpath:entitymanagement.properties")
@PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)
//@EnableScheduling
@Component
/**
 * This class is used to trigger asynchronuous execution of scheduled jobs 
 * @author GordeaS
 *
 */
public class BatchEntityUpdateExecutor {

  private static final Logger logger = LogManager.getLogger(BatchEntityUpdateExecutor.class);
  private final JobLauncher entityUpdateJobLauncher;
  private final JobLauncher entityDeletionsJobLauncher;
  private final EntityUpdateJobFactory entityUpdateJobFactory;
  private final JobDescriptionFactory jobDescriptionFactory;

  @Autowired
  public BatchEntityUpdateExecutor(
          @Qualifier(ENTITY_UPDATE_JOB_LAUNCHER) JobLauncher entityUpdateJobLauncher,
          @Qualifier(ENTITY_REMOVALS_JOB_LAUNCHER) JobLauncher entityDeletionsJobLauncher,
          EntityUpdateJobFactory entityUpdateJobFactory,
          @Qualifier(JOB_DESCRIPTION_FACTORY) JobDescriptionFactory jobDescriptionFactory) {
    this.entityUpdateJobLauncher    = entityUpdateJobLauncher;
    this.entityDeletionsJobLauncher = entityDeletionsJobLauncher;
    this.entityUpdateJobFactory     = entityUpdateJobFactory;
    this.jobDescriptionFactory = jobDescriptionFactory;
  }

  /** Periodically run full entity and metric updates (in one run). */
  @Async
  public void runScheduledTasks() {
    logger.info("Running scheduled tasks - {}, {} for entities", TaskType.full_update, TaskType.metrics_update);
    try {
      entityUpdateJobLauncher.run(
              entityUpdateJobFactory.createScheduledUpdateJob(jobDescriptionFactory.get(TaskType.full_update)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.full_update,
                      false));

      entityUpdateJobLauncher.run(
              entityUpdateJobFactory.createScheduledUpdateJob(jobDescriptionFactory.get(TaskType.metrics_update)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.metrics_update,
                      false));
    } catch (Exception e) {
      logger.warn("Error running scheduled {} and {} update", TaskType.full_update, TaskType.metrics_update, e);
    }
  }

  /** Periodically run deprecations and deletions (in one run) */
  @Async
  public void runScheduledDeprecationsAndDeletions() {
    logger.info("Triggering scheduled deprecations and deletions for entities");
    try {
      entityDeletionsJobLauncher.run(
              entityUpdateJobFactory.removeScheduledEntities(jobDescriptionFactory.get(TaskType.permanent_deletion)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.permanent_deletion,
                      false));

      entityDeletionsJobLauncher.run(
              entityUpdateJobFactory.removeScheduledEntities(jobDescriptionFactory.get(TaskType.deprecation)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.deprecation,
                      false));
    } catch (Exception e) {
      logger.warn("Error running scheduled deprecations and deletions", e);
    }
  }
}
