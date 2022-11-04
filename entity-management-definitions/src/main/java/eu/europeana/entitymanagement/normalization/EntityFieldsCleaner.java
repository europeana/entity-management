package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.*;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BEGIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_BIRTH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_DEATH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_ESTABLISHMENT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DATE_OF_TERMINATION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.END;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.GENDER;

import eu.europeana.entitymanagement.definitions.LanguageCodes;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.ConsolidatedAgent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityFieldsCleaner {

  private static final Set<String> ISO_LANGUAGES = Set.of(Locale.getISOLanguages());

  private static final Logger logger = LogManager.getLogger(EntityFieldsCleaner.class);

  private final LanguageCodes emLanguageCodes;

  private final String thumbnailBaseUrl;

  /**
   * Mapping between Entity classes and List<String> fields that should contain a single entity.
   * This is only applicable for the consolidated entity, where we want to serialize certain fields
   * as String but save them to the database as List<String>
   *
   * <p>List values must match field names in the Entity class.
   */
  private final Map<Class<? extends Entity>, List<String>> SINGLE_ELEMENT_LIST_FIELDS =
      Map.of(
          ConsolidatedAgent.class,
          List.of(
              BEGIN,
              END,
              DATE_OF_BIRTH,
              DATE_OF_DEATH,
              DATE_OF_ESTABLISHMENT,
              DATE_OF_TERMINATION,
              GENDER));

  public EntityFieldsCleaner(LanguageCodes emLanguageCodes, String thumbnailBaseUrl) {
    this.emLanguageCodes = emLanguageCodes;
    this.thumbnailBaseUrl = thumbnailBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public void cleanAndNormalize(Entity entity) {
    List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());

    try {
      for (Field field : entityFields) {
        Object fieldValue = entity.getFieldValue(field);
        if (fieldValue == null) continue;
        Class<?> fieldType = field.getType();
        if (fieldType.isAssignableFrom(String.class)) {
          // remove spaces from the String fields
          normalizeTextField(field, (String) fieldValue, entity);
        } else if (fieldType.isAssignableFrom(String[].class)) {
          // remove spaces from the String[] fields
          List<String> normalizedList =
              normalizeValues(field.getName(), Arrays.asList((String[]) fieldValue));
          String[] normalized = normalizedList.toArray(new String[0]);
          entity.setFieldValue(field, normalized);
        } else if (fieldType.isAssignableFrom(List.class)) {
          List<String> fieldValueList = (List<String>) fieldValue;
          if (fieldValueList.isEmpty()) {
            continue;
          }

          // remove spaces from the List<String> fields
          List<String> normalizedList = normalizeValues(field.getName(), fieldValueList);

          // if Entity field is supposed to contain a single element, remove all elements
          // except the
          // first
          if (SINGLE_ELEMENT_LIST_FIELDS.containsKey(entity.getClass())
              && SINGLE_ELEMENT_LIST_FIELDS.get(entity.getClass()).contains(field.getName())
              && normalizedList.size() > 1) {
            normalizedList.subList(1, normalizedList.size()).clear();
          }
          entity.setFieldValue(field, normalizedList);

        } else if (fieldType.isAssignableFrom(Map.class)) {
          @SuppressWarnings("rawtypes")
          Map normalized = normalizeMapField(field, (Map) fieldValue);
          entity.setFieldValue(field, normalized);
        } else if (WebResource.class.isAssignableFrom(fieldType)) {
          entity.setFieldValue(field, normalizeWebResource((WebResource) fieldValue));
        }
      }
    } catch (IllegalArgumentException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException
        | InstantiationException e) {
      throw new EntityManagementRuntimeException(
          "Unexpected exception occured during the normalization and cleaning of entity metadata ",
          e);
    }
  }

  private WebResource normalizeWebResource(WebResource existingFieldValue) {
    WebResource webResource = new WebResource(existingFieldValue);

    // generate missing thumbnail for Europeana resources
    if (StringUtils.isEmpty(webResource.getThumbnail())
        && StringUtils.isNotBlank(webResource.getSource())
        && webResource.getSource().startsWith(BASE_DATA_EUROPEANA_URI)) {
      webResource.setThumbnail(
          thumbnailBaseUrl + URLEncoder.encode(webResource.getId(), StandardCharsets.UTF_8));
    }

    return webResource;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map normalizeMapField(Field field, Map fieldValue)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {
    if (fieldValue == null) {
      return null;
    }
    if (isSingleValueStringMap(field)) {
      return normalizeSingleValueMap(field, (Map<String, String>) fieldValue);
    } else if (isMultipleValueStringMap(field)) {
      return normalizeMultipleValueMap(field.getName(), (Map<String, List<String>>) fieldValue);
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("normalization not supported for maps of type: {}", field.getGenericType());
      }
      return fieldValue;
    }
  }

  @SuppressWarnings({"unchecked"})
  Map<String, String> normalizeSingleValueMap(Field field, Map<String, String> singleValueMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {
    // remove spaces from the keys in the Map fields, normalize and remove trailing
    // spaces for the value, apply language normalization and filtering
    // value must be restricted to the 24 language codes that Europeana supports
    // Text Fields values must be capitalised
    Map<String, String> normalizedMap =
        ConstructorUtils.invokeConstructor(singleValueMap.getClass(), singleValueMap.size());
    for (Map.Entry<String, String> mapEntry : singleValueMap.entrySet()) {
      String normalizedKey = normalizeMapKey(mapEntry.getKey());
      if (normalizedKey == null) {
        // skip invalid language codes
        continue;
      }
      // remove trailing spaces in the value and capitalise text Fields
      normalizedMap.put(normalizedKey, capitaliseTextFields(field.getName(), mapEntry.getValue()));
    }
    return normalizedMap;
  }

  @SuppressWarnings({"unchecked"})
  private Map<String, List<String>> normalizeMultipleValueMap(
      String fieldName, Map<String, List<String>> singleValueMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {
    // remove spaces from the keys in the Map fields
    //	normalize and remove trailing spaces for the value and capitalise Text Fields
    //	 apply language normalization and filtering
    //	 value must be restricted to the 24 language codes that Europeana supports
    Map<String, List<String>> normalizedMap =
        ConstructorUtils.invokeConstructor(singleValueMap.getClass(), singleValueMap.size());
    for (Map.Entry<String, List<String>> mapEntry : singleValueMap.entrySet()) {

      String normalizedKey = normalizeMapKey(mapEntry.getKey());
      if (normalizedKey == null) {
        // skip invalid language codes
        continue;
      }

      List<String> normalizedValues = normalizeValues(fieldName, mapEntry.getValue());
      // remove trailing spaces in the value
      normalizedMap.put(normalizedKey, normalizedValues);
    }
    return normalizedMap;
  }

  private List<String> normalizeValues(String fieldName, List<String> values) {
    List<String> normalized;
    if (EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_DATE)) {
      normalized = new ArrayList<>();
      for (String value : values) {
        String normalizedValue = normalizeTextValue(fieldName, value);
        normalized.add(normalizedValue);
      }
    } else {
      normalized =
          values.stream().map(x -> normalizeTextValue(fieldName, x)).collect(Collectors.toList());
    }
    return normalized;
  }

  String normalizeMapKey(String key) {
    if (key == null) {
      return null;
    }
    String normalizedKey = key;
    if (key.contains(" ")) {
      normalizedKey = key.replaceAll("\\s+", "");
    }

    if (normalizedKey.length() < 2) {
      // invalid Key
      return null;
    } else if (normalizedKey.length() == 2) {
      if (!emLanguageCodes.isValidLanguageCode(normalizedKey)) {
        return null;
      }
    } else {
      // normalize by altLabel
      normalizedKey = emLanguageCodes.getByAlternativeCode(normalizedKey);
    }

    return normalizedKey;
  }

  private boolean isMultipleValueStringMap(Field field) {
    Type genericType = field.getGenericType();
    return genericType
        .getTypeName()
        .contains("Map<java.lang.String, java.util.List<java.lang.String>>");
  }

  private boolean isSingleValueStringMap(Field field) {
    Type genericType = field.getGenericType();
    return genericType.getTypeName().contains("Map<java.lang.String, java.lang.String>");
  }

  private void normalizeTextField(Field field, String fieldValue, Entity entity)
      throws IllegalArgumentException, IllegalAccessException {
    if (fieldValue != null) {
      String normalizedValue = normalizeTextValue(field.getName(), fieldValue);
      if (!normalizedValue.equals(fieldValue)) {
        entity.setFieldValue(field, normalizedValue);
      }
    }
  }

  String normalizeTextValue(String fieldName, String fieldValue) {
    if (fieldValue != null) {
      // remove trailing spaces and capitalise Text fields
      String normalizedValue = capitaliseTextFields(fieldName, fieldValue);

      if (EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_DATE)) {
        normalizedValue = convertDatetimeToDate(normalizedValue);
      }
      if (fieldValue != normalizedValue) {
        return normalizedValue;
      }
      // TODO: clean fields that are URI or URLS
    }
    return fieldValue;
  }

  String convertDatetimeToDate(String fieldValue) {
    try {
      if (fieldValue.contains("T")) {
        // converts from ISO Date time format to ISO Date format
        return LocalDate.parse(fieldValue, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            .toString();
      }
    } catch (DateTimeParseException e) {
      return fieldValue;
    }
    return fieldValue;
  }

  /**
   * Will trim the white spaces for the field value. Also will capitalise the Text Fields
   *
   * @param fieldName
   * @param fieldValue
   * @return
   */
  private String capitaliseTextFields(String fieldName, String fieldValue) {
    // do not capitalize language code strings (e.g. en, de, fr)
    if (!ISO_LANGUAGES.contains(fieldValue)) {
      if (EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_TEXT)) {
        return StringUtils.capitalize(fieldValue.trim());
      }
      if (EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_TEXT_OR_URI)
          && !(StringUtils.startsWithAny(fieldValue, "https://", "http://"))) {
        return StringUtils.capitalize(fieldValue.trim());
      }
      // for keyword field type leave it as it is
      if (EntityFieldsTypes.getFieldType(fieldName).equals(FIELD_TYPE_KEYWORD)) {
        return fieldValue.trim();
      }
    }
    return fieldValue.trim();
  }
}
