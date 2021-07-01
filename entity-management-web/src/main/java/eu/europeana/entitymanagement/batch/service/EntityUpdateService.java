package eu.europeana.entitymanagement.batch.service;


import eu.europeana.entitymanagement.batch.config.EntityUpdateJobConfig;
import eu.europeana.entitymanagement.batch.BatchUtils;
import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_LAUNCHER;

@Service
public class EntityUpdateService {
    private static final Logger logger = LogManager.getLogger(EntityUpdateService.class);

    private final EntityUpdateJobConfig entityUpdateJobConfig;
    private final JobLauncher synchronousJobLauncher;

    private final ScheduledTaskService scheduledTaskService;

    @Autowired
    public EntityUpdateService(EntityUpdateJobConfig entityUpdateJobConfig,
                               @Qualifier(SYNC_JOB_LAUNCHER) JobLauncher synchronousJobLauncher,
                               ScheduledTaskService scheduledTaskService) {
        this.entityUpdateJobConfig = entityUpdateJobConfig;
        this.scheduledTaskService = scheduledTaskService;
        this.synchronousJobLauncher = synchronousJobLauncher;
    }

    /**
     * Synchronously updates the entity with the given entityId
     * @param entityId entityId
     * @throws Exception on exception
     */
    public void runSynchronousUpdate(String entityId) throws Exception {
        logger.info("Triggering synchronous update for entityId={}", entityId);
        synchronousJobLauncher.run(entityUpdateJobConfig.updateSingleEntity(),
                BatchUtils.createJobParameters(entityId, Date.from(Instant.now()), BatchUpdateType.FULL));
    }


    /**
     * Schedules entities with the given entityIds for an update.
     * @param entityIds list of entity ids
     * @param updateType type of update to schedule
     */
    public void scheduleUpdates(List<String> entityIds, BatchUpdateType updateType) {
        if (CollectionUtils.isEmpty(entityIds)) {
            return;
        }
        logger.info("Scheduling async updates for entityIds={}, count={}", Arrays.toString(entityIds.toArray()),
                entityIds.size());
        scheduledTaskService.scheduleUpdateBulk(entityIds, updateType);
    }
}
