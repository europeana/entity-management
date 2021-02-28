package eu.europeana.entitymanagement.testutils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * This class creates a Mongo container, similar to {@link org.testcontainers.containers.MongoDBContainer},
 * using the dockerfile in the docker-scripts directory.
 */
public class MongoContainer extends GenericContainer<MongoContainer> {

    public MongoContainer() {
        this(new ImageFromDockerfile()
                // in test/resources directory
                .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
                .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh")

        );

    }

    private MongoContainer(ImageFromDockerfile dockerImageName) {
        super(dockerImageName);
        this.withExposedPorts(27017);
        this.waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));
    }

    public String getConnectionUrl() {
        if (!this.isRunning()) {
            throw new IllegalStateException("MongoDBContainer should be started first");
        } else {
            return String.format("mongodb://%s:%d", this.getContainerIpAddress(), this.getMappedPort(27017));
        }
    }


}
