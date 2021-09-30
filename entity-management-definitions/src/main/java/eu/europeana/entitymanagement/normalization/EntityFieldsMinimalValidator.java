package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.util.List;

import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.definitions.exceptions.EntityFieldAccessException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;

@Component
public class EntityFieldsMinimalValidator implements ConstraintValidator<EntityFieldsMinimalValidatorInterface, Entity> {
    
    public void initialize(EntityFieldsMinimalValidatorInterface constraint) {
//        System.out.println();
    }

    @Override
	public boolean isValid(Entity entity, ConstraintValidatorContext context) {	
    			
		if (entity==null) {
			return false;
		}
		
		boolean returnValue=true;
		
		List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());
		
		try {
			for (Field field : entityFields) {
				Object fieldValue = entity.getFieldValue(field);
				String fieldName = field.getName();

				if (!EntityFieldsTypes.hasTypeDefinition(fieldName)) {
					continue;
				}

				if (fieldValue == null) {
					if (EntityFieldsTypes.isMandatory(fieldName)) {
						addConstraint(context, "The mandatory field: " + fieldName + " cannot be NULL.");
						if (returnValue) {
							returnValue = false;
						}
					}

					continue;
				}

				int minContentCount = EntityFieldsTypes.getMinContentCount(fieldName);
				if (minContentCount > 0) {
					if ((List.class.isAssignableFrom(fieldValue.getClass())
							&& ((List<?>) fieldValue).size() < minContentCount) ||
							(Map.class.isAssignableFrom(fieldValue.getClass())
									&& ((Map<?, ?>) fieldValue).size() < minContentCount)) {
						addConstraint(context,
								fieldName + " must have at least " + minContentCount + " values");
					}
				}
			}
		}
		catch (IllegalArgumentException e) {
		    throw new EntityFieldAccessException("During the validation of the entity fields an illegal or inappropriate argument exception has happened.", e);	    
		} catch (IllegalAccessException e) {
		    throw new EntityFieldAccessException("During the validation of the entity fields an illegal access to some method or field has happened.", e);
		}
		
		return returnValue;
    }
    
    private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
    }

}
