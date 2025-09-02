package eu.europeana.entitymanagement.definitions.batch.codec;

import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

/** Codec provider for {@link TaskTypeCodec} */
public final class TaskTypeCodecProvider implements PropertyCodecProvider {

  @Override
  public <T> Codec<T> get(
      final TypeWithTypeParameters<T> type, final PropertyCodecRegistry propertyCodecRegistry) {
    Class<T> clazz = type.getType();
    if (TaskType.class.isAssignableFrom(clazz)) {
      new TaskTypeCodec();
    }
    return null;
  }
}
