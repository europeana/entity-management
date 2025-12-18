package eu.europeana.entitymanagement.batch.listener;

import static eu.europeana.entitymanagement.batch.utils.BatchUtils.getEntityIds;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.lang.NonNull;
import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.BatchEntityRecord;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;

/** Listens for Read, Processing and Write operations during Entity Update steps. */
public class ScheduledTaskItemListener
    extends ItemListenerSupport<BatchEntityRecord, BatchEntityRecord> {

  private static final Logger logger = LogManager.getLogger(ScheduledTaskItemListener.class);

  private final FailedTaskService failedTaskService;
  private final ScheduledTaskService scheduledTaskService;
  private final boolean isSynchronous;
  private final EntityUpdateStats fullUpdateStats;
  private final EntityUpdateStats metricUpdateStats;
  @Resource(name = AppConfigConstants.BEAN_ZOHO_CONFIGURATION)
  ZohoConfiguration zohoConfiguration;
  
  public ScheduledTaskItemListener(
          FailedTaskService failedTaskService,
          ScheduledTaskService scheduledTaskService,
          boolean isSynchronous, EntityUpdateStats fullUpdateStats, EntityUpdateStats metricUpdateStats) {
    this.failedTaskService = failedTaskService;
    this.scheduledTaskService = scheduledTaskService;
    this.isSynchronous = isSynchronous;
    this.fullUpdateStats = fullUpdateStats;
    this.metricUpdateStats = metricUpdateStats;
  }

  @Override
  public void afterRead(BatchEntityRecord item) {
    // update stats
    if(TaskType.hasStatsToCount(item.getScheduledTaskType())) {
      BatchUtils.selectStats(item.getScheduledTaskType(), fullUpdateStats, metricUpdateStats).updateEntityCounters(item);
    }
  }

  @Override
  public void afterWrite(@NonNull List<? extends BatchEntityRecord> entityRecords) {
    if (entityRecords.isEmpty()) {
      return;
    }
    
    // Remove full updates entries from the FailedTask collection if exists
    removeFailedTasks(entityRecords, TaskType.full_update);

    // Remove metrics update entries from the FailedTask collection if exists
    removeFailedTasks(entityRecords, TaskType.metrics_update);
    
    //remove also eventual organization registration failures, which are saved with external URL
    List<String> zohoUrls = BatchUtils.getZohoUrls(entityRecords, zohoConfiguration.getZohoBaseUrlOrganizations());
    failedTaskService.removeFailures(zohoUrls, TaskType.registration);

    // ScheduledTasks cleanup not required for synchronous execution
    if (!isSynchronous) {
      scheduledTaskService.markAsProcessed(
          entityRecords.stream()
              .collect(
                  Collectors.toMap(
                      p -> p.getEntityRecord().getEntityId(), p -> p.getScheduledTaskType())));
    }
  }

  @SuppressWarnings("unchecked")
  void removeFailedTasks(List<? extends BatchEntityRecord> entityRecords, TaskType taskType) {
    List<String> updatedEntityIds = getEntityIds((List<BatchEntityRecord>)entityRecords, taskType);
    if(!updatedEntityIds.isEmpty()) {
      failedTaskService.removeFailures(updatedEntityIds);
      
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Removed full_update Failed Tasks: entityIds={}, count={};", updatedEntityIds, updatedEntityIds.size());
      }  
    }
  }


  @Override
  public void onReadError(@NonNull Exception e) {
    // No entity linked to error, so we just log a warning
    logger.warn("onReadError", e);
  }

  @Override
  public void onProcessError(@NonNull BatchEntityRecord entityRecord, @NonNull Exception e) {
    String entityId = entityRecord.getEntityRecord().getEntityId();
    logger.warn("onProcessError: entityId={}", entityId, e);
    failedTaskService.persistFailure(entityId, entityRecord.getScheduledTaskType(), e);
    // update failed count in the stats
    if(TaskType.hasStatsToCount(entityRecord.getScheduledTaskType())) {
      BatchUtils.selectStats(entityRecord.getScheduledTaskType(), fullUpdateStats, metricUpdateStats).addFailed();
    }
  }

  @Override
  public void onWriteError(
      @NonNull Exception e, @NonNull List<? extends BatchEntityRecord> entityRecords) {
    //entityId, taskType map
    Map<String, TaskType> taskMap = entityRecords.stream()
    .collect(
        Collectors.toMap(
            r -> r.getEntityRecord().getEntityId(), r -> r.getScheduledTaskType()));
    
    logger.warn("onWriteError: entityIds={}", taskMap.keySet(), e);
    
    failedTaskService.persistFailureBulk(taskMap, e);
    // update failed count in the stats
    for (Map.Entry<String, TaskType> entry : taskMap.entrySet()) {
      if(TaskType.hasStatsToCount(entry.getValue())) {
        BatchUtils.selectStats(entry.getValue(), fullUpdateStats, metricUpdateStats).addFailed();
      }
    }
  }
}
