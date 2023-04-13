package eu.europeana.entitymanagement.normalization;

import eu.europeana.entitymanagement.vocabulary.ValidationObject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EntityFieldsCompleteValidationValidator
    implements ConstraintValidator<EntityFieldsCompleteValidationInterface, ValidationObject> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsCompleteValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  public void initialize(EntityFieldsCompleteValidationInterface constraint) {
    // no initialization steps required.
  }

  @Override
  public boolean isValid(ValidationObject entity, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    return emEntityFieldDatatypeValidation.validateEntity(entity, context, true, true);
  }
}
