package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.entitymanagement.vocabulary.ScheduledTaskTypeFields;

@JsonDeserialize(as = ScheduledRemovalType.class)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public enum ScheduledRemovalType implements ScheduledTaskType {
  @JsonProperty(ScheduledTaskTypeFields.PERMANENT_DELETION)
  PERMANENT_DELETION("permanent_deletion"),
  DEPRECATION("deprecation");

  String value;

  ScheduledRemovalType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }
}
