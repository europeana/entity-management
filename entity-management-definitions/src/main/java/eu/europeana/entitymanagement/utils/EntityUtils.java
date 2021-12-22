package eu.europeana.entitymanagement.utils;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityUtils {

  public static String createWikimediaResourceString(String wikimediaCommonsId) {
    assert wikimediaCommonsId.contains("Special:FilePath/");
    return wikimediaCommonsId.replace("Special:FilePath/", "File:");
  }

  public static String toGeoUri(String latLon) {
    return WebEntityConstants.PROTOCOL_GEO + latLon;
  }

  // getting all fields of the class including the inherited ones
  public static List<Field> getAllFieldsIncludingInherited(Class<?> entityType) {
    List<Field> entityFields = new ArrayList<Field>();
    getAllFieldsIncludingInheritedRecursively(entityFields, entityType);
    return entityFields;
  }

  private static void getAllFieldsIncludingInheritedRecursively(
      List<Field> fields, Class<?> entityType) {
    fields.addAll(Arrays.asList(entityType.getDeclaredFields()));
    if (entityType.getSuperclass() != null) {
      getAllFieldsIncludingInheritedRecursively(fields, entityType.getSuperclass());
    }
  }

  /*
   * Getting all entity fields including the inherited and the nested object fields.
   * Together with the field, their values are returned. This needs to be done here,
   * since there can be nested fields whose values cannot be obtained later, based
   * solely on the field, but the field object instance would be needed.
   */
  public static void getAllFieldsIncludingInheritedAndNested(
      List<Field> fields, List<Object> fieldsValues, Entity entity)
      throws IllegalArgumentException, IllegalAccessException {
    List<Field> entityFields = getAllFieldsIncludingInherited(entity.getClass());
    for (Field entityFieldElem : entityFields) {
      Object entityFieldValue = EntityFieldsTypes.getFieldValue(entity, entityFieldElem);
      getAllFieldsIncludingInheritedAndNestedRecursively(
          fields, fieldsValues, entityFieldElem, entityFieldValue);
    }
  }

  /*
   * Getting all entity fields that belong to the object type fields,
   * including the inherited and the nested fields.
   */
  public static void getObjectsFieldsIncludingInheritedAndNested(
      List<Field> fields, List<Object> fieldsValues, Entity entity)
      throws IllegalArgumentException, IllegalAccessException {
    List<Field> entityFields = getAllFieldsIncludingInherited(entity.getClass());
    for (Field entityFieldElem : entityFields) {
      Object entityFieldValue = EntityFieldsTypes.getFieldValue(entity, entityFieldElem);
      if (entityFieldValue != null
          && EntityFieldsTypes.hasClassTypeOfField(entityFieldValue.getClass())) {
        getAllFieldsIncludingInheritedAndNestedRecursively(
            fields, fieldsValues, entityFieldElem, entityFieldValue);
      }
    }
  }

  private static void getAllFieldsIncludingInheritedAndNestedRecursively(
      List<Field> fieldsReturn, List<Object> fieldsValuesReturn, Field field, Object fieldValue)
      throws IllegalArgumentException, IllegalAccessException {
    /*
     * if the fieldValue is an object, get its nested fields, but avoid getting the fields
     * for the objects like String, Integer, etc. That is why we get the nested fields
     * for the objects of the type Class, which appears to be a field type.
     */
    if (fieldValue != null && EntityFieldsTypes.hasClassTypeOfField(fieldValue.getClass())) {
      List<Field> objectFields = getAllFieldsIncludingInherited(fieldValue.getClass());
      if (objectFields.size() > 0) {
        for (Field objectFieldElem : objectFields) {
          Object objectFieldValue = EntityFieldsTypes.getFieldValue(fieldValue, objectFieldElem);
          if (objectFieldValue != null) {
            getAllFieldsIncludingInheritedAndNestedRecursively(
                fieldsReturn, fieldsValuesReturn, objectFieldElem, objectFieldValue);
          } else {
            fieldsReturn.add(objectFieldElem);
            fieldsValuesReturn.add(objectFieldValue);
          }
        }
      }
    } else {
      fieldsReturn.add(field);
      fieldsValuesReturn.add(fieldValue);
    }
  }
}
