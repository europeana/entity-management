package eu.europeana.entitymanagement;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(
    scanBasePackages = {"eu.europeana.entitymanagement"},
    exclude = {
      // Remove these exclusions to re-enable security
      SecurityAutoConfiguration.class,
      // DataSources are manually configured (for EM and batch DBs)
      DataSourceAutoConfiguration.class
    })
@EnableBatchProcessing
public class EntityManagementBatchApp {
  /**
   * Main entry point of this application
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        SpringApplication.run(EntityManagementBatchApp.class, args);
    System.exit(SpringApplication.exit(context));
  }
}
