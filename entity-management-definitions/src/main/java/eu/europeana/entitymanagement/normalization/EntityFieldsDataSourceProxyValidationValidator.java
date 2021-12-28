package eu.europeana.entitymanagement.normalization;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.utils.EntityUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class EntityFieldsDataSourceProxyValidationValidator
    implements ConstraintValidator<EntityFieldsDataSourceProxyValidationInterface, Entity> {

  private final EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation;

  public EntityFieldsDataSourceProxyValidationValidator(
      EntityFieldsDatatypeValidation emEntityFieldDatatypeValidation) {
    this.emEntityFieldDatatypeValidation = emEntityFieldDatatypeValidation;
  }

  public void initialize(EntityFieldsCompleteValidationInterface constraint) {
    // no initialization steps required.
  }

  @Override
  public boolean isValid(Entity entity, ConstraintValidatorContext context) {
    context.disableDefaultConstraintViolation();
    return EntityUtils.validateEntity(
        entity, context, emEntityFieldDatatypeValidation, false, false);
  }
}
