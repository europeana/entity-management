package eu.europeana.entitymanagement;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_UPDATE_STATS;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_METRICS_UPDATE_STATS;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import eu.europeana.entitymanagement.batch.model.EntityUpdateStats;
import eu.europeana.entitymanagement.batch.model.JobType;
import eu.europeana.entitymanagement.batch.service.BatchEntityUpdateExecutor;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.service.EntitySynchronizationService;
import eu.europeana.entitymanagement.web.service.SlackConnection;

/**
 * Base class for Entity Synchronization cron jobs
 */
public class EntitySyncCronJob {

  private static final Logger LOGGER = LogManager.getLogger(EntitySyncCronJob.class);
  
  public static final String STATS_REPORT_MESSAGE =
      "%d entites were scheduled for %s with the following distribution: organizations: %d, agents: %d, concepts: %d, places: %d, timespans: %d.";
  public static final String STATS_REPORT_FAILED_MESSAGE =
      "failed update: %d See <%s|here> which entities have failed update. ";

  // slack json format including full update, metrics update and failed on separate text lines
  public static final String SYNC_REPORT_SLACK_MESSAGE = """
      {"text" : "%s%n%s%n%s"}""";

  @Autowired
  private BatchEntityUpdateExecutor batchUpdateExecutor;
  @Autowired
  private EntitySynchronizationService entitySyncService;
  @Autowired
  private EntityManagementConfiguration emConfiguration;
  @Autowired
  private EntityUpdateService entityUpdateService;
  @Resource(name = BEAN_ENTITY_UPDATE_STATS)
  private EntityUpdateStats entityUpdateStats;

  @Resource(name = BEAN_METRICS_UPDATE_STATS)
  private EntityUpdateStats metricsUpdateStats;

  static ScheduledTaskService getScheduledTasksService(ConfigurableApplicationContext context) {
    return (ScheduledTaskService) context
        .getBean(AppConfigConstants.BEAN_BATCH_SCHEDULED_TASK_SERVICE);
  }

  void performEntitySynchronizationWorkflow(Set<String> tasks) {
    // first sync organizations
    ZohoSyncReport report = runZohoSync(tasks);

    //send slack message for zoho sync
    if (report == null) {
      LOGGER.warn("Zoho sync was not scheduled or not successfully executed");
    } else {
      entitySyncService.publishReport(report);
    }

    // Schedule Tasks
    scheduleUpdateTasks();

    // execute tasks
    if (tasks.contains(JobType.SCHEDULE_DELETION.value())) {
      // run also the deletions called through the API directly
      LOGGER.info("Executing scheduled deletions");
      batchUpdateExecutor.runScheduledDeprecationsAndDeletions();
      // SG: should read the number of scheduled deletions and deprecations from the database
      // and write it to the LOGGERs
    }

    if (tasks.contains(JobType.SCHEDULE_UPDATE.value())) {
      LOGGER.info("Executing scheduled updates");
      batchUpdateExecutor.runScheduledTasks();
      // SG: should read the number of scheduled deletions and deprecations from the database
      // and write it to the LOGGERs
    }

    //send slack message for updates
    sendEntitySyncReport(entityUpdateStats, metricsUpdateStats);
  }

  private void sendEntitySyncReport(@NonNull EntityUpdateStats entityUpdateStats, @NonNull EntityUpdateStats metricsUpdateStats) {
    
    if (entityUpdateStats.getTotalEntitiesForUpdate() + metricsUpdateStats.getTotalEntitiesForUpdate() > 0) {
      String fullUpdateMessage = String.format(STATS_REPORT_MESSAGE,
          entityUpdateStats.getTotalEntitiesForUpdate(),
          TaskType.full_update,
          entityUpdateStats.getOrganizations(),
          entityUpdateStats.getAgents(),
          entityUpdateStats.getConcepts(),
          entityUpdateStats.getPlaces(),
          entityUpdateStats.getTimespans());
      
      String metricsUpdateMessage = String.format(STATS_REPORT_MESSAGE,
          metricsUpdateStats.getTotalEntitiesForUpdate(),
          TaskType.metrics_update,
          metricsUpdateStats.getOrganizations(),
          metricsUpdateStats.getAgents(),
          metricsUpdateStats.getConcepts(),
          metricsUpdateStats.getPlaces(),
          metricsUpdateStats.getTimespans());
      
      StringBuilder entityMangmtFailedUrl= new StringBuilder(emConfiguration.getEntityManagementBaseUrl());
      if (!emConfiguration.getEntityManagementBaseUrl().endsWith("/")) {
        entityMangmtFailedUrl.append('/');
      }    
      entityMangmtFailedUrl.append("entity/management/failed?pageSize=60");
      String failedMessage = String.format(STATS_REPORT_FAILED_MESSAGE, 
          entityUpdateStats.getFailed() + metricsUpdateStats.getFailed(), entityMangmtFailedUrl);
      
      String slackMessage = String.format(SYNC_REPORT_SLACK_MESSAGE, fullUpdateMessage, metricsUpdateMessage, failedMessage);
      
      SlackConnection slackConnection = new SlackConnection(emConfiguration.getEmUpdateSlackWebHook());
      slackConnection.publishStatusReport(slackMessage);
      
  } else {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Status report not sent !! As there are no entities were scheduled for update (full or metrics):  {}, \n {}", 
            entityUpdateStats, metricsUpdateStats);
      }
  }
    
  }

  void scheduleUpdateTasks() {
    Instant now = Instant.now();

    if (isExecuteFullUpdates(now)) {
      // schedule FULL Updates
      scheduleFullUpdates();
    } else {
      // schedule Metrics Update
      scheduleMetricsUpdates();
    }
  }

  ZohoSyncReport runZohoSync(Set<String> tasks) {
    // first zoho sync as it runs synchronuous operations
    if (tasks.contains(JobType.ZOHO_SYNC.value())) {
      LOGGER.info("Executing zoho sync");
      return entitySyncService.synchronizeModifiedZohoOrganizations();
    }
    return null;
  }

  protected boolean isExecuteFullUpdates(Instant now) {
    return now.atZone(ZoneId.systemDefault()).getDayOfWeek() == DayOfWeek
        .valueOf(emConfiguration.getBatchScheduleFullupdateDay().trim());
  }

  protected void scheduleFullUpdates() {
    if (StringUtils.isAllBlank(emConfiguration.getBatchScheduleFullupdateTypes())) {
      LOGGER.info(
          "Skipping scheduling of full updates for entities, no entity types configured for update");
      return;
    }

    String[] typesToUpdate = emConfiguration.getBatchScheduleFullupdateTypes().trim().split(",");
    scheduleTasks(TaskType.full_update, typesToUpdate);
  }

  void scheduleMetricsUpdates() {
    if (StringUtils.isAllBlank(emConfiguration.getBatchScheduleMetricsupdateTypes())) {
      LOGGER.info(
          "Skipping scheduling of metrics update for entities, no entity types configured for update");
      return;
    }

    String[] typesToUpdate = emConfiguration.getBatchScheduleMetricsupdateTypes().split(",");
    scheduleTasks(TaskType.metrics_update, typesToUpdate);
  }

  void scheduleTasks(TaskType taskType, String[] typesToUpdate) {
    // schedule for each entity type
    for (String entityType : typesToUpdate) {
      try {
        entityUpdateService.scheduleUpdatesWithSearch(EntitySolrFields.TYPE + ": " + entityType,
            taskType);
      } catch (SolrServiceException e) {
        LOGGER.warn("Cannot schedule updates ({}) for entity type:{}", taskType, entityType, e);
      }
    }
  }

}
