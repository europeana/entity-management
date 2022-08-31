package eu.europeana.entitymanagement.utils;

import eu.europeana.entitymanagement.definitions.model.Entity;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityComparator implements Comparator<Entity>, Serializable {

  private static final long serialVersionUID = 4897357254139647262L;

  private static final Logger LOGGER = LogManager.getLogger(EntityComparator.class);

  private static final String MAP_ERROR_MESSAGE = "Map 2 contains different values for key {}!";

  @Override
  public int compare(Entity e1, Entity e2) {

    if (compareClass(e1, e2) > 0) {
      // verify for null values or different class
      return 1;
    }

    try {
      return compareContent(e1, e2);
    } catch (IllegalArgumentException e) {
      LOGGER.error(
          "During the comparison of the entity objects an illegal or inappropriate argument has been passed to some method.",
          e);
      return 1;
    } catch (IllegalAccessException e) {
      LOGGER.error(
          "During the comparison of the entity objects an illegal access to some field has been detected.",
          e);
      return 1;
    }
  }

  int compareClass(Object e1, Object e2) {
    if (e1 == null && e2 == null) {
      // both null
      return 0;
    } else if (e1 == null || e2 == null) {
      // only one of them is null
      return 1;
    } else if (!e1.getClass().equals(e2.getClass())) {
      // not same class
      return 1;
    }

    return 0;
  }

  @SuppressWarnings("unchecked")
  int compareContent(Entity e1, Entity e2) throws IllegalAccessException {

    List<Field> allObjectFieldsE1 = EntityUtils.getAllFields(e1.getClass());

    for (Field field : allObjectFieldsE1) {

      if (field.getName().startsWith("tmp")) {
        // ignore corelib adapter fields
        continue;
      }
      return compareEntityField(e1, e2, field);
    }
    return 0;
  }

  private int compareEntityField(Entity e1, Entity e2, Field field) throws IllegalAccessException {
    Class<?> fieldType = field.getType();

    if (fieldType.isArray()) {
      Object[] array1 = (Object[]) e1.getFieldValue(field);
      Object[] array2 = (Object[]) e2.getFieldValue(field);
      if (compareArrays(array1, array2) > 0) {
        return getCompareResult(field);
      }
    } else if (Map.class.isAssignableFrom(fieldType)) {
      Map<Object, Object> map1 = (Map<Object, Object>) e1.getFieldValue(field);
      Map<Object, Object> map2 = (Map<Object, Object>) e2.getFieldValue(field);
      if (compareMaps(map1, map2) > 0) {
        return getCompareResult(field);
      }
    } else if (List.class.isAssignableFrom(fieldType)) {
      List<Object> list1 = (List<Object>) e1.getFieldValue(field);
      List<Object> list2 = (List<Object>) e2.getFieldValue(field);
      if (compareLists(list1, list2) > 0) {
        return getCompareResult(field);
      }
    } else {
      Object obj1 = e1.getFieldValue(field);
      Object obj2 = e2.getFieldValue(field);
      if (compareClass(obj1, obj2) > 0) {
        // verify for null values or different class
        return getCompareResult(field);
      } else if (obj1 != null && !obj1.equals(obj2)) {
        getCompareResult(field);
      }
    }
    return 0;
  }

  private int getCompareResult(Field field) {
    LOGGER.trace("The values of the field {} are not equal!", field.getName());
    return 1;
  }

  int compareArrays(Object[] array1, Object[] array2) {

    List<Object> list1 = null, list2 = null;
    if (array1 != null) {
      list1 = Arrays.asList(array1);
    }
    if (array2 != null) {
      list2 = Arrays.asList(array2);
    }

    return compareLists(list1, list2);
  }

  private int compareLists(List<Object> l1, List<Object> l2) {
    if (CollectionUtils.isEmpty(l1) && CollectionUtils.isEmpty(l2)) {
      return 0;
    } else if (CollectionUtils.isEmpty(l1) || CollectionUtils.isEmpty(l2)) {
      return 1;
    }

    if (l1.size() != l2.size()) {
      LOGGER.trace("List size is not equal l1 size:{}, l2 size;{}!", l1.size(), l2.size());
      return 1;
    }

    for (Object l1Elem : l1) {
      if (!l2.contains(l1Elem)) {
        LOGGER.trace("List doesn't contain element:{}", l1Elem);
        return 1;
      }
    }

    return 0;
  }

  @SuppressWarnings("unchecked")
  private int compareMaps(Map<Object, Object> m1, Map<Object, Object> m2) {
    if (MapUtils.isEmpty(m1) && MapUtils.isEmpty(m2)) {
      // if both null or empty
      return 0;
    } else if (MapUtils.isEmpty(m1) || MapUtils.isEmpty(m2)) {
      // if only one map is null or empty
      return 1;
    }

    if (m1.size() != m2.size()) {
      // not same size
      LOGGER.trace("Map size is not equal m1 size:{}, m2 size;{}!", m1.size(), m2.size());
      return 1;
    }
    return getMapResults(m1, m2);
  }

  private int getMapResults(Map<Object, Object> m1, Map<Object, Object> m2) {
    for (Map.Entry<Object, Object> m1Elem : m1.entrySet()) {
      if (m2.containsKey(m1Elem.getKey())) {
        Object val1 = m1Elem.getValue();
        Object val2 = m2.get(m1Elem.getKey());
        if (List.class.isAssignableFrom(val1.getClass())) {
          // value is a list
          if (compareLists((List<Object>) val1, (List<Object>) val2) > 0) {
            LOGGER.trace(MAP_ERROR_MESSAGE, m1Elem.getKey());
            return 1;
          }
        } else {
          // value is an object
          if (!val1.equals(val2)) {
            LOGGER.trace(MAP_ERROR_MESSAGE, m1Elem.getKey());
            return 1;
          }
        }
      } else {
        LOGGER.trace("Map 2 doesn't contain key {}!", m1Elem.getKey());
        return 1;
      }
    }
    return 0;
  }
}
