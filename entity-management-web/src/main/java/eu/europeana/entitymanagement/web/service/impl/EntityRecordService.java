package eu.europeana.entitymanagement.web.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entitymanagement.config.I18nConstants;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.exception.EntityRetrievalException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;

@Service
public class EntityRecordService {
	
    public final String BASE_URI_DATA = "http://data.europeana.eu/";
    
    Logger logger = LogManager.getLogger(getClass());
    
	@Autowired
	private EntityRecordRepository entityRecordRepository;

    public EntityRecord retrieveEntityRecordByUri(String type, String namespace, String identifier) throws HttpException {

		StringBuilder stringBuilder = new StringBuilder();
	
		stringBuilder.append(BASE_URI_DATA);
		if (StringUtils.isNotEmpty(type))
		    stringBuilder.append(type.toLowerCase() + "/");
		if (StringUtils.isNotEmpty(namespace))
		    stringBuilder.append(namespace + "/");
		if (StringUtils.isNotEmpty(identifier))
		    stringBuilder.append(identifier);
	
		String entityUri = stringBuilder.toString();
		EntityRecord result;
		try {
		    result = entityRecordRepository.findByEntityId(entityUri);
		} catch (EntityRetrievalException e) {
		    throw new HttpException(e.getMessage(), I18nConstants.SERVER_ERROR_CANT_RETRIEVE_URI,
			    new String[] { entityUri }, HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		// if not found send appropriate error message
		if (result == null)
		    throw new HttpException(null, I18nConstants.RESOURCE_NOT_FOUND, new String[] { 
			    WebEntityConstants.ENTITY_API_RESOURCE, entityUri },
			    HttpStatus.NOT_FOUND, null);
	
		return result;
    }

    public void deleteEntityRecordByEntityId(String entityId) {
    	entityRecordRepository.deleteDataset(entityId);
    }
    
    public void saveEntityRecord (EntityRecord er) {
    	entityRecordRepository.save(er);
    }


}
