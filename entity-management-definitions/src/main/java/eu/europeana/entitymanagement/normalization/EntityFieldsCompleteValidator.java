package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.exceptions.EntityFieldAccessException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityUtils;
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
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EntityFieldsCompleteValidator
    implements ConstraintValidator<EntityFieldsCompleteValidatorInterface, Entity> {

  @Resource(name = "emLanguageCodes")
  LanguageCodes emLanguageCodes;

  private final IRIFactory iriFactory = IRIFactory.iriImplementation();

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

        Class<?> fieldType = field.getType();
        /*
         * the validation rules are implemented here
         */
        if (EntityFieldsTypes.isMultilingual(fieldName)) {
          returnValueLocal = validateMultilingualField(context, field, fieldValue);
        } else if (FIELD_TYPE_URI.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateURIField(context, field, fieldValue);
        } else if (FIELD_TYPE_DATE.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateDateField(context, field, fieldValue);
        } else if (FIELD_TYPE_EMAIL.equals(EntityFieldsTypes.getFieldType(fieldName))) {
          returnValueLocal = validateEmailField(context, field, fieldValue);
        } else if (fieldType.isAssignableFrom(String.class) && !fieldValue.toString().isBlank()) {
          // Text or Keyword, not multilingual
          returnValueLocal = validateStringValue(context, field, (String) fieldValue, null);
        } else if (WebResource.class.isAssignableFrom(fieldType)) {
          returnValueLocal = validateWebResourceField(context, field, (WebResource) fieldValue);
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
      ConstraintValidatorContext context, Field field, String fieldValue, String key) {
    if (!fieldValue.equals(fieldValue.trim())) {
      // must not contain trainling spaces
      addConstraint(
          context,
          "The entity field: "
              + field.getName()
              + ", contains leading and/or trailing spaces: "
              + fieldValue
              + ".");
      return false;
    } else {
      EntityFieldsTypes fieldType = EntityFieldsTypes.valueOf(field.getName());
      boolean isUrl = EntityUtils.isUrl(fieldValue);
      if (!"".equals(key) && isUrl) {
        // do not allow URIs for other key than empty keys
        addUriNotAllowedConstraint(context, field, fieldValue, key);
        return false;
      } else if ("".equals(key) && isUrl) {
        // key "", allow URIs only for field type TEXT OR URI
        if (EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI.equals(fieldType.getFieldType())) {
          return validateURIFormat(context, field, fieldValue);
        } else {
          addUriNotAllowedConstraint(context, field, fieldValue, key);
          return false;
        }
      }
    }
    return true;
  }

  void addUriNotAllowedConstraint(
      ConstraintValidatorContext context, Field field, String fieldValue, String key) {
    String messageTemplate =
        "The entity field: " + field.getName() + ", must not contain URI: " + fieldValue + ".";
    if (key != null) {
      messageTemplate += " for key: " + key;
    }
    addConstraint(context, messageTemplate);
  }

  @SuppressWarnings("unchecked")
  boolean validateEmailField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    boolean returnValue = true;
    if (field.getType().isAssignableFrom(ArrayList.class)) {
      returnValue = validateEmailListField(context, field, (List<String>) fieldValue);
    } else if (field.getType().isAssignableFrom(String.class)) {
      returnValue = checkEmailFormat(context, field, (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateEmailListField(
      ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, field, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      boolean returnValueLocal = checkEmailFormat(context, field, fieldValueElem);
      // update global return value
      returnValue = returnValue && returnValueLocal;
    }
    return returnValue;
  }

  boolean hasFieldCardinalityViolation(
      ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    if (EntityFieldsTypes.isSingleValueField(field.getName()) && fieldValues.size() > 1) {
      addConstraint(
          context, "The entity field: " + field.getName() + " cannot have more than one value");
      return true;
    }
    return false;
  }

  boolean checkEmailFormat(ConstraintValidatorContext context, Field field, String fieldValue) {
    EmailValidator validator = EmailValidator.getInstance();
    if (validator.isValid(fieldValue)) {
      return true;
    } else {
      addConstraint(
          context,
          "The entity field: "
              + field.getName()
              + " is of type Email and contains inappropriate characters: "
              + fieldValue
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  boolean validateDateField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    boolean returnValue = true;
    if (field.getType().isAssignableFrom(ArrayList.class)) {
      returnValue = validateDateListField(context, field, (List<String>) fieldValue);
    } else if (field.getType().isAssignableFrom(String.class)) {
      returnValue = checkDateFormatISO8601(context, field, (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateDateListField(
      ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }
    if (hasFieldCardinalityViolation(context, field, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      if (checkDateFormatISO8601(context, field, fieldValueElem) == false && returnValue == true) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  boolean checkDateFormatISO8601(
      ConstraintValidatorContext context, Field field, String fieldValue) {
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
              + field.getName()
              + " is of type Date and does not comply with the ISO-8601 format: "
              + fieldValue.toString()
              + ".");
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  boolean validateURIField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    boolean returnValue = true;
    if (field.getType().isAssignableFrom(ArrayList.class)) {
      returnValue = validateURIListField(context, field, (List<String>) fieldValue);
    } else if (field.getType().isAssignableFrom(String.class)) {
      returnValue = validateURIFormat(context, field, (String) fieldValue);
    }
    return returnValue;
  }

  boolean validateURIListField(
      ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    boolean returnValue = true;
    if (fieldValues.isEmpty()) {
      return true;
    }

    if (hasFieldCardinalityViolation(context, field, fieldValues)) {
      return false;
    }
    for (String fieldValueElem : fieldValues) {
      if (!validateURIFormat(context, field, fieldValueElem)) {
        returnValue = false;
      }
    }
    return returnValue;
  }

  boolean validateURIFormat(ConstraintValidatorContext context, Field field, String fieldValue) {
    // validate IRI Format
    IRI iri = iriFactory.create(fieldValue);
    if (iri.hasViolation(false)) {
      addConstraint(
          context,
          "The entity field: "
              + field.getName()
              + " is of type: "
              + EntityFieldsTypes.getFieldType(field.getName())
              + " but the value: "
              + fieldValue
              + " does not have the proper IRI form.");
      return false;
    } else {
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  boolean validateMultilingualField(
      ConstraintValidatorContext context, Field field, Object fieldValue) {
    // per default the multilingual field must be of type Map and these fields are
    // multilingual
    Map<String, Object> fieldValueMap = (Map<String, Object>) fieldValue;
    if (fieldValueMap.isEmpty()) {
      return true;
    }

    // validate language codes
    boolean returnValue = validateLanguageCodes(context, field, fieldValueMap.keySet());

    // field names are validted in main method
    String fieldType = EntityFieldsTypes.getFieldType(field.getName());
    boolean definitionIsList = EntityFieldsTypes.isList(field.getName());

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
                  + field.getName()
                  + " cardinality: "
                  + EntityFieldsTypes.valueOf(field.getName()).getFieldCardinality()
                  + " and must not be represented as list");
          localReturnValue = false;
        } else {
          List<String> values = (List<String>) fieldValueMapElem.getValue();
          for (String multilingualValue : values) {
            localReturnValue =
                validateMultilingualValue(
                    context, field, fieldValueMapElem.getKey(), multilingualValue, fieldType);
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
                  + field.getName()
                  + " cardinality: "
                  + EntityFieldsTypes.valueOf(field.getName()).getFieldCardinality()
                  + " and must not be represented as list");
          localReturnValue = false;
        } else {
          String multilingualValue = (String) fieldValueMapElem.getValue();
          localReturnValue =
              validateMultilingualValue(
                  context, field, fieldValueMapElem.getKey(), multilingualValue, fieldType);
        }
        returnValue = returnValue && localReturnValue;
      }
    }
    return returnValue;
  }

  private boolean validateMultilingualValue(
      ConstraintValidatorContext context,
      Field field,
      String key,
      String multilingualValue,
      String fieldType) {
    if ("".equals(key) && FIELD_TYPE_TEXT_OR_URI.equals(fieldType)) {
      return validateURIFormat(context, field, multilingualValue);
    } else {
      return validateStringValue(context, field, multilingualValue, key);
    }
  }

  boolean validateLanguageCodes(
      ConstraintValidatorContext context, Field field, Set<String> keySet) {

    //		if (emLanguageCodes==null) return true;
    //		if (emLanguageCodes.getLanguages()==null) return true;
    boolean isValid = true;
    for (String key : keySet) {
      if (!emLanguageCodes.isValidLanguageCode(key)
          && !emLanguageCodes.isValidAltLanguageCode(key)) {
        addConstraint(
            context,
            "The entity field: "
                + field.getName()
                + " contains the language code: "
                + key
                + " that does not belong to the Europena languge codes.");
      }
    }
    return isValid;
  }

  private boolean validateWebResourceField(
      ConstraintValidatorContext context, Field field, WebResource webResource) {
    boolean isValid = true;

    if (webResource.getId() == null || !validateURIFormat(context, field, webResource.getId())) {
      addConstraint(context, "Field " + field.getName() + " has an invalid id value");
      isValid = false;
    }
    if (webResource.getSource() == null
        || !validateURIFormat(context, field, webResource.getSource())) {
      addConstraint(context, "Field " + field.getName() + " has an invalid source value");
      isValid = false;
    }

    // thumbnail can be empty
    if (StringUtils.hasLength(webResource.getThumbnail())
        && !validateURIFormat(context, field, webResource.getThumbnail())) {
      addConstraint(context, "Field " + field.getName() + " has an invalid thumbnail value");
      isValid = false;
    }
    return isValid;
  }

  private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
  }
}
