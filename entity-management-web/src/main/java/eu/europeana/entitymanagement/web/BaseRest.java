package eu.europeana.entitymanagement.web;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.entitymanagement.common.config.BuildInfo;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.serialization.EntityXmlSerializer;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.web.service.authorization.AuthorizationService;
import eu.europeana.entitymanagement.web.service.authorization.AuthorizationServiceImpl;


public abstract class BaseRest extends BaseRestController {

    @Resource(name="emAuthorizationService")
    AuthorizationServiceImpl emAuthorizationService;

    @Resource(name="emBuildInfo")
    BuildInfo emBuildInfo;
    
    @Autowired
    EntityXmlSerializer entityXmlSerializer;
    
    @Autowired
    JsonLdSerializer jsonLdSerializer;

    Logger logger = LogManager.getLogger(getClass());

    public BaseRest() {
    	super();
    }

    public AuthorizationService getAuthorizationService() {
	return emAuthorizationService;
    }

    public Logger getLogger() {
	return logger;
    }

    /**
     * @return
     */
//    protected String getDefaultUserToken() {
//	return getAuthorizationService().getConfiguration().getUserToken();
//    }

    public String getApiVersion() {
	return emBuildInfo.getAppVersion();
    }

    /**
     * This method selects serialization method according to provided format.
     * 
     * @param entity The entity
     * @param format The format extension
     * @return entity in jsonLd format
     * @throws EntityManagementRuntimeException 
     */
    protected String serialize(EntityRecord entityRecord, FormatTypes format, String profile) throws EntityManagementRuntimeException {

	String responseBody = null;

	if (FormatTypes.jsonld.equals(format)) {
	    responseBody = jsonLdSerializer.serialize(entityRecord, profile);
	} else if (FormatTypes.schema.equals(format)) {
//	    responseBody = (new EntitySchemaOrgSerializer()).serializeEntity(entityRecord.getEntity());
	} else if (FormatTypes.xml.equals(format)) {
	    responseBody = entityXmlSerializer.serializeXml(entityRecord, profile);
	}
	return responseBody;
    }

}
