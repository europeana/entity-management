package eu.europeana.entitymanagement;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import eu.europeana.entitymanagement.batch.model.JobType;
import eu.europeana.entitymanagement.batch.service.BatchEntityUpdateExecutor;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.service.ZohoSyncService;

/**
 * Main application. Allows deploying as a war and logs instance data when deployed in Cloud Foundry
 */
@SpringBootApplication(scanBasePackages = {"eu.europeana.entitymanagement"}, exclude = {
    // Remove these exclusions to re-enable security
    SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
    // DataSources are manually configured (for EM and batch DBs)
    DataSourceAutoConfiguration.class})
public class EntityManagementApp implements CommandLineRunner {

  private static final Logger LOG = LogManager.getLogger(EntityManagementApp.class);
  private static final int WAITING_INTREVAL = 5;

  @Autowired
  private BatchEntityUpdateExecutor batchUpdateExecutor;
  @Autowired
  private ZohoSyncService zohoSyncService;
 
  /**
   * Main entry point of this application
   *
   * <p>
   * if the command line Argument - 'schedule_update' is passed then call EM Batch processing is
   * invoked.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    // jobType = args.length > 0 ? args[0] : "";
    if (isScheduledTask(args)) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Starting batch updates execution with args: {}", Arrays.toString(args));
      }
      validateArguments(args);
      // disable web server since we're only running an update task
      ConfigurableApplicationContext context = startStandAlloneApp(args);

      if (LOG.isInfoEnabled()) {
        LOG.info(
            "Batch scheduling was completed for {}, waiting for completion of asynchonuous processing ",
            Arrays.toString(args));
      }
      ScheduledTaskService scheduledTaskService = getScheduledTasksService(context);
      long notCompletedTasks = 0;
      boolean processingComplete = false;
      do {
        //wait for execution of schedules tasks
        long currentRunningTasks = scheduledTaskService.getRunningTasksCount();
        // log progress
        if (LOG.isInfoEnabled()) {
          LOG.info("Scheduled Tasks to process : {}", notCompletedTasks);
        }
        
        //failed tasks will not complete, therefore not all scheduled tasks are marked as completed in the database
        //untill we have a better mechanism to reschedule failed tasks we wait for the next executions to mark them as complete
        if (currentRunningTasks == 0 || currentRunningTasks == notCompletedTasks){
          //if the open tasks is the same after waiting interval, than the processing is considered complete
          //reseting currentRunningTasks is not needed anymore
          processingComplete = true;
        } else {
          processingComplete = false;
          notCompletedTasks = currentRunningTasks;
        }

        try {
          Thread.sleep(Duration.ofMinutes(WAITING_INTREVAL).toMillis());
        } catch (InterruptedException e) {
          LOG.error("Cannot complete execution!", e);
          SpringApplication.exit(context);
          System.exit(-2);
        }
      } while (!processingComplete);

      // failed application execution should be indicated with negative codes
      LOG.info("Stoping application after processing all Schdeduled Tasks!");
      System.exit(SpringApplication.exit(context));

    } else {
      LOG.info("No args provided to application. Starting web server");
      SpringApplication.run(EntityManagementApp.class, args);
      return;
    }
  }

  static boolean isScheduledTask(String[] args) {
    return hasCmdLineParams(args);
  }

  static ScheduledTaskService getScheduledTasksService(ConfigurableApplicationContext context) {
    return (ScheduledTaskService) context
        .getBean(AppConfigConstants.BEAN_BATCH_SCHEDULED_TASK_SERVICE);
  }

  static ConfigurableApplicationContext startStandAlloneApp(String[] args) {
    return new SpringApplicationBuilder(EntityManagementApp.class).web(WebApplicationType.NONE)
            .run(args);
  }

  static boolean hasCmdLineParams(String[] args) {
    return args != null && args.length > 0;
  }

  @Override
  public void run(String... args) throws Exception {
    if (isScheduledTask(args)) {
      runScheduledTasks(args);
    } 
    // if no arguments then web server should be started
    return;
  }


  void runScheduledTasks(String... args) throws EntityUpdateException {
    Set<String> tasks = Set.of(args);

    // first zoho sync as it runs synchronuous operations
    if (tasks.contains(JobType.ZOHO_SYNC.value())) {
      LOG.info("Executing zoho sync");
      ZohoSyncReport zohoSyncReport = zohoSyncService.synchronizeModifiedZohoOrganizations();
      LOG.info("Synchronization Report: {}", zohoSyncReport.toString());
    }

    if (tasks.contains(JobType.SCHEDULE_DELETION.value())) {
      // run also the deletions called through the API directly
      LOG.info("Executing scheduled deletions");
      batchUpdateExecutor.runScheduledDeprecationsAndDeletions();
      // TODO: should read the number of scheduled deletions and deprecations from the database
      // and write it to the logs
    }

    if (tasks.contains(JobType.SCHEDULE_UPDATE.value())) {
      LOG.info("Executing scheduled updates");
      batchUpdateExecutor.runScheduledUpdate();
      // TODO: should read the number of scheduled deletions and deprecations from the database
      // and write it to the logs
    }
  }

  /** validates the arguments passed */
  private static void validateArguments(String[] args) {
    for (String arg : args) {
      if (!JobType.isValidJobType(arg)) {
        String allowdJobTypes = JobType.values().toString();
        LOG.error("Unsupported argument '{}'. Supported arguments are '{}'", arg, allowdJobTypes);
        System.exit(1);
      }
    }
  }
}
