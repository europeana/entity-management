package eu.europeana.entitymanagement.normalization;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class EntityFieldsEuropeanaProxyValidationValidator
    implements ConstraintValidator<EntityFieldsEuropeanaProxyValidationInterface, Entity> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsEuropeanaProxyValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {
    return EntityUtils.validateEntity(
        entity, context, emEntityFieldDatatypeValidation, false, true);
  }
}
