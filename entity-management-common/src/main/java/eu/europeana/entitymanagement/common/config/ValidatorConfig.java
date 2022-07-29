package eu.europeana.entitymanagement.common.config;

import eu.europeana.entitymanagement.definitions.LanguageCodes;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationValidator;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationValidator;
import eu.europeana.entitymanagement.normalization.EntityFieldsDatatypeValidation;
import eu.europeana.entitymanagement.normalization.EntityFieldsEuropeanaProxyValidationValidator;
import org.springframework.context.annotation.Bean;

public class ValidatorConfig {

  @Bean
  public EntityFieldsEuropeanaProxyValidationValidator
      entityFieldsEuropeanaProxyValidationValidator() {
    return new EntityFieldsEuropeanaProxyValidationValidator(
        new EntityFieldsDatatypeValidation(new LanguageCodes()));
  }

  @Bean
  public EntityFieldsDataSourceProxyValidationValidator
      entityFieldsDataSourceProxyValidationValidator() {
    return new EntityFieldsDataSourceProxyValidationValidator(
        new EntityFieldsDatatypeValidation(new LanguageCodes()));
  }

  @Bean
  public EntityFieldsCompleteValidationValidator entityFieldsCompleteValidationValidator() {
    return new EntityFieldsCompleteValidationValidator(
        new EntityFieldsDatatypeValidation(new LanguageCodes()));
  }
}
