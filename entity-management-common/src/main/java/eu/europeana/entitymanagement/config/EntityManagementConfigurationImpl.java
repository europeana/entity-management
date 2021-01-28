package eu.europeana.entitymanagement.config;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityManagementConfigurationImpl implements EntityManagementConfiguration {

	@Autowired
	private EMSettings emSetting;
	
	private Properties entityProperties;
	
	@Override
	public String getComponentName() {
		return "entitymanagement";
	}


	public Properties getEntityProperties() {
		return entityProperties;
	}

	public void setEntityPropertiesConfig(Properties entityProperties) {
		this.entityProperties = entityProperties;
	}

//	@Override
//	public boolean isProductionEnvironment() {
//		return VALUE_ENVIRONMENT_PRODUCTION.equals(getEnvironment());
//	}

//	@Override
//	public String getEnvironment() {
//		return getEntityProperties().getProperty(ENTITY_ENVIRONMENT);
//	}

//	@Override
//	public String getUserToken() {
//		return getEntityProperties().getProperty(DEFAULT_USER_TOKEN);
//	}


	@Override
	public String getJwtTokenSignatureKey() {
	    return getEntityProperties().getProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY);
	}

	@Override
	public String getAuthorizationApiName() {
	    return getEntityProperties().getProperty(AUTHORIZATION_API_NAME);
	}
	
	
	@Override
	public String getApiVersion() {
	    return getEntityProperties().getProperty(API_VERSION);
	}	
	
    @PostConstruct
    public void init() {
    	entityProperties = new Properties();
    	entityProperties.setProperty(API_VERSION, emSetting.getEntitymanagementApiVersion());
    	entityProperties.setProperty(AUTHORIZATION_API_NAME, emSetting.getAuthorizationApiName());
    	entityProperties.setProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY, emSetting.getEuropeanaApikeyJwttokenSiganturekey());
    }

}
