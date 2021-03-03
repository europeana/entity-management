package eu.europeana.entitymanagement.testutils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * This class creates a Mongo container using the dockerfile in the docker-scripts directory.
 */
public class MongoContainer extends GenericContainer<MongoContainer> {


    private final String dbUser;
    private final String dbPassword;

    private final String entityDb;
    private final String batchDb;
    private final String adminUsername = "admin_user";
    private final String adminPassword = "admin_password";


    /**
     * Creates a new Mongo container instance
     *
     * @param dbUser     database username
     * @param dbPassword database password
     * @param entityDb   entity database
     * @param batchDb    batch database
     */
    public MongoContainer(String dbUser, String dbPassword, String entityDb, String batchDb) {

        this(new ImageFromDockerfile()
                        // in test/resources directory
                        .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
                        .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh"),
                dbUser, dbPassword, entityDb, batchDb
        );

    }

    private MongoContainer(ImageFromDockerfile dockerImageName, String dbUser, String dbPassword, String entityDb, String batchDb) {
        super(dockerImageName);

        this.withExposedPorts(27017)
                .withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername)
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword)
                .withEnv("EM_DB_USER", dbUser)
                .withEnv("EM_DB_PASSWORD", dbPassword)
                .withEnv("EM_APP_DB", entityDb)
                .withEnv("EM_BATCH_DB", batchDb);

        this.waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.entityDb = entityDb;
        this.batchDb = batchDb;
    }

    public String getConnectionUrl() {
        if (!this.isRunning()) {
            throw new IllegalStateException("MongoDBContainer should be started first");
        } else {
            // TODO: figure out why user/password from ENV not set; use admin credentials
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
