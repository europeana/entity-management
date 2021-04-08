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
    private final String enrichmentDb;
    private final String adminUsername = "admin_user";
    private final String adminPassword = "admin_password";
    private boolean useFixedPorts = false;


    /**
     * Creates a new Mongo container instance
     *
     * @param entityDb   entity database
     * @param batchDb    batch database
     */
    public MongoContainer(String entityDb, String batchDb, String enrichmentDb) {

        this(new ImageFromDockerfile()
                        // in test/resources directory
                        .withFileFromClasspath("Dockerfile", "mongo-docker/Dockerfile")
                        .withFileFromClasspath("init-mongo.sh", "mongo-docker/init-mongo.sh"),
                entityDb, batchDb, enrichmentDb
        );

    }

    private MongoContainer(ImageFromDockerfile dockerImageName, String entityDb, String batchDb, String enrichmentDb) {
        super(dockerImageName);

        if(useFixedPorts){
            this.addFixedExposedPort(27017,27017);     
        }else{
            this.withExposedPorts(27017);    
        }
        
        this.withEnv("MONGO_INITDB_ROOT_USERNAME", adminUsername)
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", adminPassword)
                .withEnv("EM_APP_DB", entityDb)
                .withEnv("EM_BATCH_DB", batchDb)
                .withEnv("ENRICHMENT_DB", enrichmentDb);

        this.waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));
        this.entityDb = entityDb;
        this.batchDb = batchDb;
        this.enrichmentDb = enrichmentDb;
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

    public String getEnrichmentDb() {
        return enrichmentDb;
    }
}