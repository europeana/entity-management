package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.EmailValidator;

public class EntityFieldsDatatypeValidation {

  private final LanguageCodes emLanguageCodes;

  public EntityFieldsDatatypeValidation(LanguageCodes emLanguageCodes) {
    this.emLanguageCodes = emLanguageCodes;
  }

  public boolean validateStringValue(
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

  private void addUriNotAllowedConstraint(
      ConstraintValidatorContext context, String fieldName, String fieldValue, String key) {
    String messageTemplate =
        "The entity field: " + fieldName + ", must not contain URI: " + fieldValue + ".";
    if (key != null) {
      messageTemplate += " for key: " + key;
    }
    addConstraint(context, messageTemplate);
  }

  @SuppressWarnings("unchecked")
  public boolean validateEmailField(
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

  private boolean validateEmailListField(
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

  private boolean hasFieldCardinalityViolation(
      ConstraintValidatorContext context, String fieldName, List<String> fieldValues) {
    if (EntityFieldsTypes.isSingleValueField(fieldName) && fieldValues.size() > 1) {
      addConstraint(context, "The entity field: " + fieldName + " cannot have more than one value");
      return true;
    }
    return false;
  }

  private boolean checkEmailFormat(
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
  public boolean validateDateField(
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

  private boolean validateDateListField(
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

  private boolean checkDateFormatISO8601(
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
  public boolean validateURIField(
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
              context, fieldName, EntityFieldsTypes.getFieldType(fieldName), (String) fieldValue);
    }
    return returnValue;
  }

  private boolean validateURIListField(
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
          context, fieldName, EntityFieldsTypes.getFieldType(fieldName), fieldValueElem)) {
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
  public boolean validateMultilingualField(
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
                  + EntityFieldsTypes.getFieldCardinality(fieldName)
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
                  + EntityFieldsTypes.getFieldCardinality(fieldName)
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
          context, fieldName, EntityFieldsTypes.getFieldType(fieldName), multilingualValue);
    } else {
      return validateStringValue(context, fieldName, fieldInternalType, multilingualValue, key);
    }
  }

  private boolean validateLanguageCodes(
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
        if (isValid) isValid = false;
      }
    }
    return isValid;
  }

  private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
  }

  private boolean isUriRequired(String key, String fieldType) {
    return "".equals(key) && EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI.equals(fieldType);
  }

  public boolean valildateMandatoryField(
      ConstraintValidatorContext context, String fieldName, Object fieldValue) {
    if (fieldValue == null) {
      if (EntityFieldsTypes.isMandatory(fieldName)) {
        addConstraint(context, "The mandatory field: " + fieldName + " cannot be NULL.");
        return false;
      }
    }
    // validating the minimal content for the mandatory fields
    int minContentCount = EntityFieldsTypes.getMinContentCount(fieldName);
    if (minContentCount > 0) {
      if ((List.class.isAssignableFrom(fieldValue.getClass())
              && ((List<?>) fieldValue).size() < minContentCount)
          || (Map.class.isAssignableFrom(fieldValue.getClass())
              && ((Map<?, ?>) fieldValue).size() < minContentCount)) {
        addConstraint(context, fieldName + " must have at least " + minContentCount + " values");
        return false;
      }
    }

    return true;
  }

  public boolean validateDatatypeCompliance(
      ConstraintValidatorContext context, Field field, Object fieldValue) {

    if (fieldValue == null) {
      return true;
    }

    Class<?> fieldJavaType = field.getType();
    String fieldName = field.getName();
    String fieldInternalType = EntityFieldsTypes.valueOf(fieldName).getFieldType();

    /*
     * validating the fields' type compliance
     */
    if (EntityFieldsTypes.isMultilingual(fieldName)) {
      return validateMultilingualField(context, fieldName, fieldInternalType, fieldValue);
    } else if (FIELD_TYPE_URI.equals(EntityFieldsTypes.getFieldType(fieldName))) {
      return validateURIField(context, fieldName, fieldJavaType, fieldValue);
    } else if (FIELD_TYPE_DATE.equals(EntityFieldsTypes.getFieldType(fieldName))) {
      return validateDateField(context, fieldName, fieldJavaType, fieldValue);
    } else if (FIELD_TYPE_EMAIL.equals(EntityFieldsTypes.getFieldType(fieldName))) {
      return validateEmailField(context, fieldName, fieldJavaType, fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class) && !fieldValue.toString().isBlank()) {
      // Text or Keyword, not multilingual
      return validateStringValue(context, fieldName, fieldInternalType, (String) fieldValue, null);
    }

    return true;
  }
}
