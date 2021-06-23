package eu.europeana.entitymanagement.batch;


import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.SYNC_JOB_LAUNCHER;

@Service
public class BatchService {
    private static final Logger logger = LogManager.getLogger(BatchService.class);

    private final BatchEntityUpdateConfig batchUpdateConfig;
    private final JobLauncher synchronousJobLauncher;
    private final ScheduledTaskService scheduledTaskService;

    @Autowired
    public BatchService(BatchEntityUpdateConfig batchUpdateConfig,
                        @Qualifier(SYNC_JOB_LAUNCHER) JobLauncher synchronousJobLauncher,
                        ScheduledTaskService scheduledTaskService) {
        this.batchUpdateConfig = batchUpdateConfig;
        this.scheduledTaskService = scheduledTaskService;
        this.synchronousJobLauncher = synchronousJobLauncher;
    }

    public void runSynchronousUpdate(String entityId) throws Exception {
        logger.info("Triggering synchronous update for entityId={}", entityId);
        synchronousJobLauncher.run(batchUpdateConfig.updateSingleEntityJob(),
                        BatchUtils.createJobParameters(entityId, Date.from(Instant.now())));
    }


    public void scheduleUpdates(List<String> entityIds, BatchUpdateType updateType) {
        if(CollectionUtils.isEmpty(entityIds)){
            return;
        }
        logger.info("Scheduling async updates for entityIds={}", entityIds.toArray());
            scheduledTaskService.scheduleUpdateBulk(entityIds, updateType);
    }
}
