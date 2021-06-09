package eu.europeana.entitymanagement.web;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.BuildInfo;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.serialization.EntityXmlSerializer;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.web.service.AuthorizationService;
import eu.europeana.entitymanagement.web.service.EntityObjectFactory;
import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;


public abstract class BaseRest extends BaseRestController {

    @Resource(name=AppConfig.BEAN_AUTHORIZATION_SERVICE)
    AuthorizationService emAuthorizationService;

    @Resource(name=AppConfig.BEAN_EM_BUILD_INFO)
    BuildInfo emBuildInfo;
    
    @Resource(name=AppConfigConstants.BEAN_EM_XML_SERIALIZER)
    EntityXmlSerializer entityXmlSerializer;
    
    @Resource(name=AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
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
    protected String serialize(EntityRecord entityRecord, FormatTypes format, String profile)
        throws EntityManagementRuntimeException, EntityCreationException {

	String responseBody = null;

	if (FormatTypes.jsonld.equals(format)) {
	    responseBody = jsonLdSerializer.serialize(entityRecord, profile);
	} else if (FormatTypes.xml.equals(format)) {
	  XmlBaseEntityImpl<?> xmlEntity = EntityObjectFactory.createXmlEntity(entityRecord.getEntity());

	    responseBody = entityXmlSerializer.serializeXmlExternal(new RdfBaseWrapper(xmlEntity));
	}
	return responseBody;
    }

    /**
     * Generates serialised EntityRecord Response entity along with
     * Http status and headers
     *
     * @param profile
     * @param outFormat
     * @param contentType
     * @param entityRecord
     * @param status
     * @return
     * @throws EntityCreationException
     */
    public ResponseEntity<String> generateResponseEntity(String profile, FormatTypes outFormat, EntityRecord entityRecord, HttpStatus status, String allowHeaderParams)
            throws EntityCreationException {

        Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();

        long timestamp = isAggregatedBy != null ?
                isAggregatedBy.getModified().getTime() :
                0L;

        String etag = computeEtag(timestamp, outFormat.name(), getApiVersion(), profile);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(HttpHeaders.ALLOW, allowHeaderParams);
        if (!outFormat.equals(FormatTypes.schema)) {
            headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
            headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
        }
        
        if(outFormat.equals(FormatTypes.jsonld)) headers.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
        else if(outFormat.equals(FormatTypes.xml)) headers.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
        
        String body = serialize(entityRecord, outFormat, profile);
        return ResponseEntity.status(status).headers(headers).eTag(etag).body(body);
    }


    /**
     * Generates a unique hex string based on the input params
     * TODO: move logic to {@link eu.europeana.api.commons.web.controller.BaseRestController#generateETag(Date, String, String)}
     */
    public String computeEtag(long timestamp, String format, String version, String profile){
        return DigestUtils.md5Hex(String.format("%s:%s:%s:%s", timestamp, format, version, profile));
    }

	/**
	 * This method saves the @RequestMapping "method" and "value" attributes
	 */
	protected void getMethodRESTTypes(Map<String,Set<String>> methodsRESTTypes, Class<? extends BaseRest> objectClass) {
		Method[] emControlMethods = objectClass.getMethods();
		for(Method method : emControlMethods) {
			RequestMapping methodDeclaredAnnos = method.getDeclaredAnnotation(RequestMapping.class);
			if(methodDeclaredAnnos!=null) {
				//the split part removes all characters after "."
				String methodPath = methodDeclaredAnnos.value()[0].split("\\.", 2)[0];
				if(!methodPath.contains(".")) {
					if(methodsRESTTypes.containsKey(methodPath)) {
						methodsRESTTypes.get(methodPath).add(methodDeclaredAnnos.method()[0].name());
					}
					else {
						Set<String> methodRESTTypesValue = new HashSet<String>();
						methodRESTTypesValue.add(methodDeclaredAnnos.method()[0].name());
						methodsRESTTypes.put(methodPath, methodRESTTypesValue);
					}
				}
				
			}
		}
	}
}
