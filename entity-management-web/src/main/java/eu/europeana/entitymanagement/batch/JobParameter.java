package eu.europeana.entitymanagement.batch;

/**
 * Parameters for triggering jobs
 */
public enum JobParameter {
    CURRENT_START_TIME("currentStartTime"),
    PREVIOUS_START_TIME("previousStartTime"),
    ENTITY_ID("entityId");

    private final String key;

    JobParameter(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
