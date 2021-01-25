package eu.europeana.entitymanagement.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.formats.FormatTypes;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.utils.jsonld.EuropeanaEntityLd;
import eu.europeana.entitymanagement.web.jsonld.EntitySchemaOrgSerializer;
import eu.europeana.entitymanagement.web.service.authorization.AuthorizationService;
import eu.europeana.entitymanagement.web.service.authorization.AuthorizationServiceImpl;
import eu.europeana.entitymanagement.web.xml.EntityXmlSerializer;

public abstract class BaseRest extends BaseRestController {

    @Autowired
    AuthorizationServiceImpl authorizationService;

    @Autowired
    EntityXmlSerializer entityXmlSerializer;

    Logger logger = LogManager.getLogger(getClass());

    public BaseRest() {
    	super();
    }

    public AuthorizationService getAuthorizationService() {
	return authorizationService;
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
	return getAuthorizationService().getConfiguration().getApiVersion();
    }

    /**
     * This method selects serialization method according to provided format.
     * 
     * @param entity The entity
     * @param format The format extension
     * @return entity in jsonLd format
     * @throws UnsupportedEntityTypeException
     * @throws HttpException
     */
    protected String serialize(EntityRecord entityRecord, FormatTypes format) throws UnsupportedEntityTypeException, HttpException {

	String responseBody = null;

	if (FormatTypes.jsonld.equals(format)) {
	    EuropeanaEntityLd entityLd = new EuropeanaEntityLd(entityRecord.getEntity());
	    return entityLd.toString(4);
	} else if (FormatTypes.schema.equals(format)) {
	    responseBody = (new EntitySchemaOrgSerializer()).serializeEntity(entityRecord.getEntity());
	} else if (FormatTypes.xml.equals(format)) {
	    responseBody = entityXmlSerializer.serializeXml(entityRecord.getEntity());
	}
	return responseBody;
    }

}
