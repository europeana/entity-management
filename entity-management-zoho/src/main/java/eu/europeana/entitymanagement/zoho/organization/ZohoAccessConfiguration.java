package eu.europeana.entitymanagement.zoho.organization;

import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoInMemoryTokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration(AppConfigConstants.BEAN_ZOHO_ACCESS_CONFIGURATION)
@PropertySource(value = "classpath:zoho_import.properties", ignoreResourceNotFound = true)
public class ZohoAccessConfiguration {

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
  
  @Value("${zoho.baseUrl=: #{null}}")
  private String zohoBaseUrl;

  private volatile ZohoAccessClient zohoAccessClient;

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
                  zohoRedirectUrl);
        }
      }
    }
    return zohoAccessClient;
  }

  public String getZohoBaseUrl() {
    return zohoBaseUrl;
  }
}
