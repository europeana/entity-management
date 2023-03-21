package eu.europeana.entitymanagement.batch.model;

public enum JobType {
  SCHEDULE_UPDATE("schedule_update"),
  SCHEDULE_DELETION("schedule_deletion");

  final String value;

  JobType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
