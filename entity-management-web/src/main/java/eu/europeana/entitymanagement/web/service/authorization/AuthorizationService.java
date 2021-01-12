package eu.europeana.entitymanagement.web.service.authorization;

import eu.europeana.entitymanagement.config.EntityManagementConfiguration;

public interface AuthorizationService extends eu.europeana.api.commons.service.authorization.AuthorizationService{

	EntityManagementConfiguration getConfiguration();
	
}
