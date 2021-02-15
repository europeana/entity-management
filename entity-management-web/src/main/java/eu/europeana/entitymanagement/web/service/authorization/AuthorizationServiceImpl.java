package eu.europeana.entitymanagement.web.service.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.EntityManagementConfigurationImpl;

@Component
public class AuthorizationServiceImpl extends BaseAuthorizationService implements AuthorizationService {

    protected final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    EntityManagementConfigurationImpl configuration;

    @Autowired
    EuropeanaClientDetailsService clientDetailsService;

    public AuthorizationServiceImpl() {

    }

    public EntityManagementConfiguration getConfiguration() {
	return configuration;
    }

    public void setConfiguration(EntityManagementConfigurationImpl configuration) {
	this.configuration = configuration;
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
