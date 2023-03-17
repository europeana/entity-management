package eu.europeana.entitymanagement.normalization;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import eu.europeana.entitymanagement.vocabulary.ValidationEntity;

public class EntityFieldsCompleteValidationValidator
    implements ConstraintValidator<EntityFieldsCompleteValidationInterface, ValidationEntity> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsCompleteValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  public void initialize(EntityFieldsCompleteValidationInterface constraint) {
    // no initialization steps required.
  }

  @Override
  public boolean isValid(ValidationEntity entity, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    return emEntityFieldDatatypeValidation.validateEntity(entity, context, true, true);
  }
}
