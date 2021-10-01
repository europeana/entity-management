package eu.europeana.entitymanagement.zoho.organization;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.store.FileStore;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration(AppConfigConstants.BEAN_ZOHO_ACCESS_CONFIGURATION)
@PropertySource(value = "classpath:zoho_import.properties", ignoreResourceNotFound = true)
public class ZohoAccessConfiguration {

  private static final Logger LOGGER = LogManager.getLogger(ZohoAccessConfiguration.class);

  public ZohoAccessConfiguration() {
    LOGGER.info("Initializing ZohoOrganizationImporterConfiguration bean as: configuration.");
  }

  @Value("${zoho.email:#{null}}")
  private String zohoEmail;

  @Value("${zoho.client.id:#{null}}")
  private String zohoClientId;

  @Value("${zoho.client.secret:#{null}}")
  private String zohoClientSecret;

  @Value("${zoho.grant.token:#{null}}")
  private String zohoGrantToken;

  @Value("${zoho.refresh.token:#{null}}")
  private String zohoRefreshToken;

  @Value("${zoho.redirect.url:#{null}}")
  private String zohoRedirectUrl;

  @Value("${token.store.file.path:#{null}}")
  private String tokenFile;

  TokenStore fileTokenStore;

  ZohoAccessClient zohoAccessClient;

  TokenStore getFileTokenStore() throws Exception {
    if (fileTokenStore != null) {
      return fileTokenStore;
    }

    if (StringUtils.isBlank(tokenFile)) {
      return null;
    }

    File tokenStoreFile = new File(tokenFile);
    initTokenStore(tokenStoreFile);

    return fileTokenStore;
  }

  void initTokenStore(File tokenStoreFile) throws Exception {
    if (tokenStoreFile.exists()) {
      // create fresh token store
      tokenStoreFile.delete();
    } else {
      if (tokenStoreFile.getParentFile() != null) tokenStoreFile.getParentFile().mkdirs();
    }
    // create empty token store
    boolean created = tokenStoreFile.createNewFile();
    if (!created) {
      throw new IOException(
          "Cannot create token store. Please verify the properties for the configuration of file token store and verify that the application has permissions to create the file for the token store. ");
    }
    fileTokenStore = new FileStore(tokenStoreFile.getAbsolutePath());
  }

  public ZohoAccessClient getZohoAccessClient() throws Exception {
    if (zohoAccessClient != null) {
      return zohoAccessClient;
    }

    if (zohoGrantToken == null || zohoGrantToken.length() < 6) {
      throw new IllegalArgumentException("zoho.authentication.token is invalid: " + zohoGrantToken);
    }
    LOGGER.info("Using zoho authentication token: {} ...", zohoGrantToken.substring(0, 3));
    zohoAccessClient =
        new ZohoAccessClient(
            getFileTokenStore(),
            zohoEmail,
            zohoClientId,
            zohoClientSecret,
            zohoGrantToken,
            zohoRedirectUrl);

    if (!StringUtils.isBlank(zohoRefreshToken)) {
      // if available, switch to the use of existing refresh token
      OAuthToken refreshToken =
          new OAuthToken(
              zohoClientId, zohoClientSecret, zohoRefreshToken, TokenType.REFRESH, zohoRedirectUrl);
      refreshToken.setUserMail(zohoEmail);
      refreshToken.setAccessToken("accessToken-placeholder");
      refreshToken.setExpiresIn("0");
      fileTokenStore.saveToken(new UserSignature(zohoEmail), refreshToken);

      SDKConfig sdkConfig =
          new SDKConfig.Builder().setAutoRefreshFields(false).setPickListValidation(true).build();
      Environment environment = USDataCenter.PRODUCTION;
      Initializer.switchUser(new UserSignature(zohoEmail), environment, refreshToken, sdkConfig);
    }

    return zohoAccessClient;
  }
}
