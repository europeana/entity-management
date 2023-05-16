package eu.europeana.entitymanagement.batch.model;

public enum JobType {
  SCHEDULE_UPDATE("schedule_update"),
  ZOHO_SYNC("zoho_sync"),
  SCHEDULE_DELETION("schedule_deletion");

  final String value;

  JobType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  /**
   * Verifies if the provided type is valid
   * @param type job type as string
   * @return true if valid type
   */
  public static boolean isValidJobType(String type) { 
    for (JobType jobType : JobType.values()) {
      if(jobType.value().equals(type)) {
        return true;
      }
    }
    return false;
  }  
  
  @Override
  public String toString() {
    return value();
  }
}
