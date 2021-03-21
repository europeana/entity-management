package eu.europeana.entitymanagement.batch;

/**
 * Parameters for triggering jobs
 */
public enum JobParameter {
    RUN_TIME("runTime"),
    ENTITY_ID("entityId");

    private final String key;

    JobParameter(String key) {
        this.key = key;
    }
    public String key() {
        return key;
    }
}
