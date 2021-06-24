package eu.europeana.entitymanagement.batch;

/**
 * Parameters for triggering jobs
 */
public enum JobParameter {
    CURRENT_START_TIME("currentStartTime"),
    ENTITY_ID("entityId"),
    UPDATE_TYPE("updateType");

    private final String key;

    JobParameter(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
