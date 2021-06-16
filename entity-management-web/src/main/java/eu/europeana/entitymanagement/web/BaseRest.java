package eu.europeana.entitymanagement.web;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.BuildInfo;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.serialization.EntityXmlSerializer;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.web.service.AuthorizationService;
import eu.europeana.entitymanagement.web.service.EntityObjectFactory;
import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;

public abstract class BaseRest extends BaseRestController {

    @Resource(name = AppConfig.BEAN_AUTHORIZATION_SERVICE)
    AuthorizationService emAuthorizationService;

    @Resource(name = AppConfig.BEAN_EM_BUILD_INFO)
    BuildInfo emBuildInfo;

    @Resource(name = AppConfigConstants.BEAN_EM_XML_SERIALIZER)
    EntityXmlSerializer entityXmlSerializer;

    @Resource(name = AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
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
     * Generates serialised EntityRecord Response entity along with Http status and
     * headers
     *
     * @param profile
     * @param outFormat
     * @param contentType
     * @param entityRecord
     * @param status
     * @return
     * @throws EntityCreationException
     * @throws FunctionalRuntimeException
     */
    public ResponseEntity<String> generateResponseEntity(String profile, FormatTypes outFormat, String languages,
            String contentType, EntityRecord entityRecord, HttpStatus status) throws EntityCreationException {

        Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();

        long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

        String etag = computeEtag(timestamp, outFormat.name(), getApiVersion());

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
        if (!outFormat.equals(FormatTypes.schema)) {
            headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
            headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
        }
        if (contentType != null && !contentType.isEmpty())
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        processLanguage(entityRecord.getEntity(), languages);

        String body = serialize(entityRecord, outFormat, profile);
        return ResponseEntity.status(status).headers(headers).eTag(etag).body(body);
    }

    /**
     * Generates a unique hex string based on the input params TODO: move logic to
     * {@link eu.europeana.api.commons.web.controller.BaseRestController#generateETag(Date, String, String)}
     */
    public String computeEtag(long timestamp, String format, String version) {
        return DigestUtils.md5Hex(String.format("%s:%s:%s", timestamp, format, version));
    }

    @SuppressWarnings("unchecked")
    private void processLanguage(Entity entity, String languages) {
        if (languages == null || languages.isEmpty())
            return;
        
        List<String> languagesList = Arrays.asList(languages.split(",", -1));
        List<Field> entityFields = EntityUtils.getAllFields(entity.getClass());
        Map<String, Object> currentFieldValue;
        String fieldName = null;
        try {
            for (Field field : entityFields) {

                fieldName = field.getName();
                // filter values only for multilingual fields
                if (!EntityFieldsTypes.isMultilingual(fieldName)) {
                    continue;
                }

                // ignore empty fields
                currentFieldValue = (Map<String, Object>) entity.getFieldValue(field);
                if (currentFieldValue == null) {
                    continue;
                }

                //filter entries by language
                Map<String, Object> newFieldValue = new HashMap<>();
                for (Map.Entry<String, Object> mapEntry : currentFieldValue.entrySet()) {
                    // allow also the URIs available for empty key
                    if (languagesList.contains(mapEntry.getKey()) || mapEntry.getKey().equals("")) {
                        newFieldValue.put(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
                if (currentFieldValue.size() != newFieldValue.size()) {
                    entity.setFieldValue(field, newFieldValue);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new FunctionalRuntimeException("An exception occured during setting the entity field: " + fieldName,
                    e);
        }
    }

}
