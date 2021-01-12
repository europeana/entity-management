package eu.europeana.entitymanagement.web.service.authorization;

import eu.europeana.entitymanagement.config.EntityConfiguration;

public interface AuthorizationService extends eu.europeana.api.commons.service.authorization.AuthorizationService{

	EntityConfiguration getConfiguration();
	
}
