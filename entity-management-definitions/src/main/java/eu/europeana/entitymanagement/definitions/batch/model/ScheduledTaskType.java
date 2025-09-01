package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.entitymanagement.serialization.TaskTypeSerializer;

@JsonSerialize(using = TaskTypeSerializer.class)
public interface ScheduledTaskType {

  String getValue();
}
