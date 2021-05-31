package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_TEXT_OR_URI;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

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

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.exceptions.EntityValidationException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;

@Component
public class EntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity> {

    @Resource(name="emLanguageCodes")
    LanguageCodes emLanguageCodes;
    
    public void initialize(ValidEntityFields constraint) {
//        System.out.println();
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
					returnValueLocal = validateMultilingualField(context, field, fieldValue);
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
				        //Text or Keyword
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

    


    boolean validateStringField(ConstraintValidatorContext context, Field field, String fieldValue) {
	if (!fieldValue.equals(fieldValue.trim())) {
		addConstraint(context, "The entity field: "+field.getName()+", contains leading and/or trailing spaces: "+fieldValue+".");
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
    boolean validateMultilingualField(ConstraintValidatorContext context, Field field, Object fieldValue) {
        // per default the multilingual field must be of type Map and these fields are
        // multilingual
        Map<String, Object> fieldValueMap = (Map<String, Object>) fieldValue;
        if (fieldValueMap.isEmpty()) {
            return true;
        }
        
        //validate language codes
        boolean returnValue = validateLanguageCodes(context, field, fieldValueMap.keySet());
        
        //field names are validted in main method
        String fieldType = EntityFieldsTypes.getFieldType(field.getName());
        boolean definitionIsList = EntityFieldsTypes.isList(field.getName()); 
        
        //validate values
        boolean localReturnValue;
        boolean valueIsList;
        for (Map.Entry<String, Object> fieldValueMapElem : fieldValueMap.entrySet()) {
            valueIsList = fieldValueMapElem.getValue().getClass().isAssignableFrom(ArrayList.class);
            // only if the key is an empty string, the value is of type URI
            if(definitionIsList) {
                //string list
                if(!valueIsList){
                    //check cardinality for list
                    addConstraint(context, "The entity field: "+field.getName()+" cardinality: "+EntityFieldsTypes.valueOf(field.getName()).getFieldCardinality()+" and must not be represented as list");
                    localReturnValue = false;
                }else {
                    List<String> values =  (List<String>) fieldValueMapElem.getValue();
                    for (String multilingualValue : values) {
                        localReturnValue = validateMultilingualValue(context, field, fieldValueMapElem.getKey(), multilingualValue, fieldType);
                        returnValue = returnValue && localReturnValue;                    
                    }
                }
            }else {
                //string value
                if(valueIsList){
                    //check cardinality for single valued
                    addConstraint(context, "The entity field: "+field.getName()+" cardinality: "+EntityFieldsTypes.valueOf(field.getName()).getFieldCardinality()+" and must not be represented as list");
                    localReturnValue = false;
                }else {
                    String multilingualValue = (String) fieldValueMapElem.getValue();
                    localReturnValue = validateMultilingualValue(context, field, fieldValueMapElem.getKey(), multilingualValue, fieldType);
                }
                returnValue = returnValue && localReturnValue;
                
            }            
        }
        return returnValue;
    }
    
	private boolean validateMultilingualValue(ConstraintValidatorContext context, Field field, String key, String multilingualValue,
            String fieldType) {
	    if("".equals(key) && FIELD_TYPE_TEXT_OR_URI.equals(fieldType)) {
	        return checkURIFormat(context, field, multilingualValue);
	    }else {
	        return validateStringField(context, field, multilingualValue);
	    }
    }

    boolean validateLanguageCodes (ConstraintValidatorContext context, Field field, Set<String> keySet) {
    	
//		if (emLanguageCodes==null) return true;
//		if (emLanguageCodes.getLanguages()==null) return true;
	        boolean isValid = true;
		for (String key : keySet) {
                    if(!emLanguageCodes.isValidLanguageCode(key) && !emLanguageCodes.isValidAltLanguageCode(key)) {
                        addConstraint(context, "The entity field: "+field.getName()+" contains the language code: "+key+" that does not belong to the Europena languge codes.");                        
                    }
                }
		return isValid;		
    }
    
    private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
    }

}
