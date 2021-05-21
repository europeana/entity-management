package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
		
		for (Field field : entityFields) {
			try {			
				Object fieldValue = entity.getFieldValue(field);
				if (fieldValue==null) continue;
				Class<?> fieldType = field.getType();
				String fieldName = field.getName();
				/*
				 * the validation rules are implemented here
				 */
				//for the Text field type, check if it is a string and not a reference, and check the language codes
				if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Text") || EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Keyword")) {
					returnValue = validateTextOrKeywordField(context, field, fieldValue, returnValue);
				}
				//for the URI field type, check if it is a reference
				else if (EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("URI") || EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Date") || EntityFieldsTypes.valueOf(fieldName).getFieldType().equals("Email")) {
					returnValue = validateEmailOrDateOrURIField(context, field, fieldValue, returnValue);
				}				
				//check the leading and trailing spaces from the String fields
				else if (fieldType.isAssignableFrom(String.class)) {					
					returnValue = validateStringField(context, field, fieldValue, returnValue);
				}				
			} catch (IllegalArgumentException e) {
			    logger.warn("During the validation of the entity fields an illegal or inappropriate argument exception has happened.",e);
			} catch (IllegalAccessException e) {
			    logger.warn("During the validation of the entity fields an illegal access to some method or field has happened.",e);
			} 
		}
		return returnValue;
    }


    boolean validateStringField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) throws IllegalAccessException {
	if (!fieldValue.toString().trim().equals(fieldValue.toString())) {
		addConstraint(returnValue, context, "The entity field: "+field.getName()+", contains leading and/or trailing spaces: "+fieldValue.toString()+".");
		return false;
	}
	else {
		return true;
    }
    }
    
    boolean validateEmailOrDateOrURIField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	if (field.getType().isArray()) {
			returnValue = validateEmailOrDateOrURIArrayField(context, field, fieldValue, returnValue);
		}
        else if (field.getType().isAssignableFrom(ArrayList.class)) {
			returnValue = validateEmailOrDateOrURIListField(context, field, fieldValue, returnValue);
		}
        else if (field.getType().isAssignableFrom(String.class)){
			returnValue = validateEmailOrDateOrURIStringField(context, field, fieldValue, returnValue);
        }
        return returnValue;
    }
    
    boolean validateEmailOrDateOrURIArrayField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
		Object[] fieldValueArray = (Object[])fieldValue;    			
		if (fieldValueArray.length==0) return returnValue;			
		for (Object fieldValueElem : fieldValueArray) {
			if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Email")) {
				returnValue = checkEmailFormat(context, field, fieldValueElem, returnValue);
			}
			else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Date")) {
				returnValue = checkDateFormatISO8601(context, field, fieldValueElem, returnValue);
			}
			else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("URI")) {
				returnValue = checkURIFormat(context, field, fieldValueElem, returnValue, true);
			}
		}
        return returnValue;
    }  
    
    boolean validateEmailOrDateOrURIListField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
 		@SuppressWarnings("unchecked")
		List<Object> fieldValueList = (List<Object>)fieldValue;
		if (fieldValueList.size()==0) return returnValue;
		for (Object fieldValueElem : fieldValueList) {
			if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Email")) {
				returnValue = checkEmailFormat(context, field, fieldValueElem, returnValue);
			}
			else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Date")) {
				returnValue = checkDateFormatISO8601(context, field, fieldValueElem, returnValue);
			}
			else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("URI")) {
				returnValue = checkURIFormat(context, field, fieldValueElem, returnValue, true);					
			}
			
		}		
        return returnValue;
    }
    
    boolean validateEmailOrDateOrURIStringField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Email")) {
    		returnValue = checkEmailFormat(context, field, fieldValue, returnValue);
    	}
    	else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("Date")) {
    		returnValue = checkDateFormatISO8601(context, field, fieldValue, returnValue);
    	}
    	else if (EntityFieldsTypes.valueOf(field.getName()).getFieldType().equals("URI")) {
			returnValue = checkURIFormat(context, field, fieldValue, returnValue, true);				
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
        if (pat.matcher(fieldValue.toString()).matches()) {
        	return true;
        }
        else
        {
    		addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Email and contains inappropriate characters: "+fieldValue.toString()+".");
    		return false;
        }
    }
    
    boolean checkDateFormatISO8601(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	try {
        	//LocalDate.parse(fieldValue.toString(), DateTimeFormatter.ofPattern(java.time.format.DateTimeFormatter.ISO_DATE.toString()).withResolverStyle(ResolverStyle.STRICT));
    		LocalDate.parse(fieldValue.toString(), java.time.format.DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
    		addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type Date and does not comply with the ISO-8601 format: "+fieldValue.toString()+".");
            returnValue = false;
        }
    	return returnValue;
    }
    
    boolean checkURIFormat(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue, boolean shouldBeURI) {
    	if (shouldBeURI && !EntityUtils.isUri(fieldValue.toString())) {
			addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type: "+EntityFieldsTypes.valueOf(field.getName()).getFieldType()+" and it's element: "+fieldValue.toString()+" does not have the URI form.");
			returnValue = false;
		}
    	else if (!shouldBeURI && EntityUtils.isUri(fieldValue.toString())) {
			addConstraint(returnValue, context, "The entity field: "+field.getName()+" is of type: "+EntityFieldsTypes.valueOf(field.getName()).getFieldType()+" and it's element: "+fieldValue.toString()+" has the URI form.");
			returnValue = false;
		}
    	return returnValue;
    }
   
    boolean validateTextOrKeywordField (ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//non-multilingual fields are not of type Map, but they can be of type String[], List<String>, or String
    	if (!EntityFieldsTypes.valueOf(field.getName()).getFieldIsmultilingual()) {
    		returnValue = validateNonMultilingualTextOrKeywordField(context, field, fieldValue, returnValue);    		
    	}
    	else {
    		returnValue = validateMultilingualTextOrKeywordField(context, field, fieldValue, returnValue); 
    		returnValue = validateLanguageCodes(context, field, fieldValue, returnValue);    		
    	}
    	return returnValue;
    }
    
    boolean validateNonMultilingualTextOrKeywordField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//if the field is an array
		if (field.getType().isArray()) {
			Object[] fieldValueArray = (Object[])fieldValue;    			
			if (fieldValueArray.length==0) return returnValue;   			
			for (Object fieldValueElem : fieldValueArray) {
				returnValue = checkURIFormat(context, field, fieldValueElem, returnValue, false);
			}
		}
		//if the field is List
		else if (field.getType().isAssignableFrom(ArrayList.class)) {
			@SuppressWarnings("unchecked")
			List<Object> fieldValueList = (List<Object>)fieldValue;
			if (fieldValueList.size()==0) return returnValue;
			for (Object fieldValueElem : fieldValueList) {
				returnValue = checkURIFormat(context, field, fieldValueElem, returnValue, false);
			}
		}
		//if the field is String
		else if (field.getType().isAssignableFrom(String.class)) {
			returnValue = checkURIFormat(context, field, fieldValue, returnValue, false);
		}
		
		return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	boolean validateMultilingualTextOrKeywordField(ConstraintValidatorContext context, Field field, Object fieldValue, boolean returnValue) {
    	//per default the multilingual field must be of type Map
		Map<Object,Object> fieldValueMap = (Map<Object,Object>)fieldValue;
		if (fieldValueMap.size()==0) return returnValue;
		for (Map.Entry<Object, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
			if (fieldValueMapElem.getValue().getClass().isAssignableFrom(ArrayList.class)) {
				for (Object fieldValueMapElemValue : (List<Object>)(fieldValueMapElem.getValue())) {
					returnValue = checkURIFormat(context, field, fieldValueMapElemValue, returnValue, false);
				}
			}
			else {
				returnValue = checkURIFormat(context, field, fieldValueMapElem.getValue(), returnValue, false);				
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
				addConstraint(returnValue, context, "The entity field: "+field.getName()+" contains the language code: "+fieldValueMapElem.getKey().toString()+" that does not belong to the Europena languge codes.");
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
