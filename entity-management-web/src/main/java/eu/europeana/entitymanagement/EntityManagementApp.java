package eu.europeana.entitymanagement;

import static eu.europeana.entitymanagement.batch.model.JobType.SCHEDULE_DELETION;
import static eu.europeana.entitymanagement.batch.model.JobType.SCHEDULE_UPDATE;
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
import eu.europeana.entitymanagement.common.config.SocksProxyConfig;
import eu.europeana.entitymanagement.config.SocksProxyActivator;

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

  /**
   * Main entry point of this application
   *
   * <p>if the command line Argument - 'schedule_update' is passed then call EM Batch processing is
   * invoked.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    
    // Activate socks proxy (if your application requires it)
    SocksProxyActivator.activate(
        new SocksProxyConfig("entitymanagement.properties", "entitymanagement.user.properties"));
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
        LOG.debug("Executing scheduled deletions");
        batchUpdateExecutor.runScheduledDeprecationsAndDeletions();
      }
      
      if(tasks.contains(JobType.SCHEDULE_UPDATE.value())) {
        LOG.debug("Executing scheduled updates");
        batchUpdateExecutor.runScheduledDeprecationsAndDeletions();
      }
        batchUpdateExecutor.runScheduledUpdate();
    }
    // if no arguments then web server should be started
    return;
  }

  /** validates the arguments passed */
  private static void validateArguments(String[] args) {
    for (String arg : args) {
      if(!JobType.isValidJobType(arg)) {
        LOG.error("Unsupported argument '{}'. Supported arguments are '{}' or '{}'",
            arg, SCHEDULE_DELETION.value(),
            SCHEDULE_UPDATE.value());
        System.exit(1);  
      }
    }
  }
}
