package eu.europeana.entitymanagement.batch.model;

public enum JobType {
  SCHEDULE_UPDATES("schedule_update");

  final String value;

  JobType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
