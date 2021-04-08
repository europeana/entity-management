package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language.AlternativeLanguage;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;

@Component
/**
 * @Deprecated the component annotation must not be used in this module
 */
@Deprecated
public class ValidEntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity> {

    @Resource(name="emLanguageCodes")
    @Deprecated
    /**
     * @deprecated data model must not access any beans
     */
    LanguageCodes emLanguageCodes;
    
    @Deprecated
    /**
     * @deprecated verify if needed and use the name according to the naming conventions
     */
    public static final String altLabelFieldNamePrefix = "altLabel";
    @Deprecated
    /**
     * @deprecated verify if needed and use the name according to the naming conventions
     */
    public static final String altLabelCharacterSeparator = "_";

    @Deprecated
    /**
     * @deprecated verify if needed and use the name according to the naming conventions
     */
    public static final String languageSeparator = "_";

    
    private static final Logger logger = LogManager.getLogger(ValidEntityFieldsValidator.class);

    public void initialize(ValidEntityFields constraint) {
    }

    @Override
    @Deprecated
    /**
     * @deprecated required refactoring and proper exception handling consider
     *             moving this class to a more appropriate module
     *
     */
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
				//remove spaces from the String fields
				if (fieldType.isAssignableFrom(String.class)) {					
					if (((String)fieldValue).contains(" ")) {
						addConstraint(returnValue, context, "The entity field: "+field.getName()+" contains not allowed spaces.");
						returnValue=false;
						String newFieldValue = ((String)fieldValue).replaceAll("\\s+","");
						//improving the situation and removing the spaces
						entity.setFieldValue(field, newFieldValue);
					}
				}
				//remove spaces from the keys in the Map fields 
				else if (fieldType.isAssignableFrom(Map.class)) {
					Map<Object,Object> oldFieldValue = (Map<Object,Object>)fieldValue;
					if (oldFieldValue.size()<1) continue;
					Map<Object,Object> newFieldValue = new HashMap<>();
					boolean changedValues = false;
					for (Map.Entry oldFieldValueElem : oldFieldValue.entrySet()) 
					{
						String newKey = (String) oldFieldValueElem.getKey();
						if (((String)oldFieldValueElem.getKey()).contains(" ")) {
							addConstraint(returnValue, context, "The entity field: "+field.getName()+" contains not allowed spaces in the key: "+(String)oldFieldValueElem.getKey());
							returnValue=false;
							if (changedValues == false) changedValues = true;
							newKey = ((String)oldFieldValueElem.getKey()).replaceAll("\\s+","");
						}
						newFieldValue.put(newKey, oldFieldValueElem.getValue());						
					}
					if (changedValues == true) entity.setFieldValue(field, newFieldValue);					
				}
				
				//check the language labels for the altLabel field to be in the predefined language labels
				//getting the new, updated value with removed spaces
				fieldValue = entity.getFieldValue(field);
				if (fieldName.contains(altLabelFieldNamePrefix)) {
					Map<String, List<String>> newAltLabel = new HashMap<>();
					Map<String,List<String>> oldAltLabel = (Map<String,List<String>>) fieldValue;
					boolean foundAlternativeCodes = false;
					for (Map.Entry<String, List<String>> altLabel : oldAltLabel.entrySet()) {
						boolean foundKnownCode = false;
						if (emLanguageCodes==null) break;
						if (emLanguageCodes.getLanguages()==null) break;
						for (Language language : emLanguageCodes.getLanguages()) {
							String altLabelEnding = altLabel.getKey().substring(altLabel.getKey().lastIndexOf("_") + 1);
							if (language.getCode().contains(altLabelEnding)) {
								foundKnownCode = true;
								newAltLabel.put(altLabel.getKey(), altLabel.getValue());
								break;
							}
							if (language.getAlternativeLanguages()==null) continue;
							for (AlternativeLanguage alternativeLanguage : language.getAlternativeLanguages()) {
								if (alternativeLanguage.getCode().contains(altLabelEnding)) {
									if (foundAlternativeCodes == false) foundAlternativeCodes = true;
									List<String> newAltLabelValue = new ArrayList<>(altLabel.getValue());
									newAltLabel.put(altLabelFieldNamePrefix+altLabelCharacterSeparator+language.getCode(), newAltLabelValue);
									foundKnownCode=true;
									break;
								}
							}
							if (foundKnownCode==true) break;
						}
						if (foundKnownCode==false) {
							newAltLabel.put(altLabel.getKey(), altLabel.getValue());
							addConstraint(returnValue, context, "The alternative label field: "+ altLabel.getKey() +" contains the language code that does not belong to the Europena languge codes.");
							returnValue=false;
						}
						
					}
					if (foundAlternativeCodes==true) entity.setFieldValue(field, newAltLabel);
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
