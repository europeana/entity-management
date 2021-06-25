package eu.europeana.entitymanagement.batch;


import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static eu.europeana.entitymanagement.batch.model.BatchUpdateType.FULL;
import static eu.europeana.entitymanagement.batch.model.BatchUpdateType.METRICS;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SCHEDULED_JOB_LAUNCHER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_LAUNCHER;

/**
 * Config loaded here so scheduling properties can be loaded
 */
@Configuration
@PropertySources({@PropertySource("classpath:entitymanagement.properties"),
        @PropertySource(value = "classpath:entitymanagement.user.properties", ignoreResourceNotFound = true)})
@Service
public class BatchService {
    private static final Logger logger = LogManager.getLogger(BatchService.class);

    private final BatchJobConfig batchUpdateConfig;
    private final JobLauncher synchronousJobLauncher;
    private final JobLauncher scheduledJobLauncher;
    private final ScheduledTaskService scheduledTaskService;

    @Autowired
    public BatchService(BatchJobConfig batchUpdateConfig,
                        @Qualifier(SYNC_JOB_LAUNCHER) JobLauncher synchronousJobLauncher,
                        @Qualifier(SCHEDULED_JOB_LAUNCHER) JobLauncher scheduledJobLauncher,
                        ScheduledTaskService scheduledTaskService) {
        this.batchUpdateConfig = batchUpdateConfig;
        this.scheduledTaskService = scheduledTaskService;
        this.synchronousJobLauncher = synchronousJobLauncher;
        this.scheduledJobLauncher = scheduledJobLauncher;
    }

    public void runSynchronousUpdate(String entityId) throws Exception {
        logger.info("Triggering synchronous update for entityId={}", entityId);
        synchronousJobLauncher.run(batchUpdateConfig.updateSingleEntity(),
                BatchUtils.createJobParameters(entityId, Date.from(Instant.now())));
    }

    public void scheduleUpdates(List<String> entityIds, BatchUpdateType updateType) {
        if (CollectionUtils.isEmpty(entityIds)) {
            return;
        }
        logger.info("Scheduling async updates for entityIds={}", entityIds.toArray());
        scheduledTaskService.scheduleUpdateBulk(entityIds, updateType);
    }

    /**
     * Periodically run full entity updates.
     */
    @Scheduled(initialDelayString = "${batch.schedule.full.initialDelayMillis}",
            fixedDelayString = "${batch.schedule.fixedDelayMillis}")
    private void runScheduledFullUpdate() throws Exception {
        logger.info("Triggering scheduled full update for entities");
        scheduledJobLauncher.run(batchUpdateConfig.updateScheduledEntities(FULL),
                BatchUtils.createJobParameters(FULL, Date.from(Instant.now())));
    }

    /**
     * Periodically run metrics updates.
     */
    @Scheduled(initialDelayString = "${batch.schedule.metrics.initialDelayMillis}",
            fixedDelayString = "${batch.schedule.fixedDelayMillis}")
    private void runScheduledMetricsUpdate() throws Exception {
        logger.info("Triggering scheduled metrics update for entities");
        scheduledJobLauncher.run(batchUpdateConfig.updateScheduledEntities(METRICS),
                BatchUtils.createJobParameters(METRICS, Date.from(Instant.now())));
    }
}
