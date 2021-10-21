package eu.europeana.entitymanagement.definitions.batch.model;

public enum ScheduledUpdateType implements ScheduledTaskType {
  METRICS_UPDATE("metrics_update"),
  FULL_UPDATE("full_update");

  String value;

  ScheduledUpdateType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }
}
