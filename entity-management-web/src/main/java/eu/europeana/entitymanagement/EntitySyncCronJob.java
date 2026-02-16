package eu.europeana.entitymanagement;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_ENTITY_UPDATE_STATS;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_METRICS_UPDATE_STATS;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
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
      "%d entites %s with the following distribution: organizations: %d, agents: %d, concepts: %d, places: %d, timespans: %d.";
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
  
  @Resource(name = AppConfigConstants.BEAN_BATCH_SCHEDULED_TASK_SERVICE)
  private ScheduledTaskService scheduledTaskService;
  
  /**
   * Method to support static access to the scheduled tasks service
   * @param context application context
   * @return the scheduled task service bean
   */
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
          "updated",
          entityUpdateStats.getOrganizations(),
          entityUpdateStats.getAgents(),
          entityUpdateStats.getConcepts(),
          entityUpdateStats.getPlaces(),
          entityUpdateStats.getTimespans());
      
      String metricsUpdateMessage = String.format(STATS_REPORT_MESSAGE,
          metricsUpdateStats.getTotalEntitiesForUpdate(),
          "updated for metrics",
          metricsUpdateStats.getOrganizations(),
          metricsUpdateStats.getAgents(),
          metricsUpdateStats.getConcepts(),
          metricsUpdateStats.getPlaces(),
          metricsUpdateStats.getTimespans());
      
      StringBuilder entityMangmtFailedUrl = entitySyncService.buildFailedTasksUrl(null);
      String failedMessage = String.format(STATS_REPORT_FAILED_MESSAGE, 
          entityUpdateStats.getFailed() + metricsUpdateStats.getFailed(), entityMangmtFailedUrl);
      
      String slackMessage = String.format(SYNC_REPORT_SLACK_MESSAGE, fullUpdateMessage, metricsUpdateMessage, failedMessage);
      
      SlackConnection slackConnection = new SlackConnection(emConfiguration.getEmUpdateSlackWebHook());
      slackConnection.publishStatusReport(slackMessage);
      
  } else {
    String slackMessage = String.format(SYNC_REPORT_SLACK_MESSAGE,
        "No entities have been scheduled for update (full or metrics)!", "updated: 0", "updated for metrics: 0");
    
    SlackConnection slackConnection = new SlackConnection(emConfiguration.getEmUpdateSlackWebHook());
    slackConnection.publishStatusReport(slackMessage);
    if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Slack message sent: No entities have been scheduled for update ( or metrics):  {}, \n {}", 
            entityUpdateStats, metricsUpdateStats);
      }
  }
    
  }


  /**
   * Schedules task based on the configurations
   *
   * 1. Full updates : These are exceuted monthly on the date configured via
   *                   property 'batch.schedule.monthly.full.update.date'.
   *                   By default date is set to 1 of month
   * 2. Metrics update : these are scheduled to run weekly, By default is set for sunday.
   *                     But is configurable via property 'batch.schedule.metrics.update.day'
   */
  void scheduleUpdateTasks() {
    
    //remove completed tasks first, otherwise we cannot schedule metrics executions (type will stay full_update) #EA-4308
    scheduledTaskService.removeProcessedTasks(List.of(TaskType.full_update, TaskType.metrics_update));

    ZonedDateTime dateTime = ZonedDateTime.now();
    if (executeMonthlyFullUpdate(dateTime)) {
      // schedule FULL Updates
      LOGGER.info("{} day of the Month [{}]. Will schedule monthly full updates.",
              emConfiguration.getBatchScheduleMonthlyFullUpdateDate(), dateTime);
      scheduleFullUpdates();
    }
    if (executeMetricsUpdates(dateTime)) {
      // schedule Metrics Update
      LOGGER.info("Today is [{}]. Will schedule weekly metrics updates.", dateTime.getDayOfWeek());
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

  /**
   * Returns true if day of the month matches with configured date
   * @param dateTime current system date
   * @return true if matches
   */
  private boolean executeMonthlyFullUpdate(ZonedDateTime dateTime) {
    return (dateTime.getDayOfMonth() == emConfiguration.getBatchScheduleMonthlyFullUpdateDate());
  }

  /**
   * Returns true if the day of week matches configured day
   * @param dateTime current system date
   * @return true if matches
   */
  protected boolean executeMetricsUpdates(ZonedDateTime dateTime) {
    return (dateTime.getDayOfWeek() ==
        DayOfWeek.valueOf(emConfiguration.getBatchScheduleMetricsUpdateDay().trim()));
  }

  /**
   * Schedules full update for the types configured
   */
  protected void scheduleFullUpdates() {
    if (StringUtils.isAllBlank(emConfiguration.getBatchScheduleFullUpdateTypes())) {
      LOGGER.info(
          "Skipping scheduling of full updates for entities, no entity types configured for update");
      return;
    }

    LOGGER.info(
            "Scheduling full updates for entity types : {}",
            emConfiguration.getBatchScheduleFullUpdateTypes());
    String[] entityTypes = emConfiguration.getBatchScheduleFullUpdateTypes().trim().split(",");
    scheduleTasks(TaskType.full_update, entityTypes);
  }

  /**
   * Schedules metrics update for the types configured
   */
  protected void scheduleMetricsUpdates() {
    if (StringUtils.isAllBlank(emConfiguration.getBatchScheduleMetricsUpdateTypes())) {
      LOGGER.info(
          "Skipping scheduling of metrics update for entities, no entity types configured for update");
      return;
    }
    LOGGER.info(
            "Scheduling full updates for entity types : {}",
            emConfiguration.getBatchScheduleMetricsUpdateTypes());

    String[] typesToUpdate = emConfiguration.getBatchScheduleMetricsUpdateTypes().split(",");
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
