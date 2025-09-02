package eu.europeana.entitymanagement.definitions.batch.model;

import org.apache.commons.lang3.StringUtils;

@Deprecated
public enum ScheduledUpdateType implements ScheduledTaskType {
  METRICS_UPDATE("metrics_update"),
  FULL_UPDATE("full_update");

  String value;

  ScheduledUpdateType(String value) {
    this.value = value;
  }

  public static ScheduledUpdateType getType(String value) {
    for (ScheduledUpdateType type: ScheduledUpdateType.values()) {
      if (StringUtils.equals(type.getValue(), value)) {
        return type;
      }
    }
    return null;
  }

  @Override
  public String getValue() {
    return value;
  }
}
