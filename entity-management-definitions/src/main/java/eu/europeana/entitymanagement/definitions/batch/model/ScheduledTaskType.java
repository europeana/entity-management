package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.entitymanagement.serialization.ScheduledTaskTypeSerializer;

@JsonSerialize(using = ScheduledTaskTypeSerializer.class)
public interface ScheduledTaskType {

  String getValue();
}
