package eu.europeana.entitymanagement.web.service;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;

@Component(AppConfig.BEAN_AUTHORIZATION_SERVICE)
public class AuthorizationService extends BaseAuthorizationService implements eu.europeana.api.commons.service.authorization.AuthorizationService {

    protected final Logger logger = LogManager.getLogger(getClass());

    EntityManagementConfiguration emConfiguration;

    @Resource(name="clientDetailsService")
    EuropeanaClientDetailsService clientDetailsService;

    public AuthorizationService() {

    }

    public EntityManagementConfiguration getConfiguration() {
	return emConfiguration;
    }

    public void setConfiguration(EntityManagementConfiguration configuration) {
	this.emConfiguration = configuration;
    }

    @Override
    protected ClientDetailsService getClientDetailsService() {
	return clientDetailsService;
    }

    @Override
    protected String getSignatureKey() {
	return getConfiguration().getJwtTokenSignatureKey();
    }


    @Override
    protected String getApiName() {
	return getConfiguration().getAuthorizationApiName();
    }

	@Override
	protected Role getRoleByName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
