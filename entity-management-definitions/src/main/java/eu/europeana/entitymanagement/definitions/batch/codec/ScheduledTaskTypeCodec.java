package eu.europeana.entitymanagement.definitions.batch.codec;

import eu.europeana.entitymanagement.definitions.batch.ScheduledTaskUtils;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Implements codec to convert {@link ScheduledTaskType} values to/from corresponding BSON string
 * values.
 *
 * <p>Needs to be configured with when creating the {@link com.mongodb.client.MongoClient]} for
 * interacting with the database.
 */
public class ScheduledTaskTypeCodec implements Codec<ScheduledTaskType> {

  @Override
  public ScheduledTaskType decode(BsonReader reader, DecoderContext decoderContext) {
    return ScheduledTaskUtils.scheduledTaskTypeValueOf(reader.readString());
  }

  @Override
  public void encode(
      BsonWriter writer, ScheduledTaskType scheduledTaskType, EncoderContext encoderContext) {
    if (scheduledTaskType != null) {
      writer.writeString(scheduledTaskType.getValue());
    }
  }

  @Override
  public Class<ScheduledTaskType> getEncoderClass() {
    return ScheduledTaskType.class;
  }
}
