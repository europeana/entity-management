package eu.europeana.entitymanagement.definitions.batch.codec;

import eu.europeana.entitymanagement.definitions.batch.ScheduledTaskUtils;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Implements codec to convert {@link TaskType} values to/from corresponding BSON string
 * values.
 *
 * <p>Needs to be configured with when creating the {@link com.mongodb.client.MongoClient]} for
 * interacting with the database.
 */
public class TaskTypeCodec implements Codec<TaskType> {

  @Override
  public TaskType decode(BsonReader reader, DecoderContext decoderContext) {
    return ScheduledTaskUtils.taskTypeValueOf(reader.readString());
  }

  @Override
  public void encode(
      BsonWriter writer, TaskType taskType, EncoderContext encoderContext) {
    if (taskType != null) {
      writer.writeString(taskType.getValue());
    }
  }

  @Override
  public Class<TaskType> getEncoderClass() {
    return TaskType.class;
  }
}
