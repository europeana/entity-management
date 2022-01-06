package eu.europeana.entitymanagement.batch.service;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_WEB_REQUEST_JOB_LAUNCHER;

import eu.europeana.entitymanagement.batch.config.EntityUpdateJobConfig;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EntityUpdateService {
  private static final Logger logger = LogManager.getLogger(EntityUpdateService.class);

  private final EntityUpdateJobConfig entityUpdateJobConfig;
  private final JobLauncher syncWebRequestLauncher;

  private final ScheduledTaskService scheduledTaskService;

  @Autowired
  public EntityUpdateService(
      EntityUpdateJobConfig entityUpdateJobConfig,
      @Qualifier(SYNC_WEB_REQUEST_JOB_LAUNCHER) JobLauncher syncWebRequestLauncher,
      ScheduledTaskService scheduledTaskService) {
    this.entityUpdateJobConfig = entityUpdateJobConfig;
    this.scheduledTaskService = scheduledTaskService;
    this.syncWebRequestLauncher = syncWebRequestLauncher;
  }

  /**
   * Synchronously updates the entity with the given entityId
   *
   * @param entityId entityId
   * @throws Exception on exception
   */
  public void runSynchronousUpdate(String entityId) throws Exception {
    logger.info("Triggering synchronous update for entityId={}", entityId);
    syncWebRequestLauncher.run(
        entityUpdateJobConfig.updateSingleEntity(),
        BatchUtils.createJobParameters(
            entityId, Date.from(Instant.now()), ScheduledUpdateType.FULL_UPDATE, true));
  }

  /**
   * Schedules entities with the given entityIds for an update.
   *
   * @param entityIds list of entity ids
   * @param updateType type of update to schedule
   */
  public void scheduleTasks(List<String> entityIds, ScheduledTaskType updateType) {
    if (CollectionUtils.isEmpty(entityIds)) {
      return;
    }
    logger.info(
        "Scheduling async task for entityIds={}, count={} updateType={}",
        Arrays.toString(entityIds.toArray()),
        entityIds.size(),
        updateType);
    scheduledTaskService.scheduleTasksForEntities(entityIds, updateType);
  }
}
