package eu.europeana.entitymanagement.definitions.batch.model;

import org.apache.commons.lang3.StringUtils;

public enum TaskType {

    full_update("full_update"),
    meta_update("meta_update"),
    metrics_update("metrics_update"),
    permanent_deletion("permanent_deletion"),
    deprecation("deprecation"),
    registration("registration");

    final String value;

    TaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TaskType getType(String value) {
        for (TaskType type: TaskType.values()) {
            if (StringUtils.equals(type.getValue(), value)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Indicates if metrics need to be collected for the given task type
     * @param taskType the type
     * @return true for metrics update and full update
     */
    public static boolean hasStatsToCount(TaskType taskType) {
      return full_update == taskType || metrics_update == taskType;
    }
}
