package eu.europeana.entitymanagement.config;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_LANGUAGE_CODES;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_VALIDATOR_FACTORY;
import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_XML_MAPPER;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.LanguageCodes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Component
public class ValidatorConfig {

  private final EntityManagementConfiguration emConfiguration;

  private final XmlMapper xmlMapper;

  @Autowired
  public ValidatorConfig(
      EntityManagementConfiguration emConfiguration,
      @Qualifier(BEAN_XML_MAPPER) XmlMapper xmlMapper) {
    this.emConfiguration = emConfiguration;
    this.xmlMapper = xmlMapper;
  }

  @Bean(name = BEAN_EM_LANGUAGE_CODES)
  public LanguageCodes getLanguageCodes() throws IOException {

    String languagecodesXMLConfig = emConfiguration.getLanguagecodesXMLConfig();
    try (InputStream inputStream = getClass().getResourceAsStream(languagecodesXMLConfig);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
      return xmlMapper.readValue(contents, LanguageCodes.class);
    }
  }

  @Bean(name = BEAN_EM_VALIDATOR_FACTORY)
  public ValidatorFactory getValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }
}
