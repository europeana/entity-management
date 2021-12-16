package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.exceptions.EntityFieldAccessException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class EntityFieldsCompleteValidator
    implements ConstraintValidator<EntityFieldsCompleteValidatorInterface, Entity> {

  // used to separate the nested field within a parent field
  private static final String NESTED_FIELD_SEPARATOR = ".";

  @Resource(name = "emLanguageCodes")
  LanguageCodes emLanguageCodes;

  public void initialize(EntityFieldsCompleteValidatorInterface constraint) {
    //        System.out.println();
  }

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {

    if (entity == null) {
      return false;
    }

    boolean returnValue = true;

    List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());

    for (Field field : entityFields) {
      try {
        String fieldName = field.getName();
        if (!EntityFieldsTypes.hasTypeDefinition(fieldName)) {
          // there is no type definition to validate against
          continue;
        }

        Object fieldValue = entity.getFieldValue(field);
        if (fieldValue == null) {
          /*
           * TODO: enable the code below if there is a need to check the mandatory fields in this validator
           */
          //					addConstraint(context, "The mandatory field: "+field.getName()+" cannot be NULL.");
          //	                if(returnValue==true) {
          //	                	returnValue=false;
          //	                }
          continue;
        }
        boolean returnValueLocal = true;

        Class<?> fieldJavaType = field.getType();
        String fieldInternalType = EntityFieldsTypes.valueOf(fieldName).getFieldType();
        /*
         * the validation rules are implemented here
         */
        if (EntityFieldsTypes.isMultilingual(fieldName)) {
          returnValueLocal =
              validateMultilingualField(context, fieldName, fieldInternalType, fieldValue);
        } else if (FIELD_TYPE_URI.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateURIField(context, fieldName, fieldJavaType, fieldValue);
        } else if (FIELD_TYPE_DATE.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateDateField(context, fieldName, fieldJavaType, fieldValue);
        } else if (FIELD_TYPE_EMAIL.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateEmailField(context, fieldName, fieldJavaType, fieldValue);
        } else if (fieldJavaType.isAssignableFrom(String.class)
            && !fieldValue.toString().isBlank()) {
          // Text or Keyword, not multilingual
          returnValueLocal =
              validateStringValue(context, fieldName, fieldInternalType, (String) fieldValue, null);
        } else if (WebResource.class.isAssignableFrom(fieldJavaType)) {
          returnValueLocal = validateWebResourceField(context, fieldName, (WebResource) fieldValue);
        }

        // update global return value
        returnValue = returnValue && returnValueLocal;

      } catch (IllegalArgumentException e) {
        throw new EntityFieldAccessException(
            "During the validation of the entity fields an illegal or inappropriate argument exception has happened.",
            e);
      } catch (IllegalAccessException e) {
        throw new EntityFieldAccessException(
            "During the validation of the entity fields an illegal access to some method or field has happened.",
            e);
      }
    }
    return returnValue;
  }

  boolean validateStringValue(
      ConstraintValidatorContext context,
      String fieldName,
      String fieldInternalType,
      String fieldValue,
      String key) {
    if (!fieldValue.equals(fieldValue.trim())) {
      // must not contain trainling spaces
      addConstraint(
          context,
          "The entity field: "
              + fieldName
              + ", contains leading and/or trailing spaces: "
              + fieldValue
              + ".");
      return false;
    }
    boolean isUriRequired = isUriRequired(key, fieldInternalType);
    boolean isUriValue = UriValidator.isUri(fieldValue);

    // URI fields must have a valid URI value
    if (isUriRequired && !isUriValue) {
      addInvalidUriConstraint(context, fieldName, fieldInternalType, fieldValue);
      return false;

      // URI values in non-URI fields aren't allowed
    } else if (isUriValue && !isUriRequired) {
      addUriNotAllowedConstraint(context, fieldName, fieldValue, key);
      return false;
    }

    return true;
  }

  void addUriNotAllowedConstraint(
      ConstraintValidatorContext context, String fieldName, String fieldValue, String key) {
    String messageTemplate =
        "The entity field: " + fieldName + ", must not contain URI: " + fieldValue + ".";
    if (key != null) {
      messageTemplate += " for key: " + key;
    }
    addConstraint(context, messageTemplate);
  }

  @SuppressWarnings("unchecked")
  boolean validateEmailField(
      ConstraintValidatorContext context,
      String fieldName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateEmailListField(context, fieldName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      returnValue = checkEmailFormat(context, fieldName, (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateEmailListField(
      ConstraintValidatorContext context, String fieldName, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, fieldName, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      boolean returnValueLocal = checkEmailFormat(context, fieldName, fieldValueElem);
      // update global return value
      returnValue = returnValue && returnValueLocal;
    }
    return returnValue;
  }

  boolean hasFieldCardinalityViolation(
      ConstraintValidatorContext context, String fieldName, List<String> fieldValues) {
    if (EntityFieldsTypes.isSingleValueField(fieldName) && fieldValues.size() > 1) {
      addConstraint(context, "The entity field: " + fieldName + " cannot have more than one value");
      return true;
    }
    return false;
  }

  boolean checkEmailFormat(
      ConstraintValidatorContext context, String fieldName, String fieldValue) {
    EmailValidator validator = EmailValidator.getInstance();
    if (validator.isValid(fieldValue)) {
      return true;
    } else {
      addConstraint(
          context,
          "The entity field: "
              + fieldName
              + " is of type Email and contains inappropriate characters: "
              + fieldValue
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  boolean validateDateField(
      ConstraintValidatorContext context,
      String fieldName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateDateListField(context, fieldName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      returnValue = checkDateFormatISO8601(context, fieldName, (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateDateListField(
      ConstraintValidatorContext context, String fieldName, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }
    if (hasFieldCardinalityViolation(context, fieldName, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      if (checkDateFormatISO8601(context, fieldName, fieldValueElem) == false
          && returnValue == true) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  boolean checkDateFormatISO8601(
      ConstraintValidatorContext context, String fieldName, String fieldValue) {
    try {
      // LocalDate.parse(fieldValue.toString(),
      // DateTimeFormatter.ofPattern(java.time.format.DateTimeFormatter.ISO_DATE.toString()).withResolverStyle(ResolverStyle.STRICT));
      if (fieldValue.contains("T")) {
        // ISO Date time format
        LocalDate.parse(fieldValue, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
      } else {
        // ISO Date format
        LocalDate.parse(fieldValue, java.time.format.DateTimeFormatter.ISO_DATE);
      }
      return true;
    } catch (DateTimeParseException e) {
      addConstraint(
          context,
          "The entity field: "
              + fieldName
              + " is of type Date and does not comply with the ISO-8601 format: "
              + fieldValue
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  boolean validateURIField(
      ConstraintValidatorContext context,
      String fieldName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateURIListField(context, fieldName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      returnValue =
          validateUri(
              context,
              fieldName,
              EntityFieldsTypes.valueOf(fieldName).getFieldType(),
              (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateURIListField(
      ConstraintValidatorContext context, String fieldName, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, fieldName, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      if (!validateUri(
          context,
          fieldName,
          EntityFieldsTypes.valueOf(fieldName).getFieldType(),
          fieldValueElem)) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  private boolean validateUri(
      ConstraintValidatorContext context,
      String fieldName,
      String fieldInternalType,
      String fieldValue) {
    // validate URI Format
    if (!UriValidator.isUri(fieldValue)) {
      addInvalidUriConstraint(context, fieldName, fieldInternalType, fieldValue);
      return false;
    } else {
      return true;
    }
  }

  private void addInvalidUriConstraint(
      ConstraintValidatorContext context,
      String fieldName,
      String fieldInternalType,
      String fieldValue) {
    addConstraint(
        context,
        "The entity field: "
            + fieldName
            + " is of type: "
            + fieldInternalType
            + " but the value it contains: "
            + fieldValue
            + " does not have the proper URI form.");
  }

  @SuppressWarnings("unchecked")
  boolean validateMultilingualField(
      ConstraintValidatorContext context,
      String fieldName,
      String fieldInternalType,
      Object fieldValue) {
    // per default the multilingual field must be of type Map and these fields are
    // multilingual
    Map<String, Object> fieldValueMap = (Map<String, Object>) fieldValue;
    if (fieldValueMap.isEmpty()) {
      return true;
    }

    // validate language codes
    boolean returnValue = validateLanguageCodes(context, fieldName, fieldValueMap.keySet());

    boolean definitionIsList = EntityFieldsTypes.isList(fieldName);

    // validate values
    boolean localReturnValue;
    boolean valueIsList;
    for (Map.Entry<String, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
      valueIsList = fieldValueMapElem.getValue().getClass().isAssignableFrom(ArrayList.class);
      // only if the key is an empty string, the value is of type URI
      if (definitionIsList) {
        // string list
        if (!valueIsList) {
          // check cardinality for list
          addConstraint(
              context,
              "The entity field: "
                  + fieldName
                  + " cardinality: "
                  + EntityFieldsTypes.valueOf(fieldName).getFieldCardinality()
                  + " and must not be represented as list");
          localReturnValue = false;
        } else {
          List<String> values = (List<String>) fieldValueMapElem.getValue();
          for (String multilingualValue : values) {
            localReturnValue =
                validateMultilingualValue(
                    context,
                    fieldName,
                    fieldValueMapElem.getKey(),
                    multilingualValue,
                    fieldInternalType);
            returnValue = returnValue && localReturnValue;
          }
        }
      } else {
        // string value
        if (valueIsList) {
          // check cardinality for single valued
          addConstraint(
              context,
              "The entity field: "
                  + fieldName
                  + " cardinality: "
                  + EntityFieldsTypes.valueOf(fieldName).getFieldCardinality()
                  + " and must not be represented as list");
          localReturnValue = false;
        } else {
          String multilingualValue = (String) fieldValueMapElem.getValue();
          localReturnValue =
              validateMultilingualValue(
                  context,
                  fieldName,
                  fieldValueMapElem.getKey(),
                  multilingualValue,
                  fieldInternalType);
        }
        returnValue = returnValue && localReturnValue;
      }
    }
    return returnValue;
  }

  private boolean validateMultilingualValue(
      ConstraintValidatorContext context,
      String fieldName,
      String key,
      String multilingualValue,
      String fieldInternalType) {
    if (isUriRequired(key, fieldInternalType)) {
      return validateUri(
          context,
          fieldName,
          EntityFieldsTypes.valueOf(fieldName).getFieldType(),
          multilingualValue);
    } else {
      return validateStringValue(context, fieldName, fieldInternalType, multilingualValue, key);
    }
  }

  boolean validateLanguageCodes(
      ConstraintValidatorContext context, String fieldName, Set<String> keySet) {

    //		if (emLanguageCodes==null) return true;
    //		if (emLanguageCodes.getLanguages()==null) return true;
    boolean isValid = true;
    for (String key : keySet) {
      if (!emLanguageCodes.isValidLanguageCode(key)
          && !emLanguageCodes.isValidAltLanguageCode(key)) {
        addConstraint(
            context,
            "The entity field: "
                + fieldName
                + " contains the language code: "
                + key
                + " that does not belong to the Europena languge codes.");
      }
    }
    return isValid;
  }

  private boolean validateWebResourceField(
      ConstraintValidatorContext context, String fieldName, WebResource webResource) {
    boolean isValid = true;

    if (webResource.getId() != null
        && !validateUri(
            context,
            fieldName + NESTED_FIELD_SEPARATOR + EntityFieldsTypes.id.name(),
            EntityFieldsTypes.id.getFieldType(),
            webResource.getId())) {
      isValid = false;
    }
    if (webResource.getSource() != null
        && !validateUri(
            context,
            fieldName + NESTED_FIELD_SEPARATOR + EntityFieldsTypes.source.name(),
            EntityFieldsTypes.source.getFieldType(),
            webResource.getSource())) {
      isValid = false;
    }

    // thumbnail can be empty
    if (webResource.getThumbnail() != null
        && !validateUri(
            context,
            fieldName + NESTED_FIELD_SEPARATOR + EntityFieldsTypes.thumbnail.name(),
            EntityFieldsTypes.thumbnail.getFieldType(),
            webResource.getThumbnail())) {
      isValid = false;
    }
    return isValid;
  }

  private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
  }

  private boolean isUriRequired(String key, String fieldType) {
    return "".equals(key) && EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI.equals(fieldType);
  }
}
