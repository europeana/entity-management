package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;

import java.io.IOException;

public class TaskTypeSerializer extends JsonSerializer<TaskType> {

  @Override
  public void serialize(
      TaskType taskType,
      JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(taskType.getValue());
  }
}
