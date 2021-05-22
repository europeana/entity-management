package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.routines.EmailValidator;
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
		
		List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());
		
		for (Field field : entityFields) {
			try {			
				Object fieldValue = entity.getFieldValue(field);
				if (fieldValue==null) continue;
				Class<?> fieldType = field.getType();
				String fieldName = field.getName();
				boolean returnValueLocal = true;
				/*
				 * the validation rules are implemented here
				 */
				//for the Text field type, check if it is a string and not a reference, and check the language codes
				if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals(EntityFieldsTypes.FIELD_TYPE_TEXT) || EntityFieldsTypes.valueOf(fieldName).getFieldType().equals(EntityFieldsTypes.FIELD_TYPE_KEYWORD)) {
					returnValueLocal = validateTextOrKeywordField(context, field, fieldValue);
				}
				//for the URI field type, check if it is a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals(EntityFieldsTypes.FIELD_TYPE_URI)) {
					returnValueLocal = validateURIField(context, field, fieldValue);
				}	
				//for the URI field type, check if it is a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals(EntityFieldsTypes.FIELD_TYPE_DATE)) {
					returnValueLocal = validateDateField(context, field, fieldValue);
				}
				//for the URI field type, check if it is a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals(EntityFieldsTypes.FIELD_TYPE_EMAIL)) {
					returnValueLocal = validateEmailField(context, field, fieldValue);
				}
				//check the leading and trailing spaces from the String fields
				else if (fieldType.isAssignableFrom(String.class)) {					
					returnValueLocal = validateStringField(context, field, fieldValue);
				}	
				
				if(returnValueLocal==false && returnValue==true) {
					returnValue = false;
				}
			} catch (IllegalArgumentException e) {
			    logger.warn("During the validation of the entity fields an illegal or inappropriate argument exception has happened.",e);
			} catch (IllegalAccessException e) {
			    logger.warn("During the validation of the entity fields an illegal access to some method or field has happened.",e);
			} 
		}
		return returnValue;
    }


    boolean validateStringField(ConstraintValidatorContext context, Field field, Object fieldValue) throws IllegalAccessException {
	if (!fieldValue.toString().trim().equals(fieldValue.toString())) {
		addConstraint(context, "The entity field: "+field.getName()+", contains leading and/or trailing spaces: "+fieldValue.toString()+".");
		return false;
	}
	else {
		return true;
    }
    }
    
    boolean validateEmailField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	if (field.getType().isAssignableFrom(ArrayList.class)) {
			returnValue = validateEmailListField(context, field, fieldValue);
		}
        else if (field.getType().isAssignableFrom(String.class)){
			returnValue = validateEmailStringField(context, field, fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateDateField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	if (field.getType().isAssignableFrom(ArrayList.class)) {
			returnValue = validateDateListField(context, field, fieldValue);
		}
        else if (field.getType().isAssignableFrom(String.class)){
			returnValue = validateDateStringField(context, field, fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateURIField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	if (field.getType().isAssignableFrom(ArrayList.class)) {
			returnValue = validateURIListField(context, field, fieldValue);
		}
        else if (field.getType().isAssignableFrom(String.class)){
			returnValue = validateURIStringField(context, field, fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateEmailListField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	@SuppressWarnings("unchecked")
		List<Object> fieldValueList = (List<Object>)fieldValue;
		if (fieldValueList.size()==0) return true;
		for (Object fieldValueElem : fieldValueList) {
			if (checkEmailFormat(context, field, fieldValueElem) == false && returnValue == true) {
				returnValue = false;
			}
		}		
        return returnValue;
    }

    boolean validateDateListField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	@SuppressWarnings("unchecked")
		List<Object> fieldValueList = (List<Object>)fieldValue;
		if (fieldValueList.size()==0) return true;
		for (Object fieldValueElem : fieldValueList) {
			if (checkDateFormatISO8601(context, field, fieldValueElem) == false && returnValue == true) {
				returnValue = false;
			}
		}		
        return returnValue;
    }

    boolean validateURIListField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
 		@SuppressWarnings("unchecked")
		List<Object> fieldValueList = (List<Object>)fieldValue;
		if (fieldValueList.size()==0) return true;
		for (Object fieldValueElem : fieldValueList) {
			if (checkURIFormat(context, field, fieldValueElem, true) == false && returnValue == true) {
				returnValue = false;
			}
		}		
        return returnValue;
    }

    boolean validateEmailStringField(ConstraintValidatorContext context, Field field, Object fieldValue) {
		return checkEmailFormat(context, field, fieldValue);
    }
    
    boolean validateDateStringField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	return checkDateFormatISO8601(context, field, fieldValue); 
    }
    
    boolean validateURIStringField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	return checkURIFormat(context, field, fieldValue, true);				
    }
    
    boolean checkEmailFormat(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	EmailValidator validator = EmailValidator.getInstance();
    	if (validator.isValid(fieldValue.toString())) {
    		return true;
    	} else {
    		addConstraint(context, "The entity field: "+field.getName()+" is of type Email and contains inappropriate characters: "+fieldValue.toString()+".");
    		return false;
    	}
    }
    
    boolean checkDateFormatISO8601(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	try {
        	//LocalDate.parse(fieldValue.toString(), DateTimeFormatter.ofPattern(java.time.format.DateTimeFormatter.ISO_DATE.toString()).withResolverStyle(ResolverStyle.STRICT));
    		LocalDate.parse(fieldValue.toString(), java.time.format.DateTimeFormatter.ISO_DATE);
    		return true;
        } catch (DateTimeParseException e) {
    		addConstraint(context, "The entity field: "+field.getName()+" is of type Date and does not comply with the ISO-8601 format: "+fieldValue.toString()+".");
            return false;
        }
    	
    }
    
    boolean checkURIFormat(ConstraintValidatorContext context, Field field, Object fieldValue, boolean shouldBeURI) {
    	if (shouldBeURI && !EntityUtils.isUri(fieldValue.toString())) {
			addConstraint(context, "The entity field: "+field.getName()+" is of type: "+EntityFieldsTypes.getFieldType(field.getName())+" and it's element: "+fieldValue.toString()+" does not have the URI form.");
			return false;
		}
    	else if (!shouldBeURI && EntityUtils.isUri(fieldValue.toString())) {
			addConstraint(context, "The entity field: "+field.getName()+" is of type: "+EntityFieldsTypes.getFieldType(field.getName())+" and it's element: "+fieldValue.toString()+" has the URI form.");
			return false;
		}
    	return true;
    }
   
    boolean validateTextOrKeywordField (ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	//non-multilingual fields are not of type Map, but they can be of type String[], List<String>, or String
    	if (!EntityFieldsTypes.valueOf(field.getName()).getFieldIsmultilingual()) {
    		boolean returnValueLocal = validateNonMultilingualTextOrKeywordField(context, field, fieldValue);
    		if (returnValueLocal == false) {
    			returnValue = false;
    		}
    	}
    	else {
    		boolean returnValueLocal = validateMultilingualTextOrKeywordField(context, field, fieldValue);
    		if (returnValueLocal == false) {
    			returnValue = false;
    		}
    		returnValueLocal = validateLanguageCodes(context, field, fieldValue);  
    		if (returnValueLocal == false && returnValue == true) {
    			returnValue = false;
    		}
    	}
    	return returnValue;
    }
    
    boolean validateNonMultilingualTextOrKeywordField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	//if the field is List
		if (field.getType().isAssignableFrom(ArrayList.class)) {
			@SuppressWarnings("unchecked")
			List<Object> fieldValueList = (List<Object>)fieldValue;
			if (fieldValueList.size()==0) return true;
			for (Object fieldValueElem : fieldValueList) {
				boolean returnValueLocal = checkURIFormat(context, field, fieldValueElem, false);
				if(returnValueLocal==false && returnValue==true) {
					returnValue = false;
				}
			}
		}
		//if the field is String
		else if (field.getType().isAssignableFrom(String.class)) {
			boolean returnValueLocal = checkURIFormat(context, field, fieldValue, false);
			if(returnValueLocal==false) {
				returnValue = false;
			}
		}
		
		return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	boolean validateMultilingualTextOrKeywordField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	//per default the multilingual field must be of type Map
		Map<Object,Object> fieldValueMap = (Map<Object,Object>)fieldValue;
		if (fieldValueMap.size()==0) return true;
		for (Map.Entry<Object, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
			if (fieldValueMapElem.getValue().getClass().isAssignableFrom(ArrayList.class)) {
				for (Object fieldValueMapElemValue : (List<Object>)(fieldValueMapElem.getValue())) {
					boolean returnValueLocal = checkURIFormat(context, field, fieldValueMapElemValue, false);
					if(returnValueLocal==false && returnValue==true) {
						returnValue = false;
					}
				}
			}
			else {
				boolean returnValueLocal = checkURIFormat(context, field, fieldValueMapElem.getValue(), false);
				if(returnValueLocal==false && returnValue==true) {
					returnValue = false;
				}
			}
		}		
		return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	boolean validateLanguageCodes (ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
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
				addConstraint(context, "The entity field: "+field.getName()+" contains the language code: "+fieldValueMapElem.getKey().toString()+" that does not belong to the Europena languge codes.");
				if (returnValue==true) {
					returnValue=false;
				}
			}			
		}
		return returnValue;		
    }
    
    private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
    }

}
