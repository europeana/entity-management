package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;

@Component
public class EntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity> {

    @Resource(name="emLanguageCodes")
    LanguageCodes emLanguageCodes;
    
    public static final String altLabelFieldNamePrefix = "altLabel";

    public static final String altLabelCharacterSeparator = "_";

    public static final String languageSeparator = "_";

    
    private static final Logger logger = LogManager.getLogger(EntityFieldsValidator.class);

    public void initialize(ValidEntityFields constraint) {
    }

    @Override
	public boolean isValid(Entity entity, ConstraintValidatorContext context) {		
		
		if (entity==null) {
			return false;
		}
		
		boolean returnValue=true;
		
		List<Field> entityFields = new ArrayList<>();
		EntityUtils.getAllFields(entityFields, entity.getClass());
		
		try {			
			for (Field field : entityFields) {
				Object fieldValue = entity.getFieldValue(field);
				if (fieldValue==null) continue;
				Class<?> fieldType = field.getType();
				String fieldName = field.getName();
				/*
				 * the validation rules are implemented here
				 */
				//for the Text field type, check if it is a string and not a reference, and check the language codes
				if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Text")) {
					returnValue = validateTextField(context, field, fieldValue, returnValue);
				}
				//for the Keyword field type, check if it is a string and not a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Keyword")) {
					if (EntityUtils.isUri(fieldName.toString())) {
						addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Keyword and has the URI form (it should be a String not a URI).");
						returnValue = false;
					}
				}
				//for the URI field type, check if it is a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("URI")) {
					if (!EntityUtils.isUri(fieldName.toString())) {
						addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type URI and it does not have the URI form.");
						returnValue = false;
					}
				}
				//for the Date field type, check if it has the ISO-8601 format
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Date")) {
					returnValue = validateDateField(context, field, fieldValue, returnValue);
				}
				//for the Email field type, check if it has a valid format
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Email")) {
					returnValue = validateEmailField(context, field, fieldValue, returnValue);
				}
				//check the leading and trailing spaces from the String fields
				else if (fieldType.isAssignableFrom(String.class)) {					
					returnValue = validateStringField(context, field, fieldValue, returnValue);
				}				
	    }
	} catch (IllegalArgumentException e) {
	    logger.error(
		    "During the reconceliation of the entity data from different sources a method has been passed an illegal or inappropriate argument.",
		    e);
	} catch (IllegalAccessException e) {
	    logger.error(
		    "During the reconceliation of the entity data from different sources an illegal access to some method or field has happened.",
		    e);
	} 
	return returnValue;
    }

    boolean validateStringField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) throws IllegalAccessException {
	if (!fieldValue.toString().trim().equals(fieldValue.toString())) {
		addConstraint(returnValue, context, "The entity field: "+field.getName()+", contains leading and/or trailing spaces: "+fieldValue.toString());
		return false;
	}
	else {
		return true;
    }
    }
    
    boolean validateEmailField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	if (field.getType().isArray()) {
			Object[] fieldValueArray = (Object[])fieldValue;    			
			if (fieldValueArray.length==0) return returnValue;			
			for (Object fieldValueElem : fieldValueArray) {
				returnValue = checkEmailFormat(context, field, fieldValueElem, returnValue);
			}
		}
        else if (field.getType().isAssignableFrom(List.class)) {
			@SuppressWarnings("unchecked")
			List<Object> fieldValueList = (List<Object>)fieldValue;
			if (fieldValueList.size()==0) return returnValue;
			for (Object fieldValueElem : fieldValueList) {
				returnValue = checkEmailFormat(context, field, fieldValueElem, returnValue);
			}
		}
        else if (field.getType().isAssignableFrom(String.class)){
	        returnValue = checkEmailFormat(context, field, fieldValue, returnValue);
        }
        return returnValue;
    }
    
    boolean checkEmailFormat(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                            "[a-zA-Z0-9_+&*-]+)*@" +
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                            "A-Z]{2,7}$";                              
        Pattern pat = Pattern.compile(emailRegex);
        if (fieldValue == null) return false;
        if (!pat.matcher(fieldValue.toString()).matches()) {
        	return true;
        }
        else
        {
    		addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Email and contains inappropriate characters: "+fieldValue.toString());
    		return false;
        }
    }
    
    boolean validateDateField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
        if (field.getType().isArray()) {
			Object[] fieldValueArray = (Object[])fieldValue;    			
			if (fieldValueArray.length==0) return returnValue;			
			for (Object fieldValueElem : fieldValueArray) {
				returnValue = checkDateFormatISO8601(context, field, fieldValueElem, returnValue);
			}
		}
        else if (field.getType().isAssignableFrom(List.class)) {
			@SuppressWarnings("unchecked")
			List<Object> fieldValueList = (List<Object>)fieldValue;
			if (fieldValueList.size()==0) return returnValue;
			for (Object fieldValueElem : fieldValueList) {
				returnValue = checkDateFormatISO8601(context, field, fieldValueElem, returnValue);
			}
		}
        else if (field.getType().isAssignableFrom(String.class)){
	        returnValue = checkDateFormatISO8601(context, field, fieldValue, returnValue);
        }
        return returnValue;
    }
    
    boolean checkDateFormatISO8601(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	try {
        	LocalDate.parse(fieldValue.toString(), DateTimeFormatter.ofPattern(java.time.format.DateTimeFormatter.ISO_DATE.toString()).withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
    		addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Date and does not comply with the ISO-8601 format: "+fieldValue.toString());
            returnValue = false;
        }
    	return returnValue;
    }
   
    boolean validateTextField (ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//non-multilingual fields are not of type Map, but they can be of type String[], List<String>, or String
    	if (!EntityFieldsTypes.valueOf(field.getName()).getFieldIsmultilingual()) {
    		returnValue = validateNonMultilingualTextField(context, field, fieldValue, returnValue);    		
    	}
    	else {
    		returnValue = validateMultilingualTextField(context, field, fieldValue, returnValue); 
    		returnValue = validateLanguageCodes(context, field, fieldValue, returnValue);    		
    	}
    	return returnValue;
    }
    
    boolean validateNonMultilingualTextField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//if the field is an array
		if (field.getType().isArray()) {
			Object[] fieldValueArray = (Object[])fieldValue;    			
			if (fieldValueArray.length==0) return returnValue;   			
			for (Object fieldValueElem : fieldValueArray) {
				if (EntityUtils.isUri(fieldValueElem.toString())) {
					addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Text and has an element of the URI form: "+fieldValueElem.toString()+" (it should be a String not a URI).");
					returnValue = false;
				}
			}
		}
		//if the field is List
		else if (field.getType().isAssignableFrom(List.class)) {
			@SuppressWarnings("unchecked")
			List<Object> fieldValueList = (List<Object>)fieldValue;
			if (fieldValueList.size()==0) return returnValue;
			for (Object fieldValueElem : fieldValueList) {
				if (EntityUtils.isUri(fieldValueElem.toString())) {
					addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Text and has an element of the URI form: "+fieldValueElem.toString()+" (it should be a String not a URI).");
					returnValue = false;
				}
			}
		}
		//if the field is String
		else if (field.getType().isAssignableFrom(String.class)) {
			if (EntityUtils.isUri(fieldValue.toString())) {
				addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Text and has the URI form: "+fieldValue.toString()+" (it should be a String not a URI).");
				returnValue = false;
			}
		}
		
		return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	boolean validateMultilingualTextField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//per default the multilingual field must be of type Map
		Map<Object,Object> fieldValueMap = (Map<Object,Object>)fieldValue;
		if (fieldValueMap.size()==0) return returnValue;
		for (Map.Entry<Object, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
			if (fieldValueMapElem.getValue().getClass().isAssignableFrom(List.class)) {
				for (Object fieldValueMapElemValue : (List<Object>)(fieldValueMapElem.getValue())) {
					if (EntityUtils.isUri(fieldValueMapElemValue.toString())) {
						addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Text and has the URI form: "+fieldValueMapElemValue.toString()+", for the key: "+fieldValueMapElem.getKey().toString()+" (it should be a String not a URI).");
						returnValue = false;
					}
				}
			}
			else {
				if (EntityUtils.isUri(fieldValueMapElem.getValue().toString())) {
					addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Text and has the URI form: "+fieldValueMapElem.getValue().toString()+", for the key: "+fieldValueMapElem.getKey().toString()+" (it should be a String not a URI).");
					returnValue = false;
				}				
			}
		}		
		return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	boolean validateLanguageCodes (ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	boolean foundAlternativeCodes = false;
    	//per default the multilingual field must be of type Map
		Map<Object,Object> fieldValueMap = (Map<Object,Object>)fieldValue;
		for (Map.Entry<Object,Object> fieldValueMapElem : fieldValueMap.entrySet()) {
			boolean foundKnownCode = false;
			if (emLanguageCodes==null) break;
			if (emLanguageCodes.getLanguages()==null) break;
			for (Language language : emLanguageCodes.getLanguages()) {
				if (language.getCode().contains(fieldValueMapElem.getKey().toString())) {
					foundKnownCode = true;
					break;
				}
				if (language.getAlternativeLanguages()==null) continue;
				for (Language alternativeLanguage : language.getAlternativeLanguages()) {
					if (alternativeLanguage.getCode().contains(fieldValueMapElem.getKey().toString())) {
						if (foundAlternativeCodes == false) foundAlternativeCodes = true;
						foundKnownCode=true;
						break;
					}
				}
				if (foundKnownCode==true) break;
			}
			if (foundKnownCode==false) {
				addConstraint(returnValue, context, "The entity field: "+field.getName()+"contains the language code:"+fieldValueMapElem.getKey().toString()+"that does not belong to the Europena languge codes.");
				returnValue=false;
			}			
		}
		return returnValue;		
    }
    
    private void addConstraint(boolean returnValue, ConstraintValidatorContext context, String messageTemplate) {
	if (returnValue) {
	    // disable existing violation message
	    context.disableDefaultConstraintViolation();
	    // build new violation message and add it
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
	} else {
	    // build new violation message and add it
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
	}
    }

}
