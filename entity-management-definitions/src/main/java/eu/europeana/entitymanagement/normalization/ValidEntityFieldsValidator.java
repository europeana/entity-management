package eu.europeana.entitymanagement.normalization;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.entitymanagement.common.config.EMSettings;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language;
import eu.europeana.entitymanagement.common.config.LanguageCodes.Language.AlternativeLanguage;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.utils.EntityUtils;

public class ValidEntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity>{

    @Autowired
    private EMSettings emSettings;
    
	private static final Logger logger = LogManager.getLogger(BaseEntity.class);
	
	public void initialize(ValidEntityFields constraint) {
	}
	
	@Override
	@Deprecated
	/**
	 * @deprecated required refactoring and proper exception handling 
	 * consider moving this class to a more appropriate module
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
				// remove spaces from the fields values
				if (fieldType.isAssignableFrom(String.class)) {					
					if (((String)fieldValue).contains(" ")) {
						addConstraint(returnValue, context, "The entity field: "+field.getName()+" contains not allowed spaces.");
						returnValue=false;
						String newFieldValue = ((String)fieldValue).replaceAll("\\s+","");
						//improving the situation and removing the spaces
						entity.setFieldValue(field, newFieldValue);
					}
				}
				//check the language labels for the altLabel field to be in the predefined language labels
				if (fieldName.contains(emSettings.getAltLabelFieldNamePrefix())) {
					Map<String, List<String>> newAltLabel = new HashMap<>();
					Map<String,List<String>> oldAltLabel = (Map<String,List<String>>) fieldValue;
					boolean foundAlternativeCodes = false;
					for (Map.Entry<String, List<String>> altLabel : oldAltLabel.entrySet()) {
						boolean foundKnownCode = false;
						if (emSettings.getLanguageCodes()==null) break;
						if (emSettings.getLanguageCodes().getLanguages()==null) break;
						for (Language language : emSettings.getLanguageCodes().getLanguages()) {
							String altLabelEnding = altLabel.getKey().substring(altLabel.getKey().lastIndexOf("_") + 1);
							if (language.getCode().contains(altLabelEnding)) {
								foundKnownCode = true;
								newAltLabel.put(altLabel.getKey(), altLabel.getValue());
								break;
							}
							if (language.getAlternativeLanguages()==null) continue;
							for (AlternativeLanguage alternativeLanguage : language.getAlternativeLanguages()) {
								if (alternativeLanguage.getCode()!=null && alternativeLanguage.getCode().contains(altLabelEnding)) {
									if (foundAlternativeCodes == false) foundAlternativeCodes = true;
									List<String> newAltLabelValue = new ArrayList<>(altLabel.getValue());
									newAltLabel.put(emSettings.getAltLabelFieldNamePrefix()+emSettings.getLanguageSeparator()+language.getCode(), newAltLabelValue);
									foundKnownCode=true;
									break;
								}
							}
							if (foundKnownCode==true) break;
						}
						if (foundKnownCode==false) {
							newAltLabel.put(altLabel.getKey(), altLabel.getValue());
							addConstraint(returnValue, context, "The alternative label field: "+ altLabel.getKey() +" contain the language code that does not belong to the Europena languge codes.");
							returnValue=false;

						}
						
					}
					if (foundAlternativeCodes==true) entity.setFieldValue(field, newAltLabel);
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.error("During the reconceliation of the entity data from different sources a method has been passed an illegal or inappropriate argument.", e);
		} catch (IllegalAccessException e) {
			logger.error("During the reconceliation of the entity data from different sources an illegal access to some method or field has happened.", e);
		} catch (IOException e) {
			logger.error("An IOException happended during the validation of the entity fields",e);
		}
		
		return returnValue;
	}
	
	private void addConstraint (boolean returnValue, ConstraintValidatorContext context, String messageTemplate) {
		if (returnValue) {
			//disable existing violation message
		    context.disableDefaultConstraintViolation();
		    //build new violation message and add it
			context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
		}
		else {
		    //build new violation message and add it
			context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
		}
	}
	
}
