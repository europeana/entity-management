package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.vocabulary.ScheduledTaskTypeFields;
import java.io.IOException;

public class ScheduledTaskTypeDeserializer extends JsonDeserializer<ScheduledTaskType> {
  @Override
  public ScheduledTaskType deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    ObjectNode root = (ObjectNode) mapper.readTree(jp);
    if (root.has(ScheduledTaskTypeFields.FULL_UPDATE)) {
      return mapper.readValue(root.toString(), ScheduledUpdateType.class);
    } else {
      return mapper.readValue(root.toString(), ScheduledRemovalType.class);
    }
  }
}
