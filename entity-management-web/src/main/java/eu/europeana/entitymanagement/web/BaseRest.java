package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.controller.BaseRestController;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.common.config.BuildInfo;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.exceptions.EntityManagementRuntimeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EtagMismatchException;
import eu.europeana.entitymanagement.schemaorg.model.SchemaOrgEntity;
import eu.europeana.entitymanagement.serialization.EntityXmlSerializer;
import eu.europeana.entitymanagement.serialization.JsonLdSerializer;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.web.service.AuthorizationService;
import eu.europeana.entitymanagement.web.service.RequestPathMethodService;
import eu.europeana.entitymanagement.web.xml.model.RdfBaseWrapper;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

public abstract class BaseRest extends BaseRestController {

  @Autowired private AuthorizationService emAuthorizationService;

  @Autowired private BuildInfo emBuildInfo;

  @Autowired private EntityXmlSerializer entityXmlSerializer;

  @Autowired private JsonLdSerializer jsonLdSerializer;

  @Autowired private eu.europeana.corelib.edm.utils.JsonLdSerializer corelibJsonLdSerializer;

  @Autowired private RequestPathMethodService requestMethodService;

  @Autowired protected FailedTaskService failedTaskService;

  protected Logger logger = LogManager.getLogger(getClass());

  public BaseRest() {
    super();
  }

  public AuthorizationService getAuthorizationService() {
    return emAuthorizationService;
  }

  public String getApiVersion() {
    return emBuildInfo.getAppVersion();
  }

  /**
   * This method selects serialization method according to provided format.
   *
   * @param entityRecord The entity
   * @param format The format extension
   * @param profiles
   * @return entity in jsonLd format
   * @throws EntityManagementRuntimeException
   */
  protected String serialize(
      EntityRecord entityRecord, FormatTypes format, List<EntityProfile> profiles)
      throws EuropeanaApiException {

    String responseBody = null;
    try {
      if (FormatTypes.jsonld.equals(format)) {
        boolean includeFailure = profiles.contains(EntityProfile.debug);
        Optional<FailedTask> failure =
            includeFailure
                ? failedTaskService.getFailure(entityRecord.getEntityId())
                : Optional.empty();

        responseBody = jsonLdSerializer.serialize(entityRecord, profiles, includeFailure, failure);
      } else if (FormatTypes.xml.equals(format)) {
        XmlBaseEntityImpl<?> xmlEntity =
            EntityObjectFactory.createXmlEntity(entityRecord.getEntity());
        responseBody = entityXmlSerializer.serializeXmlExternal(new RdfBaseWrapper(xmlEntity));
      } else if (FormatTypes.schema.equals(format)) {
        SchemaOrgEntity<?> schemaOrgEntity =
            EntityObjectFactory.createSchemaOrgEntity(entityRecord.getEntity());

        responseBody = corelibJsonLdSerializer.serialize(schemaOrgEntity.get());
      }
    } catch (IOException e) {
      throw new EuropeanaApiException("Error serializing entity", e);
    }
    return responseBody;
  }

  protected ResponseEntity<String> generateResponseFailedUpdates(
      HttpServletRequest request, List<String> entityIds) throws EuropeanaApiException {

    org.springframework.http.HttpHeaders headers = createAllowHeader(request);
    // Access-Control-Expose-Headers only set for CORS requests
    if (StringUtils.hasLength(request.getHeader(org.springframework.http.HttpHeaders.ORIGIN))) {
      headers.setAccessControlExposeHeaders(List.of(HttpHeaders.ETAG, HttpHeaders.VARY));
    }

    headers.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);

    StringBuffer requestUrl = request.getRequestURL();

    /*
     * RequestURL always ends with "/entity/management/failed" and doesn't contain query params.
     * App could be running on localhost:8080 or behind a proxy, so we use the requestUrl as-is
     */
    String entityUriPrefix = requestUrl.subSequence(0, requestUrl.length() - 17).toString();
    // convert entityId to navigable URL
    List<String> pathUrls =
        entityIds.stream()
            .map(
                id ->
                    entityUriPrefix + EntityRecordUtils.getEntityRequestPath(id) + "?profile=debug")
            .collect(Collectors.toList());
    try {

      String body = jsonLdSerializer.serializeFailedUpdates(pathUrls);
      headers.setContentLength(body.getBytes().length);
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(body);
    } catch (IOException e) {
      throw new EuropeanaApiException("Error serializing failed tasks", e);
    }
  }

  /**
   * Generates serialised EntityRecord Response entity along with Http status and headers
   *
   * @param request
   * @param profiles
   * @param outFormat
   * @param contentType
   * @param entityRecord
   * @param status
   * @return
   * @throws EuropeanaApiException
   */
  protected ResponseEntity<String> generateResponseEntity(
      HttpServletRequest request,
      List<EntityProfile> profiles,
      FormatTypes outFormat,
      String languages,
      String contentType,
      EntityRecord entityRecord,
      HttpStatus status)
      throws EuropeanaApiException {

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();

    long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

    String etag = computeEtag(timestamp, outFormat.name(), getApiVersion());

    org.springframework.http.HttpHeaders headers = createAllowHeader(request);

    if (!outFormat.equals(FormatTypes.schema)) {
      headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
      headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
    }

    // Access-Control-Expose-Headers only set for CORS requests
    if (StringUtils.hasLength(request.getHeader(org.springframework.http.HttpHeaders.ORIGIN))) {
      if (outFormat.equals(FormatTypes.schema)) {
        headers.setAccessControlExposeHeaders(List.of(HttpHeaders.ETAG, HttpHeaders.VARY));
      } else {
        headers.setAccessControlExposeHeaders(
            List.of(HttpHeaders.ETAG, HttpHeaders.LINK, HttpHeaders.VARY));
      }
    }

    if (contentType != null && !contentType.isEmpty()) {
      headers.add(HttpHeaders.CONTENT_TYPE, contentType);
    }

    processLanguage(entityRecord.getEntity(), languages);

    String body = serialize(entityRecord, outFormat, profiles);
    return ResponseEntity.status(status).headers(headers).eTag(etag).body(body);
  }

  protected org.springframework.http.HttpHeaders createAllowHeader(HttpServletRequest request) {
    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    String allowHeaderValue;

    Optional<String> methodsForRequestPattern =
        requestMethodService.getMethodsForRequestPattern(request);
    if (methodsForRequestPattern.isEmpty()) {
      logger.warn(
          "Could not find other matching methods for {}. Using current request method in Allow header",
          request.getRequestURL());
      allowHeaderValue = request.getMethod();
    } else {
      allowHeaderValue = methodsForRequestPattern.get();
    }

    headers.add(HttpHeaders.ALLOW, allowHeaderValue);
    return headers;
  }

  protected ResponseEntity<String> noContentResponse(HttpServletRequest request) {
    return ResponseEntity.noContent().headers(createAllowHeader(request)).build();
  }

  /**
   * Generates a unique hex string based on the input params TODO: move logic to {@link
   * eu.europeana.api.commons.web.controller.BaseRestController#generateETag(Date, String, String)}
   */
  public String computeEtag(long timestamp, String format, String version) {
    return DigestUtils.md5Hex(String.format("%s:%s:%s", timestamp, format, version));
  }

  @SuppressWarnings("unchecked")
  private void processLanguage(Entity entity, String languages) throws EuropeanaApiException {
    if (languages == null || languages.isEmpty()) return;

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

        // filter entries by language
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
      throw new EuropeanaApiException(
          "An exception occurred during setting the entity field: " + fieldName, e);
    }
  }

  /**
   * Reimplementation of {@link BaseRestController#checkIfMatchHeader(String, HttpServletRequest)}
   * that expects the ETAG value in If-Match headers to be placed between double quotes.
   *
   * <p>TODO: move to api-commons
   */
  protected void checkIfMatchHeaderWithQuotes(String etag, HttpServletRequest request)
      throws EtagMismatchException {
    String ifMatchHeader = request.getHeader("If-Match");
    // remove double-quotes from header during comparison
    if (ifMatchHeader != null && !etag.equals(ifMatchHeader.replace("\"", ""))) {
      throw new EtagMismatchException(
          "If-Match header value does not match generated ETag for entity");
    }
  }
}
