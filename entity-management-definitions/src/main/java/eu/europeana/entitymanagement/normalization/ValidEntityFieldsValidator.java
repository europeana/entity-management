package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;
import eu.europeana.entitymanagement.utils.EntityUtils;

public class ValidEntityFieldsValidator implements ConstraintValidator<ValidEntityFields, Entity>{

	private static final Logger logger = LogManager.getLogger(BaseEntity.class);

	public void initialize(ValidEntityFields constraint) {
	}
	
	@Override
	public boolean isValid(Entity entity, ConstraintValidatorContext context) {		
		
		if (entity==null) {
			return false;
		}
		
		List<Field> entityFields = new ArrayList<>();
		EntityUtils.getAllFields(entityFields, entity.getClass());
		
		try {			
			for (Field field : entityFields) {
				Class<?> fieldType = field.getType();
				// check that the fields values do not contain spaces
				if (fieldType.isAssignableFrom(String.class)) {
					String fieldValue = (String) entity.getFieldValue(field);
					if (fieldValue!=null && fieldValue.contains(" ")) {
						//disable existing violation message
					    context.disableDefaultConstraintViolation();
					    //build new violation message and add it
					    context.buildConstraintViolationWithTemplate("The entity field "+field.getName()+" has not allowed spaces in its value.").addConstraintViolation();
						return false;
					}
				}
			}
		}
		catch (IllegalArgumentException e) {
			logger.error("During the reconceliation of the entity data from different sources a method has been passed an illegal or inappropriate argument.", e);
		} catch (IllegalAccessException e) {
			logger.error("During the reconceliation of the entity data from different sources an illegal access to some method or field has happened.", e);
		}
		
		return true;
	}
	
}
