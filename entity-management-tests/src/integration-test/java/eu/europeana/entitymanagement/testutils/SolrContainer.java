package eu.europeana.entitymanagement.testutils;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

/** Creates a docker container for Solr using the dockerfile in docker-scripts directory */
public class SolrContainer extends GenericContainer<SolrContainer> {

  private static final int DEFAULT_SOLR_PORT = 8983;
  private final String entityManagementCore;
  private final boolean useFixedPorts = false;

  public SolrContainer(String entityManagementCore) {
    this(
        new ImageFromDockerfile()
            // in test/resources directory
            .withFileFromClasspath("Dockerfile", "solr-docker/Dockerfile")
            .withFileFromClasspath("solr-entrypoint.sh", "solr-docker/solr-entrypoint.sh")
            .withFileFromClasspath("conf", "solr-docker/conf/")
            .withFileFromClasspath("accent-map.txt", "solr-docker/conf/accent-map.txt")
            .withFileFromClasspath("schema.xml", "solr-docker/conf/schema.xml")
            .withFileFromClasspath("solrconfig.xml", "solr-docker/conf/solrconfig.xml"),
        entityManagementCore);
  }

  private SolrContainer(ImageFromDockerfile dockerImageName, String entityManagementCore) {
    super(dockerImageName);
    if (useFixedPorts) {
      this.addFixedExposedPort(DEFAULT_SOLR_PORT, DEFAULT_SOLR_PORT);
    } else {
      this.withExposedPorts(DEFAULT_SOLR_PORT);
    }

    this.withEnv("EM_INDEXING_CORE", entityManagementCore);
    this.waitStrategy =
        new LogMessageWaitStrategy()
            .withRegEx(".*o\\.e\\.j\\.s\\.Server Started.*")
            .withStartupTimeout(Duration.of(60, SECONDS));

    this.entityManagementCore = entityManagementCore;
  }

  public String getConnectionUrl() {
    if (!this.isRunning()) {
      throw new IllegalStateException("Solr container should be started first");
    } else {
      return String.format(
          "http://%s:%d/solr/%s",
          this.getContainerIpAddress(),
          this.getMappedPort(DEFAULT_SOLR_PORT),
          entityManagementCore);
    }
  }
}
