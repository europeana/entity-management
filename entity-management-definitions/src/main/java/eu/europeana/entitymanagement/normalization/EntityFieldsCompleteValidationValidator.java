package eu.europeana.entitymanagement.normalization;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
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

  public void initialize(EntityFieldsCompleteValidationInterface constraint) {
    // no initialization steps required.
  }

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {

    return EntityUtils.validateEntity(entity, context, emEntityFieldDatatypeValidation, true, true);
  }
}
