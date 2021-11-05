package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import java.io.IOException;

public class ScheduledTaskTypeSerializer extends JsonSerializer<ScheduledTaskType> {

  @Override
  public void serialize(
      ScheduledTaskType scheduledTaskType,
      JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(scheduledTaskType.getValue());
  }
}
