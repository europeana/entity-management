package eu.europeana.entitymanagement.definitions.batch.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.entitymanagement.serialization.ScheduledTaskTypeDeserializer;

@JsonDeserialize(using = ScheduledTaskTypeDeserializer.class)
public interface ScheduledTaskType {
  String getValue();
}
