package eu.europeana.entitymanagement;

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
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.service.ZohoSyncService;

/**
 * Main application. Allows deploying as a war and logs instance data when deployed in Cloud Foundry
 */
@SpringBootApplication(
    scanBasePackages = {"eu.europeana.entitymanagement"},
    exclude = {
      // Remove these exclusions to re-enable security
      SecurityAutoConfiguration.class,
      ManagementWebSecurityAutoConfiguration.class,
      // DataSources are manually configured (for EM and batch DBs)
      DataSourceAutoConfiguration.class
    })
public class EntityManagementApp implements CommandLineRunner {

  private static final Logger LOG = LogManager.getLogger(EntityManagementApp.class);
  @Autowired private BatchEntityUpdateExecutor batchUpdateExecutor;
  @Autowired private ZohoSyncService zohoSyncService;

  /**
   * Main entry point of this application
   *
   * <p>if the command line Argument - 'schedule_update' is passed then call EM Batch processing is
   * invoked.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    //jobType = args.length > 0 ? args[0] : "";
    if (hasCmdLineParams(args)) {
      LOG.info("Starting batch updates execution with args: {}", Arrays.toString(args));
      validateArguments(args);
      // disable web server since we're only running an update task
      ConfigurableApplicationContext context =
          new SpringApplicationBuilder(EntityManagementApp.class)
              .web(WebApplicationType.NONE)
              .run(args);

      LOG.info("Batch update execution complete for {}. Stopping application. ", Arrays.toString(args));
      System.exit(SpringApplication.exit(context));
    } else {
      LOG.info("No args provided to application. Starting web server");
      SpringApplication.run(EntityManagementApp.class, args);
      return;
    }
  }

  static boolean hasCmdLineParams(String[] args) {
    return args!=null && args.length > 0;
  }

  @Override
  public void run(String... args) throws Exception {
    if (hasCmdLineParams(args)) {
      Set<String> tasks = Set.of(args);
      if(tasks.contains(JobType.SCHEDULE_DELETION.value())) {
        LOG.info("Executing scheduled deletions");
        batchUpdateExecutor.runScheduledDeprecationsAndDeletions();
      }
      
      if(tasks.contains(JobType.ZOHO_SYNC.value())) {
        LOG.info("Executing zoho sync");
        ZohoSyncReport zohoSyncReport = zohoSyncService.synchronizeModifiedZohoOrganizations();
        LOG.info("Synchronization Report: {}", zohoSyncReport.toString());
      }
      
      if(tasks.contains(JobType.SCHEDULE_UPDATE.value())) {
        LOG.info("Executing scheduled updates");
        batchUpdateExecutor.runScheduledUpdate();
      }
        
    }
    // if no arguments then web server should be started
    return;
  }

  /** validates the arguments passed */
  private static void validateArguments(String[] args) {
    for (String arg : args) {
      if(!JobType.isValidJobType(arg)) {
        String allowdJobTypes = JobType.values().toString();
        LOG.error("Unsupported argument '{}'. Supported arguments are '{}'",
            arg, allowdJobTypes);
        System.exit(1);  
      }
    }
  }
}
