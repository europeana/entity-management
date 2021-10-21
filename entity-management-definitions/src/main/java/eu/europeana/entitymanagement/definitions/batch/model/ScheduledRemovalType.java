package eu.europeana.entitymanagement.definitions.batch.model;

public enum ScheduledRemovalType implements ScheduledTaskType {
  PERMANENT_DELETION("permanent_deletion"),
  DEPRECATION("deprecation");

  String value;

  ScheduledRemovalType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }
}
