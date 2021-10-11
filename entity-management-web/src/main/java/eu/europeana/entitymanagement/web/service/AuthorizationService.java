package eu.europeana.entitymanagement.web.service;

import eu.europeana.entitymanagement.web.auth.Roles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final EntityManagementConfiguration emConfiguration;
    private  final EuropeanaClientDetailsService clientDetailsService;

    @Autowired
    public AuthorizationService(
        EntityManagementConfiguration emConfiguration,
        EuropeanaClientDetailsService clientDetailsService) {
      this.emConfiguration = emConfiguration;

      this.clientDetailsService = clientDetailsService;
    }


    @Override
    protected ClientDetailsService getClientDetailsService() {
	return clientDetailsService;
    }

    @Override
    protected String getSignatureKey() {
	return emConfiguration.getApiKeyPublicKey();
    }


    @Override
    protected String getApiName() {
	return emConfiguration.getAuthorizationApiName();
    }

	@Override
	protected Role getRoleByName(String name) {
    return Roles.getRoleByName(name);
	}
    
}
