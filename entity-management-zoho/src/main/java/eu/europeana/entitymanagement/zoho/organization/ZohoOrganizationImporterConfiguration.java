package eu.europeana.entitymanagement.zoho.organization;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.zoho.api.authenticator.store.FileStore;
import com.zoho.api.authenticator.store.TokenStore;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;

@Configuration(AppConfigConstants.BEAN_ZOHO_ORGANIZATION_IMPORTER_CONFIGURATION)
@PropertySource(value="classpath:zoho_import.properties", ignoreResourceNotFound = true)
public class ZohoOrganizationImporterConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(ZohoOrganizationImporterConfiguration.class);

    public ZohoOrganizationImporterConfiguration() {
    	LOGGER.info("Initializing ZohoOrganizationImporterConfiguration bean as: configuration.");
    }
    
    @Value("${zoho.email}")
    private String zohoEmail;
    
    @Value("${zoho.client.id}")
    private String zohoClientId;
    
    @Value("${zoho.client.secret}")
    private String zohoClientSecret;
    
    @Value("${zoho.grant.token}")
    private String zohoGrantToken;
    
    @Value("${zoho.redirect.url}")
    private String zohoRedirectUrl;
    
    @Value("${token.store.file.path}")
    private String tokenFile;

    public FileStore getFileTokenStore() throws Exception {
    	String filepath=Thread.currentThread().getContextClassLoader().getResource(tokenFile).getPath();
    	return new FileStore(filepath);
    }

    public ZohoAccessClient getZohoAccessClient() throws Exception {
        if (zohoGrantToken == null || zohoGrantToken.length() < 6) {
            throw new IllegalArgumentException("zoho.authentication.token is invalid: " + zohoGrantToken);
        }
        LOGGER.info("Using zoho authentication token: {} ..." , zohoGrantToken.substring(0, 3));
        return new ZohoAccessClient(
                getTokenStore(),
                zohoEmail,
                zohoClientId,
                zohoClientSecret,
                zohoGrantToken,
                zohoRedirectUrl);
    }

    private TokenStore getTokenStore() throws Exception {
        TokenStore tokenStore = getFileTokenStore();
        if(tokenStore==null || tokenStore.getTokens().isEmpty()) {
            throw new IllegalArgumentException("Something went wrong with token store. Please verify the properties for the file token store.");
        }
        return tokenStore;
    }

}
