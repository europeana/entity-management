package eu.europeana.entitymanagement.testutils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * This class creates a Mongo container using the dockerfile in the docker-scripts directory.
 */
public class MongoContainer extends GenericContainer<MongoContainer> {

    private final String entityDb;
    private final String batchDb;
    private final String adminUsername = "admin_user";
    private final String adminPassword = "admin_password";


    /**
     * Creates a new Mongo container instance
     *
     * @param entityDb   entity database
     * @param batchDb    batch database
     */
    public MongoContainer(String entityDb, String batchDb) {

        this(new ImageFromDockerfile()
                        // in test/resources directory
                        .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
                        .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh"),
                entityDb, batchDb
        );

    }

    private MongoContainer(ImageFromDockerfile dockerImageName, String entityDb, String batchDb) {
        super(dockerImageName);

        this.withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername)
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword)
                .withEnv("EM_APP_DB", entityDb)
                .withEnv("EM_BATCH_DB", batchDb);

        this.waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));
        this.entityDb = entityDb;
        this.batchDb = batchDb;
    }

    public String getConnectionUrl() {
        if (!this.isRunning()) {
            throw new IllegalStateException("MongoDBContainer should be started first");
        } else {
            return String.format("mongodb://%s:%s@%s:%d", adminUsername, adminPassword, this.getContainerIpAddress(), this.getMappedPort(27017));
        }
    }

    public String getEntityDb() {
        return entityDb;
    }

    public String getBatchDb() {
        return batchDb;
    }
}