package eu.europeana.entitymanagement.batch;

public enum JobParameter {
    RUN_ID ("runId");

    private final String key;
    JobParameter(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
