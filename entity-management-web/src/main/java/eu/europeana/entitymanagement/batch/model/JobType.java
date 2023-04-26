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
  
  public static boolean isValidJobType(String type) { 
    for (JobType jobType : JobType.values()) {
      if(jobType.value().equals(type)) {
        return true;
      }
    }
    return false;
  }  
}
