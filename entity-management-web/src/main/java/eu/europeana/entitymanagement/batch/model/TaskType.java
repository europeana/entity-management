package eu.europeana.entitymanagement.batch.model;

public enum TaskType {

    FULL_UPDATE("full_update"),
    META_UPDATE("meta_update"),
    METRICS_UPDATE("metrics_update");

    final String value;

    TaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
