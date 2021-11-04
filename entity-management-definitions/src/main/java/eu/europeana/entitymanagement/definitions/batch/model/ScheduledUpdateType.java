package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.entitymanagement.vocabulary.ScheduledTaskTypeFields;

@JsonDeserialize(as = ScheduledUpdateType.class)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public enum ScheduledUpdateType implements ScheduledTaskType {
  METRICS_UPDATE("metrics_update"),
  @JsonProperty(ScheduledTaskTypeFields.FULL_UPDATE)
  FULL_UPDATE("full_update");

  String value;

  ScheduledUpdateType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }
}
