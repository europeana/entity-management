package eu.europeana.entitymanagement.zoho.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoInMemoryTokenStore;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static eu.europeana.entitymanagement.zoho.utils.ZohoConstants.ZOHO_PROPERTIES_BEAN;

@Configuration(AppConfigConstants.BEAN_ZOHO_CONFIGURATION)
@PropertySource(value = "classpath:zoho_import.properties", ignoreResourceNotFound = true)
public class ZohoConfiguration {

  private static final Logger LOGGER = LogManager.getLogger(ZohoConfiguration.class);

  @Value("${zoho.email:#{null}}")
  private String zohoEmail;

  @Value("${zoho.client.id:#{null}}")
  private String zohoClientId;

  @Value("${zoho.client.secret:#{null}}")
  private String zohoClientSecret;

  @Value("${zoho.refresh.token:#{null}}")
  private String zohoRefreshToken;

  @Value("${zoho.redirect.url:#{null}}")
  private String zohoRedirectUrl;
  
  @Value("${zoho.base.url:#{null}}")
  private String zohoBaseUrl;
  
  private ZohoAccessClient zohoAccessClient;

  public ZohoAccessClient getZohoAccessClient() throws ZohoException {
    if (zohoAccessClient == null) {
      synchronized (this) {
        if (zohoAccessClient == null) {
          zohoAccessClient =
              new ZohoAccessClient(
                  new ZohoInMemoryTokenStore(),
                  zohoEmail,
                  zohoClientId,
                  zohoClientSecret,
                  zohoRefreshToken,
                  zohoRedirectUrl,
                  zohoProperties());
        }
      }
    }
    return zohoAccessClient;
  }

  /**
   * Loads and parses the Zoho properties from the YAML configuration file `zoho-fields.yaml`.
   * The method uses Jackson's ObjectMapper with a YAML factory to map the YAML content
   * into an instance of the {@link ZohoProperties} class.
   *
   * If the file cannot be read or an exception occurs during parsing, the method logs an error
   * and returns null.
   *
   * @return an instance of {@link ZohoProperties} containing the parsed configuration,
   *         or null if an error occurs.
   */
  @Bean(ZOHO_PROPERTIES_BEAN)
  protected ZohoProperties zohoProperties() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

      ZohoProperties zohoProperties =  mapper.readValue(
              new ClassPathResource("zoho-fields.yaml").getInputStream(),
              ZohoProperties.class);
      LOGGER.info("Successfully loaded zoho-fields.yaml .... {}", zohoProperties.getModules().keySet());
      return zohoProperties;
    } catch (IOException e) {
        LOGGER.error("Could not read zoho-fields.yaml.. !! ", e.getMessage());
    }
    return null;
  }

  public String getZohoBaseUrlOrganizations() {
    StringBuilder builder = new StringBuilder(zohoBaseUrl);
    if(!zohoBaseUrl.endsWith("/")) {
      builder.append('/');
    }
    return builder.append(ZohoConstants.ACCOUNTS_MODULE_NAME).append('/').toString();
  }
  
  public String getZohoBaseUrlAggregators() {
    StringBuilder builder = new StringBuilder(zohoBaseUrl);
    if(!zohoBaseUrl.endsWith("/")) {
      builder.append('/');
    }
    return builder.append(ZohoConstants.AGGREGATORS_MODULE_NAME).append('/').toString();
  }

  public void setZohoBaseUrl(String zohoBaseUrl) {
    this.zohoBaseUrl = zohoBaseUrl;
  }
  
}
