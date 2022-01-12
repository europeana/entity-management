package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.StringUtils;

public class EntityFieldsDatatypeValidation {

  private final LanguageCodes emLanguageCodes;

  public EntityFieldsDatatypeValidation(LanguageCodes emLanguageCodes) {
    this.emLanguageCodes = emLanguageCodes;
  }

  public boolean validateStringValue(
      ConstraintValidatorContext context,
      String fieldFullName,
      String fieldInternalType,
      String fieldValue,
      String key) {
    if (!fieldValue.equals(fieldValue.trim())) {
      // must not contain trainling spaces
      addConstraint(
          context,
          "The entity field: "
              + fieldFullName
              + ", contains leading and/or trailing spaces: "
              + fieldValue
              + ".");
      return false;
    }
    boolean isUriRequired = isUriRequired(key, fieldInternalType);
    boolean isUriValue = UriValidator.isUri(fieldValue);

    // URI fields must have a valid URI value
    if (isUriRequired && !isUriValue) {
      addInvalidUriConstraint(context, fieldFullName, fieldInternalType, fieldValue);
      return false;

      // URI values in non-URI fields aren't allowed
    } else if (isUriValue && !isUriRequired) {
      addUriNotAllowedConstraint(context, fieldFullName, fieldValue, key);
      return false;
    }

    return true;
  }

  private void addUriNotAllowedConstraint(
      ConstraintValidatorContext context, String fieldFullName, String fieldValue, String key) {
    String messageTemplate =
        "The entity field '" + fieldFullName + "' must not contain URI: " + fieldValue + ".";
    if (key != null) {
      messageTemplate += " for key: " + key;
    }
    addConstraint(context, messageTemplate);
  }

  @SuppressWarnings("unchecked")
  public boolean validateEmailField(
      ConstraintValidatorContext context,
      String fieldFullName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateEmailListField(context, fieldFullName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      returnValue = checkEmailFormat(context, fieldFullName, (String) fieldValue);
    }
    return returnValue;
  }

  private boolean validateEmailListField(
      ConstraintValidatorContext context, String fieldFullName, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, fieldFullName, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      boolean returnValueLocal = checkEmailFormat(context, fieldFullName, fieldValueElem);
      // update global return value
      returnValue = returnValue && returnValueLocal;
    }
    return returnValue;
  }

  private boolean validateWebResourceField(
      ConstraintValidatorContext context, String fieldFullName, WebResource webResource) {
    boolean isValid = true;

    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);

    if (webResource.getId() == null
        || !validateUri(
            context,
            fieldFullName,
            EntityFieldsTypes.getFieldType(fieldSimpleName),
            webResource.getId())) {
      addConstraint(context, "Field '" + fieldFullName + "' has an invalid or empty id value.");
      isValid = false;
    }
    if (webResource.getSource() == null
        || !validateUri(
            context,
            fieldFullName,
            EntityFieldsTypes.getFieldType(fieldSimpleName),
            webResource.getSource())) {
      addConstraint(context, "Field '" + fieldFullName + "' has an invalid or empty source value.");
      isValid = false;
    }

    // thumbnail can be empty
    if (StringUtils.hasLength(webResource.getThumbnail())
        && !validateUri(
            context,
            fieldFullName,
            EntityFieldsTypes.getFieldType(fieldSimpleName),
            webResource.getThumbnail())) {
      addConstraint(
          context, "Field '" + fieldFullName + "' has an invalid or empty thumbnail value.");
      isValid = false;
    }
    return isValid;
  }

  private boolean hasFieldCardinalityViolation(
      ConstraintValidatorContext context, String fieldFullName, List<String> fieldValues) {
    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);
    if (EntityFieldsTypes.isSingleValueField(fieldSimpleName) && fieldValues.size() > 1) {
      addConstraint(
          context, "The entity field '" + fieldFullName + "' cannot have more than one value");
      return true;
    }
    return false;
  }

  private boolean checkEmailFormat(
      ConstraintValidatorContext context, String fieldFullName, String fieldValue) {
    EmailValidator validator = EmailValidator.getInstance();
    if (validator.isValid(fieldValue)) {
      return true;
    } else {
      addConstraint(
          context,
          "The entity field '"
              + fieldFullName
              + "' is of type Email and contains inappropriate characters: "
              + fieldValue
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public boolean validateDateField(
      ConstraintValidatorContext context,
      String fieldFullName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateDateListField(context, fieldFullName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      returnValue = checkDateFormatISO8601(context, fieldFullName, (String) fieldValue);
    }
    return returnValue;
  }

  private boolean validateDateListField(
      ConstraintValidatorContext context, String fieldFullName, List<String> fieldValues) {
    boolean isValid = true;
    if (fieldValues.isEmpty()) {
      return true;
    }
    if (hasFieldCardinalityViolation(context, fieldFullName, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      isValid = isValid && checkDateFormatISO8601(context, fieldFullName, fieldValueElem);
    }
    return isValid;
  }

  private boolean checkDateFormatISO8601(
      ConstraintValidatorContext context, String fieldFullName, String fieldValue) {
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
          "The entity field '"
              + fieldFullName
              + "' is of type Date and does not comply with the ISO-8601 format: "
              + fieldValue
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public boolean validateURIField(
      ConstraintValidatorContext context,
      String fieldFullName,
      Class<?> fieldJavaType,
      Object fieldValue) {
    boolean returnValue = true;
    if (fieldJavaType.isAssignableFrom(ArrayList.class)) {
      returnValue = validateURIListField(context, fieldFullName, (List<String>) fieldValue);
    } else if (fieldJavaType.isAssignableFrom(String.class)) {
      String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);
      returnValue =
          validateUri(
              context,
              fieldFullName,
              EntityFieldsTypes.getFieldType(fieldSimpleName),
              (String) fieldValue);
    }
    return returnValue;
  }

  private boolean validateURIListField(
      ConstraintValidatorContext context, String fieldFullName, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, fieldFullName, fieldValues)) {
      return false;
    }

    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);
    for (String fieldValueElem : fieldValues) {
      if (!validateUri(
          context,
          fieldFullName,
          EntityFieldsTypes.getFieldType(fieldSimpleName),
          fieldValueElem)) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  private boolean validateUri(
      ConstraintValidatorContext context,
      String fieldFullName,
      String fieldInternalType,
      String fieldValue) {
    // validate URI Format
    if (!UriValidator.isUri(fieldValue)) {
      addInvalidUriConstraint(context, fieldFullName, fieldInternalType, fieldValue);
      return false;
    } else {
      return true;
    }
  }

  private void addInvalidUriConstraint(
      ConstraintValidatorContext context,
      String fieldFullName,
      String fieldInternalType,
      String fieldValue) {
    addConstraint(
        context,
        "The entity field '"
            + fieldFullName
            + "' is of type: "
            + fieldInternalType
            + " but the value it contains: "
            + fieldValue
            + " does not have the proper URI form.");
  }

  @SuppressWarnings("unchecked")
  public boolean validateMultilingualField(
      ConstraintValidatorContext context,
      String fieldFullName,
      String fieldInternalType,
      Object fieldValue) {
    // per default the multilingual field must be of type Map and these fields are
    // multilingual
    Map<String, Object> fieldValueMap = (Map<String, Object>) fieldValue;
    if (fieldValueMap.isEmpty()) {
      return true;
    }

    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);

    // validate language codes
    boolean returnValue = validateLanguageCodes(context, fieldFullName, fieldValueMap.keySet());

    boolean definitionIsList = EntityFieldsTypes.isListOrMap(fieldSimpleName);

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
              "The entity field '"
                  + fieldFullName
                  + "' cardinality: "
                  + EntityFieldsTypes.getFieldCardinality(fieldSimpleName)
                  + " and must be represented as list");
          localReturnValue = false;
        } else {
          List<String> values = (List<String>) fieldValueMapElem.getValue();
          for (String multilingualValue : values) {
            localReturnValue =
                validateMultilingualValue(
                    context,
                    fieldFullName,
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
              "The entity field '"
                  + fieldFullName
                  + "' cardinality: "
                  + EntityFieldsTypes.getFieldCardinality(fieldSimpleName)
                  + " and must not be represented as list");
          localReturnValue = false;
        } else {
          String multilingualValue = (String) fieldValueMapElem.getValue();
          localReturnValue =
              validateMultilingualValue(
                  context,
                  fieldFullName,
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
      String fieldFullName,
      String key,
      String multilingualValue,
      String fieldInternalType) {
    if (isUriRequired(key, fieldInternalType)) {
      String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);
      return validateUri(
          context,
          fieldFullName,
          EntityFieldsTypes.getFieldType(fieldSimpleName),
          multilingualValue);
    } else {
      return validateStringValue(context, fieldFullName, fieldInternalType, multilingualValue, key);
    }
  }

  private boolean validateLanguageCodes(
      ConstraintValidatorContext context, String fieldFullName, Set<String> keySet) {

    // if (emLanguageCodes==null) return true;
    // if (emLanguageCodes.getLanguages()==null) return true;
    boolean isValid = true;
    for (String key : keySet) {
      if (!emLanguageCodes.isValidLanguageCode(key)
          && !emLanguageCodes.isValidAltLanguageCode(key)) {
        addConstraint(
            context,
            "The entity field '"
                + fieldFullName
                + "' contains the language code: "
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

  public boolean validateMandatoryFields(
      ConstraintValidatorContext context, String fieldFullName, Object fieldValue) {
    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);
    if (EntityFieldsTypes.isMandatory(fieldSimpleName) && fieldValue == null) {
      addConstraint(context, "The mandatory field: " + fieldFullName + " cannot be NULL.");
      return false;
    }

    int minContentCount = EntityFieldsTypes.getMinContentCount(fieldSimpleName);
    if (minContentCount > 0) {
      if ((List.class.isAssignableFrom(fieldValue.getClass())
              && ((List<?>) fieldValue).size() < minContentCount)
          || (Map.class.isAssignableFrom(fieldValue.getClass())
              && ((Map<?, ?>) fieldValue).size() < minContentCount)) {
        addConstraint(
            context, fieldFullName + " must have at least " + minContentCount + " value(s)");
        return false;
      }
    }

    // no violations
    return true;
  }

  public boolean validateMetadataFields(
      ConstraintValidatorContext context,
      String fieldFullName,
      Object fieldValue,
      Class<?> fieldType) {

    if (fieldValue == null) {
      return true;
    }

    String fieldSimpleName = fieldFullName.substring(fieldFullName.lastIndexOf(".") + 1);

    String fieldInternalType = EntityFieldsTypes.valueOf(fieldSimpleName).getFieldType();

    /*
     * validating the fields' type compliance
     */
    if (EntityFieldsTypes.isMultilingual(fieldSimpleName)) {
      return validateMultilingualField(context, fieldFullName, fieldInternalType, fieldValue);
    } else if (FIELD_TYPE_URI.equals(EntityFieldsTypes.getFieldType(fieldSimpleName))) {
      return validateURIField(context, fieldFullName, fieldType, fieldValue);
    } else if (FIELD_TYPE_DATE.equals(EntityFieldsTypes.getFieldType(fieldSimpleName))) {
      return validateDateField(context, fieldFullName, fieldType, fieldValue);
    } else if (FIELD_TYPE_EMAIL.equals(EntityFieldsTypes.getFieldType(fieldSimpleName))) {
      return validateEmailField(context, fieldFullName, fieldType, fieldValue);
    } else if (fieldType.isAssignableFrom(String.class) && !fieldValue.toString().isBlank()) {
      // Text or Keyword, not multilingual
      return validateStringValue(
          context, fieldFullName, fieldInternalType, (String) fieldValue, null);
    }

    return true;
  }

  public boolean validateMetadataObjects(
      ConstraintValidatorContext context,
      String fieldFullName,
      Object fieldValue,
      Class<?> fieldType) {
    // mandatory fields handled before this method is called
    if (fieldValue != null) {
      if (WebResource.class.isAssignableFrom(fieldType)) {
        return validateWebResourceField(context, fieldFullName, (WebResource) fieldValue);
      }
    }

    return true;
  }
}
