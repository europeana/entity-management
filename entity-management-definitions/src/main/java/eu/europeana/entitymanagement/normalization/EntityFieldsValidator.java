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
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language;
import eu.europeana.entitymanagement.definitions.exceptions.EntityValidationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.*;

@Component
public class EntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity> {

    @Resource(name="emLanguageCodes")
    LanguageCodes emLanguageCodes;
    
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
			        String fieldName = field.getName();
			        if(!EntityFieldsTypes.hasTypeDefinition(fieldName)) {
			            //there is no type definition to validate against
			            continue;
			        }
			        
			        Object fieldValue = entity.getFieldValue(field);
				if (fieldValue==null) { 
				    //skip validation of empty fields
				    continue;
				}
				boolean returnValueLocal = true;
	                            
				Class<?> fieldType = field.getType();
				/*
				 * the validation rules are implemented here
				 */
				if (EntityFieldsTypes.isMultilingual(fieldName)) {
					returnValueLocal = validateMultilingualFieldAndCheckPossibleUri(context, field, fieldValue);
				}
				else if (FIELD_TYPE_URI.equals(EntityFieldsTypes.getFieldType(fieldName))) {
					returnValueLocal = validateURIField(context, field, fieldValue);
				}					
				else if (FIELD_TYPE_DATE.equals(EntityFieldsTypes.getFieldType(fieldName))) {
					returnValueLocal = validateDateField(context, field, fieldValue);
				}
				else if (FIELD_TYPE_EMAIL.equals(EntityFieldsTypes.getFieldType(fieldName))) {
					returnValueLocal = validateEmailField(context, field, fieldValue);
				}
				else if (fieldType.isAssignableFrom(String.class)) {					
					returnValueLocal = validateStringField(context, field, (String)fieldValue);
				}	
				
				//update global return value
				returnValue = returnValue && returnValueLocal;
				
			} catch (IllegalArgumentException e) {
			    throw new EntityValidationException("During the validation of the entity fields an illegal or inappropriate argument exception has happened.", e);	    
			} catch (IllegalAccessException e) {
			    throw new EntityValidationException("During the validation of the entity fields an illegal access to some method or field has happened.", e);
			} 
		}
		return returnValue;
    }

    


    boolean validateStringField(ConstraintValidatorContext context, Field field, String fieldValue) throws IllegalAccessException {
	if (!fieldValue.equals(fieldValue.trim())) {
		addConstraint(context, "The entity field: "+field.getName()+", contains leading and/or trailing spaces: "+fieldValue.toString()+".");
		return false;
	}
	else {
		return true;
    }
    }
    
    @SuppressWarnings("unchecked")
    boolean validateEmailField(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	if (field.getType().isAssignableFrom(ArrayList.class)) {
			returnValue = validateEmailListField(context, field, (List<String>)fieldValue);
		}
        else if (field.getType().isAssignableFrom(String.class)){
			returnValue = checkEmailFormat(context, field, (String)fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateEmailListField(ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    	boolean returnValue = true;
    		if (fieldValues.isEmpty()) { 
		    return true;
		}
		for (String fieldValueElem : fieldValues) {
		    boolean returnValueLocal = checkEmailFormat(context, field, fieldValueElem);
		    //update global return value
                    returnValue = returnValue && returnValueLocal;
		}		
        return returnValue;
    }

    boolean checkEmailFormat(ConstraintValidatorContext context, Field field, String fieldValue) {
        EmailValidator validator = EmailValidator.getInstance();
        if (validator.isValid(fieldValue)) {
                return true;
        } else {
                addConstraint(context, "The entity field: "+field.getName()+" is of type Email and contains inappropriate characters: "+fieldValue+".");
                return false;
        }
    }
    
    
    @SuppressWarnings("unchecked")
    boolean validateDateField(ConstraintValidatorContext context, Field field, Object fieldValue) {
        boolean returnValue = true;
        if (field.getType().isAssignableFrom(ArrayList.class)) {
                        returnValue = validateDateListField(context, field, (List<String>) fieldValue);
                }
        else if (field.getType().isAssignableFrom(String.class)){
                        returnValue = checkDateFormatISO8601(context, field, (String)fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateDateListField(ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    	boolean returnValue = true;
    		if (fieldValues.isEmpty()) {
		    return true;
		}
		for (String fieldValueElem : fieldValues) {
			if (checkDateFormatISO8601(context, field, fieldValueElem) == false && returnValue == true) {
				returnValue = false;
			}
		}		
        return returnValue;
    }

    boolean checkDateFormatISO8601(ConstraintValidatorContext context, Field field, String fieldValue) {
        try {
            //LocalDate.parse(fieldValue.toString(), DateTimeFormatter.ofPattern(java.time.format.DateTimeFormatter.ISO_DATE.toString()).withResolverStyle(ResolverStyle.STRICT));
            LocalDate.parse(fieldValue, java.time.format.DateTimeFormatter.ISO_DATE);
            return true;
        } catch (DateTimeParseException e) {
            addConstraint(context, "The entity field: "+field.getName()+" is of type Date and does not comply with the ISO-8601 format: "+fieldValue.toString()+".");
            return false;
        }
        
    }   
    
    @SuppressWarnings("unchecked")
    boolean validateURIField(ConstraintValidatorContext context, Field field, Object fieldValue) {
        boolean returnValue = true;
        if (field.getType().isAssignableFrom(ArrayList.class)) {
            returnValue = validateURIListField(context, field, (List<String>)fieldValue);
        }
        else if (field.getType().isAssignableFrom(String.class)){
        	returnValue = checkURIFormat(context, field, (String)fieldValue);
        }
        return returnValue;
    } 
    
    boolean validateURIListField(ConstraintValidatorContext context, Field field, List<String> fieldValues) {
    	boolean returnValue = true;
 		if (fieldValues.isEmpty()) {
 		    return true;
 		}
		for (String fieldValueElem : fieldValues) {
			if (checkURIFormat(context, field, fieldValueElem) == false && returnValue == true) {
				returnValue = false;
			}
		}		
        return returnValue;
    }    
    
    boolean checkURIFormat(ConstraintValidatorContext context, Field field, String fieldValue) {
    	if (EntityUtils.isUri(fieldValue)) {
    		return true;
    	}    	
    	else {
			addConstraint(context, "The entity field: "+field.getName()+" is of type: "+EntityFieldsTypes.getFieldType(field.getName())+" and it's element: "+fieldValue+" does not have the URI form.");
			return false;
		}
    }
    
    @SuppressWarnings("unchecked")
	boolean validateMultilingualFieldAndCheckPossibleUri(ConstraintValidatorContext context, Field field, Object fieldValue) {
    	boolean returnValue = true;
    	//per default the multilingual field must be of type Map and these fields are multilingual
		Map<Object,Object> fieldValueMap = (Map<Object,Object>)fieldValue;
		if (fieldValueMap.isEmpty()) { 
		    return true;
		}
		
		for (Map.Entry<Object, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
			//only if the key is an empty string, the value is of type URI
			if(((String)fieldValueMapElem.getKey()).equals("")) {
				if (fieldValueMapElem.getValue().getClass().isAssignableFrom(ArrayList.class)) {
					for (String fieldValueMapElemValue : (List<String>)(fieldValueMapElem.getValue())) {
						boolean returnValueLocal = checkURIFormat(context, field, fieldValueMapElemValue);
						if(returnValueLocal==false && returnValue==true) {
							returnValue = false;
						}
					}
				}
				else if (fieldValueMapElem.getValue().getClass().isAssignableFrom(String.class)){
					boolean returnValueLocal = checkURIFormat(context, field, (String)fieldValueMapElem.getValue());
					if(returnValueLocal==false && returnValue==true) {
						returnValue = false;
					}
				}
			}
			boolean returnValueLocal = validateLanguageCodes(context, field, fieldValueMapElem.getKey().toString());
			if(returnValueLocal==false && returnValue==true) {
				returnValue = false;
			}			
		}		
		return returnValue;
    }
    
	boolean validateLanguageCodes (ConstraintValidatorContext context, Field field, String languageCode) {
    	
		if (emLanguageCodes==null) return true;
		if (emLanguageCodes.getLanguages()==null) return true;
		
		boolean returnValue = true;
    	boolean foundAlternativeCodes = false;
		boolean foundKnownCode = false;

		for (Language language : emLanguageCodes.getLanguages()) {
			if (language.getCode().contains(languageCode)) {
				foundKnownCode = true;
				break;
			}
			if (language.getAlternativeLanguages()==null) continue;
			for (Language alternativeLanguage : language.getAlternativeLanguages()) {
				if (alternativeLanguage.getCode().contains(languageCode)) {
					if (foundAlternativeCodes == false) foundAlternativeCodes = true;
					foundKnownCode=true;
					break;
				}
			}
			if (foundKnownCode==true) break;
		}
		if (foundKnownCode==false) {
			addConstraint(context, "The entity field: "+field.getName()+" contains the language code: "+languageCode+" that does not belong to the Europena languge codes.");
			if (returnValue==true) {
				returnValue=false;
			}
		}			
		return returnValue;		
    }
    
    private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
    }

}
