package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.entitymanagement.serialization.TaskTypeSerializer;
import org.apache.commons.lang3.StringUtils;

@JsonSerialize(using = TaskTypeSerializer.class)
public enum TaskType {

    FULL_UPDATE("full_update"),
    META_UPDATE("meta_update"),
    METRICS_UPDATE("metrics_update"),
    PERMANENT_DELETION("permanent_deletion"),
    DEPRECATION("deprecation");

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
}
