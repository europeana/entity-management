package eu.europeana.entitymanagement.definitions.batch.codec;

import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

/** Codec provider for {@link ScheduledTaskTypeCodec} */
public final class ScheduledTaskTypeCodecProvider implements PropertyCodecProvider {

  @Override
  public <T> Codec<T> get(
      final TypeWithTypeParameters<T> type, final PropertyCodecRegistry propertyCodecRegistry) {
    Class<T> clazz = type.getType();
    if (ScheduledTaskType.class.isAssignableFrom(clazz)) {
      new ScheduledTaskTypeCodec();
    }
    return null;
  }
}
