package eu.europeana.entitymanagement.definitions.batch;

import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import java.util.HashMap;
import java.util.Map;

public class ScheduledTaskUtils {

  private static final Map<String, ScheduledTaskType> scheduledTaskTypeMap = new HashMap<>();

  static {
    // build map of ScheduledType string values to enum
    for (ScheduledUpdateType value : ScheduledUpdateType.values()) {
      scheduledTaskTypeMap.put(value.getValue(), value);
    }

    for (ScheduledRemovalType value : ScheduledRemovalType.values()) {
      scheduledTaskTypeMap.put(value.getValue(), value);
    }
  }

  public static ScheduledTaskType scheduledTaskTypeValueOf(String taskTypeString) {
    return scheduledTaskTypeMap.get(taskTypeString);
  }
}
