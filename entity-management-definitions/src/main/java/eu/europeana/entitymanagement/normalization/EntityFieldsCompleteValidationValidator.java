package eu.europeana.entitymanagement.normalization;

import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_DATE;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_EMAIL;
import static eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes.FIELD_TYPE_URI;

import eu.europeana.entitymanagement.definitions.exceptions.EntityFieldAccessException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class EntityFieldsCompleteValidationValidator
    implements ConstraintValidator<EntityFieldsCompleteValidationInterface, Entity> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsCompleteValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  public void initialize(EntityFieldsCompleteValidationInterface constraint) {}

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {

    if (entity == null) {
      return false;
    }

    boolean returnValue = true;

    List<Field> entityFieldsIncludingInheritedAndNested = new ArrayList<Field>();
    List<Object> entityFieldIncludingInheritedAndNestedValues = new ArrayList<Object>();

    try {
      EntityUtils.getAllFieldsIncludingInheritedAndNested(
          entityFieldsIncludingInheritedAndNested,
          entityFieldIncludingInheritedAndNestedValues,
          entity);

      for (int i = 0; i < entityFieldsIncludingInheritedAndNested.size(); i++) {

        Field field = entityFieldsIncludingInheritedAndNested.get(i);
        String fieldName = field.getName();
        if (!EntityFieldsTypes.hasTypeDefinition(fieldName)) {
          // there is no type definition to validate against
          continue;
        }

        Object fieldValue = entityFieldIncludingInheritedAndNestedValues.get(i);

        // validating the mandatory fields
        boolean returnValueLocal = emEntityFieldDatatypeValidation.valildateMandatoryField(context, fieldName, fieldValue);
        
        returnValue = returnValue && returnValueLocal;        
        
        //validating the field datatype compliance
        returnValueLocal = emEntityFieldDatatypeValidation.validateDatatypeCompliance(context, field, fieldValue);

        returnValue = returnValue && returnValueLocal;
      }
    } catch (IllegalArgumentException e) {
      throw new EntityFieldAccessException(
          "During the validation of the entity fields an illegal or inappropriate argument exception has happened.",
          e);
    } catch (IllegalAccessException e) {
      throw new EntityFieldAccessException(
          "During the validation of the entity fields an illegal access to some method or field has happened.",
          e);
    }

    return returnValue;
  }
}
