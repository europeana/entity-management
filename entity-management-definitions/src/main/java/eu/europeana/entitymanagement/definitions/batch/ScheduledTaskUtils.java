package eu.europeana.entitymanagement.definitions.batch;

import eu.europeana.entitymanagement.definitions.batch.model.TaskType;

import java.util.HashMap;
import java.util.Map;

public class ScheduledTaskUtils {

  private static final Map<String, TaskType> taskTypeMap = new HashMap<>();

  static {
    // build map of ScheduledType string values to enum
    for (TaskType value : TaskType.values()) {
      taskTypeMap.put(value.getValue(), value);
    }
  }

  public static TaskType taskTypeValueOf(String taskTypeString) {
    return taskTypeMap.get(taskTypeString);
  }
}
