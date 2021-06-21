package eu.europeana.entitymanagement.normalization;

import java.lang.reflect.Field;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import eu.europeana.entitymanagement.definitions.exceptions.EntityValidationException;
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
				if (EntityFieldsTypes.hasTypeDefinition(field.getName()) && EntityFieldsTypes.isMandatory(field.getName()) && fieldValue==null) { 
					addConstraint(context, "The mandatory field: "+field.getName()+" cannot be NULL.");
	                if(returnValue==true) {
	                	returnValue=false;
	                }
				}
			}
		}
		catch (IllegalArgumentException e) {
		    throw new EntityValidationException("During the validation of the entity fields an illegal or inappropriate argument exception has happened.", e);	    
		} catch (IllegalAccessException e) {
		    throw new EntityValidationException("During the validation of the entity fields an illegal access to some method or field has happened.", e);
		}
		
		return returnValue;
    }
    
    private void addConstraint(ConstraintValidatorContext context, String messageTemplate) {
	    context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
    }

}
