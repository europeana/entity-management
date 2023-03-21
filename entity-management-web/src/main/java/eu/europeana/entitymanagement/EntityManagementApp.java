package eu.europeana.entitymanagement;

import static eu.europeana.entitymanagement.batch.model.JobType.SCHEDULE_DELETION;
import static eu.europeana.entitymanagement.batch.model.JobType.SCHEDULE_UPDATE;

import eu.europeana.entitymanagement.batch.config.EntityUpdateSchedulingConfig;
import eu.europeana.entitymanagement.common.config.SocksProxyConfig;
import eu.europeana.entitymanagement.config.SocksProxyActivator;
import org.apache.commons.lang3.StringUtils;
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
  private static String jobType = "";

  @Autowired private EntityUpdateSchedulingConfig schedulingConfig;

  /**
   * Main entry point of this application
   *
   * <p>if the command line Argument - 'schedule_update' is passed then call EM Batch processing is
   * invoked.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    jobType = args.length > 0 ? args[0] : "";

    // Activate socks proxy (if your application requires it)
    SocksProxyActivator.activate(
        new SocksProxyConfig("entitymanagement.properties", "entitymanagement.user.properties"));

    if (StringUtils.isNotEmpty(jobType)) {
      validateArguments();
      // disable web server since we're only running an update task
      ConfigurableApplicationContext context =
          new SpringApplicationBuilder(EntityManagementApp.class)
              .web(WebApplicationType.NONE)
              .run(args);

      System.exit(SpringApplication.exit(context));
    } else {
      LOG.info("No args provided to application. Starting web server");
      SpringApplication.run(EntityManagementApp.class, args);
      return;
    }
  }

  @Override
  public void run(String... args) throws Exception {
    if (StringUtils.isNotEmpty(jobType)) {
      if (StringUtils.equalsIgnoreCase(jobType, SCHEDULE_UPDATE.value())) {
        LOG.info("Started the Entity Management Batch Application for scheduled entity update");
        schedulingConfig.runScheduledUpdate();
      }
      if (StringUtils.equalsIgnoreCase(jobType, SCHEDULE_DELETION.value())) {
        LOG.info("Started the Entity Management Batch Application for scheduled entity deletion");
        schedulingConfig.runScheduledDeprecationsAndDeletions();
      }
    }
    // if no arguments then web server should be started
    return;
  }

  /** validates the arguments passed */
  private static void validateArguments() {
    if (StringUtils.isNotEmpty(jobType)
        && !(SCHEDULE_UPDATE.value().equalsIgnoreCase(jobType)
            || SCHEDULE_DELETION.value().equalsIgnoreCase(jobType))) {
      LOG.error(
          "Unsupported argument '{}'. Supported arguments are '{}' or '{}'",
          jobType,
          SCHEDULE_UPDATE.value(),
          SCHEDULE_DELETION.value());
      System.exit(1);
    }
  }
}
