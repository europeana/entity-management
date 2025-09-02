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
    logger.info("Triggering scheduled {}, {} for entities", TaskType.FULL_UPDATE, TaskType.METRICS_UPDATE);
    try {
      entityUpdateJobLauncher.run(
              entityUpdateJobFactory.createScheduledUpdateJob(jobDescriptionFactory.get(TaskType.FULL_UPDATE)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.FULL_UPDATE,
                      false));

      entityUpdateJobLauncher.run(
              entityUpdateJobFactory.createScheduledUpdateJob(jobDescriptionFactory.get(TaskType.METRICS_UPDATE)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.METRICS_UPDATE,
                      false));
    } catch (Exception e) {
      logger.warn("Error running scheduled {} and {} update", TaskType.FULL_UPDATE, TaskType.METRICS_UPDATE, e);
    }
  }

  /** Periodically run deprecations and deletions (in one run) */
  @Async
  public void runScheduledDeprecationsAndDeletions() {
    logger.info("Triggering scheduled deprecations and deletions for entities");
    try {
      entityDeletionsJobLauncher.run(
              entityUpdateJobFactory.removeScheduledEntities(jobDescriptionFactory.get(TaskType.PERMANENT_DELETION)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.PERMANENT_DELETION,
                      false));

      entityDeletionsJobLauncher.run(
              entityUpdateJobFactory.createScheduledUpdateJob(jobDescriptionFactory.get(TaskType.DEPRECATION)),
              BatchUtils.createJobParameters(
                      null,
                      Date.from(Instant.now()),
                      TaskType.DEPRECATION,
                      false));
    } catch (Exception e) {
      logger.warn("Error running scheduled deprecations and deletions", e);
    }
  }
}
