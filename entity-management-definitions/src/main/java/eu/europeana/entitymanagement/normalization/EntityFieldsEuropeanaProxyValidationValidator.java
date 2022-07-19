package eu.europeana.entitymanagement.normalization;

import eu.europeana.entitymanagement.definitions.model.Entity;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EntityFieldsEuropeanaProxyValidationValidator
    implements ConstraintValidator<EntityFieldsEuropeanaProxyValidationInterface, Entity> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsEuropeanaProxyValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    return emEntityFieldDatatypeValidation.validateEntity(entity, context, false, true);
  }
}
