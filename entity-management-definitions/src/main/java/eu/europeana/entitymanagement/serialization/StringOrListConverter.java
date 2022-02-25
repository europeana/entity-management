package eu.europeana.entitymanagement.serialization;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Some Agent fields are serialized as String (in consolidated entity) or List (in proxy entities).
 * Within the Agent class, these fields are implemented as Lists.
 *
 * <p>This converter ensures proper serialization for the affected fields.
 */
public class StringOrListConverter extends StdConverter<Object, List<String>> {

  @Override
  public List<String> convert(Object o) {
    List<String> list = new ArrayList<>();

    if (String.class.isAssignableFrom(o.getClass())) {
      list.add((String) o);
    } else if (List.class.isAssignableFrom(o.getClass())) {
      list.addAll(((List<?>) o).stream().map(Object::toString).collect(Collectors.toList()));
    }

    return list;
  }
}
