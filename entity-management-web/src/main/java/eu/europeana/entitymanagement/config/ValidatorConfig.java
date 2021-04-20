package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_ENTITY_FIELD_CLEANER;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_LANGUAGE_CODES;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_VALIDATOR_FACTORY;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_XML_MAPPER;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidatorConfig {

  private final XmlMapper xmlMapper;
  private final EntityManagementConfiguration emConfiguration;

  @Autowired
  public ValidatorConfig(@Qualifier(BEAN_XML_MAPPER) XmlMapper xmlMapper,
      EntityManagementConfiguration emConfiguration) {
    this.xmlMapper = xmlMapper;
    this.emConfiguration = emConfiguration;
  }

  @Bean(name=BEAN_EM_VALIDATOR_FACTORY)
  public ValidatorFactory getValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  @Bean(name=BEAN_EM_ENTITY_FIELD_CLEANER)
  public EntityFieldsCleaner getEntityFieldsCleanerBean() throws IOException {
    return new EntityFieldsCleaner(getLanguageCodes());
  }

  @Bean(name=BEAN_EM_LANGUAGE_CODES)
  public LanguageCodes getLanguageCodes() throws IOException {
    String languagecodesXMLConfig = emConfiguration.getLanguagecodesXMLConfig();
    try (InputStream inputStream = getClass().getResourceAsStream(languagecodesXMLConfig)) {
      assert inputStream != null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        return xmlMapper.readValue(contents, LanguageCodes.class);
      }
    }
  }
}
