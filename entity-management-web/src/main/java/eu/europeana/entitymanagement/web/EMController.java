package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PROFILE_SEPARATOR;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_QUERY;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.definitions.web.EntityIdResponse;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.exception.DatasourceNotKnownException;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.solr.SolrSearchCursorIterator;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import io.swagger.annotations.ApiOperation;

@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final SolrService solrService;
  private final DereferenceServiceLocator dereferenceServiceLocator;
  private final DataSources datasources;
  private final EntityUpdateService entityUpdateService;

  private static final String EXTERNAL_ID_REMOVED_MSG =
      "Entity id '%s' already exists as '%s', which has been removed";
  private static final String SAME_AS_NOT_EXISTS_MSG =
      "Url '%s' does not exist in entity owl:sameAs or skos:exactMatch";
  public static final String INVALID_UPDATE_REQUEST_MSG =
      "Request must either specify a 'query' param or contain entity identifiers in body";

  // profile used if none included in request
  public static final EntityProfile DEFAULT_REQUEST_PROFILE = EntityProfile.external;

  @Autowired
  public EMController(
      EntityRecordService entityRecordService,
      SolrService solrService,
      DereferenceServiceLocator dereferenceServiceLocator,
      DataSources datasources,
      EntityUpdateService entityUpdateService,
      EntityManagementConfiguration emConfig) {
    this.entityRecordService = entityRecordService;
    this.solrService = solrService;
    this.dereferenceServiceLocator = dereferenceServiceLocator;
    this.datasources = datasources;
    this.entityUpdateService = entityUpdateService;
  }

  @ApiOperation(
      value = "Disable an entity",
      nickname = "disableEntity",
      response = java.lang.Void.class)
  @RequestMapping(
      value = {"/{type}/{identifier}"},
      method = RequestMethod.DELETE,
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> disableEntity(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyWriteAccess(Operations.DELETE, request);

    EntityRecord entityRecord =
        entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), false);

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
    long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());
    checkIfMatchHeaderWithQuotes(etag, request);

    boolean isSynchronous = containsSyncProfile(profile);
    String entityId = entityRecord.getEntityId();
    logger.debug("Deprecating entityId={}, isSynchronous={}", entityId, isSynchronous);

    if (isSynchronous) {
      // delete from Solr before Mongo, so Solr errors won't leave DB in an inconsistent state
      entityRecordService.disableEntityRecord(entityRecord, true);
    } else {
      entityUpdateService.scheduleTasks(
          Collections.singletonList(entityId), ScheduledRemovalType.DEPRECATION);
    }

    return noContentResponse(request);
  }

  @ApiOperation(
      value = "Re-enable an entity",
      nickname = "enableEntity",
      response = java.lang.Void.class)
  @RequestMapping(
      value = {"/{type}/{identifier}"},
      method = RequestMethod.POST,
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> enableEntity(
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    List<EntityProfile> entityProfile = getEntityProfile(profile);

    verifyWriteAccess(Operations.UPDATE, request);

    EntityRecord entityRecord =
        entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), true);
    if (!entityRecord.isDisabled()) {
      return generateResponseEntity(
          request, entityProfile, FormatTypes.jsonld, null, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, entityRecord, HttpStatus.OK);
    }
    logger.debug("Re-enabling entityId={}", entityRecord.getEntityId());
    entityRecordService.enableEntityRecord(entityRecord);

    entityRecord = entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), false);
    return generateResponseEntity(
        request, entityProfile, FormatTypes.jsonld, null, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, entityRecord, HttpStatus.OK);
  }

  @ApiOperation(
      value = "Update an entity",
      nickname = "updateEntity",
      response = java.lang.Void.class)
  @RequestMapping(
      value = {"/{type}/{identifier}"},
      method = RequestMethod.PUT,
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> updateEntity(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestBody Entity updateRequestEntity,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    validateBodyEntity(updateRequestEntity);

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    // check that  type from update request matches existing entity's
    if (!entityRecord.getEntity().getType().equals(updateRequestEntity.getType())) {
      throw new HttpBadRequestException(
          String.format(
              "Request type %s does not match Entity type", updateRequestEntity.getType()));
    }

    verifyCoreferencesForUpdate(updateRequestEntity, entityRecord);

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
    long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());

    checkIfMatchHeaderWithQuotes(etag, request);

    entityRecordService.replaceEuropeanaProxy(updateRequestEntity, entityRecord);
    entityRecordService.update(entityRecord);

    // update the consolidated version in mongo and solr
    return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
  }

  private void verifyCoreferencesForUpdate(Entity updateRequestEntity, EntityRecord entityRecord)
      throws HttpBadRequestException {
    // Check if all the URIs present in the “sameAs” (of provided entity) dot not clash with another
    // Entity and if all Proxies are reflected on the “sameAs”, if not respond with HTTP 400;
    // check provided coreferences, consider static data sources
    if (updateRequestEntity.getSameReferenceLinks() == null
        || updateRequestEntity.getSameReferenceLinks().isEmpty()) {
      throw new HttpBadRequestException(
          "The mandatory coreferences (sameAs or exactMatch field) are missing in the request body");
    }

    Optional<EntityRecord> existingEntity =
        entityRecordService.findEntityDupplicationByCoreference(
            updateRequestEntity.getSameReferenceLinks(), entityRecord.getEntityId());
    if (existingEntity.isPresent()) {
      throw new HttpBadRequestException(
          "The entity coreferences (sameAs or exactMatch field) contains an entry indicating the provided input as being a dupplicate of : "
              + existingEntity.get().getEntityId());
    }

    //  //check if existing proxy ids are present in the same as
    List<String> proxyIds = entityRecord.getExternalProxyIds();
    // check the proxies which are not available in the sameAs
    proxyIds.removeAll(updateRequestEntity.getSameReferenceLinks());
    if (!proxyIds.isEmpty()) {
      throw new HttpBadRequestException(
          "The coreferences (sameAs or exactMatch field) of the request body does not contain the following proxies of the existing record:"
              + proxyIds);
    }
  }

  @ApiOperation(
      value = "Update an entity from external data source",
      nickname = "updateEntityFromDatasource",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/{type}/{identifier}/management/update",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> triggerSingleEntityFullUpdate(
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);
    // update from external data source is not available for static data sources
    datasources.verifyDataSource(entityRecord.getExternalProxies().get(0).getProxyId(), false);
    return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
  }

  @ApiOperation(
      value = "Update multiple entities from external data source",
      nickname = "updateMultipleEntityFromDatasource",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/management/update",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityIdResponse> triggerFullUpdateMultipleEntities(
      @RequestBody(required = false) List<String> entityIds,
      @RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    // query param takes precedence over request body
    if (StringUtils.isNotEmpty(query)) {
      return scheduleUpdatesWithSearch(request, query, ScheduledUpdateType.FULL_UPDATE);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    return scheduleBatchUpdates(request, entityIds, ScheduledUpdateType.FULL_UPDATE);
  }

  @ApiOperation(
      value = "Update metrics for given entities",
      nickname = "updateMetricsForEntities",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/management/metrics",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityIdResponse> triggerMetricsUpdateMultipleEntities(
      @RequestBody(required = false) List<String> entityIds,
      @RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    // query param takes precedence over request body
    if (StringUtils.isNotEmpty(query)) {
      return scheduleUpdatesWithSearch(request, query, ScheduledUpdateType.METRICS_UPDATE);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    return scheduleBatchUpdates(request, entityIds, ScheduledUpdateType.METRICS_UPDATE);
  }

  /**
   * Synchronize Organizations from Zoho
   *
   * @param type type of entity
   * @param identifier entity id
   * @param action used with either “enable” or “disable” to mark an entity to be used or not for
   *     enrichment using labels.
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(
      value = "Enable or disable the use of the entity for enrichment",
      nickname = "enableDisableForEnrich",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/{type}/{identifier}/management/enrich",
      produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
  public ResponseEntity<String> enableDisableForEnrich(
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = "action") String action,
      @RequestParam(
              value = WebEntityConstants.QUERY_PARAM_PROFILE,
              required = false,
              defaultValue = "internal")
          String profile,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);
    validateAction(action);

    EntityRecord entityRecord =
        entityRecordService.updateUsedForEnrichment(type, identifier, action);
    entityRecord = launchMetricsUpdateTask(entityRecord, true);
    return generateResponseEntity(
        request,
        getEntityProfile(profile),
        FormatTypes.jsonld,
        null,
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
        entityRecord,
        HttpStatus.OK);
  }

  private void validateAction(String action) throws HttpBadRequestException {
    if (!StringUtils.equalsAnyIgnoreCase(
        action, WebEntityConstants.ACTION_ENABLE, WebEntityConstants.ACTION_DISABLE)) {
      throw new HttpBadRequestException(
          "Invalid value for param action: "
              + action
              + ". Supported values are ["
              + WebEntityConstants.ACTION_ENABLE
              + ", "
              + WebEntityConstants.ACTION_DISABLE
              + "]");
    }
  }

  @ApiOperation(
      value = "Retrieve a known entity",
      nickname = "getEntityJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {
        "/{type}/base/{identifier}.jsonld",
        "/{type}/base/{identifier}.json",
        "/{type}/{identifier}.jsonld",
        "/{type}/{identifier}.json",
        "/{type}/base/{identifier}",
        "/{type}/{identifier}"
      },
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getJsonLdEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE, required = false)
          String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);
    
    return createResponseRetrieve (type, identifier, profile, request,  FormatTypes.jsonld, languages, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);    
  }

  @ApiOperation(
      value = "Retrieve a known entity",
      nickname = "getEntityXml",
      response = java.lang.Void.class)
  @RequestMapping(
      value = {
        "/{type}/base/{identifier}.xml",
        "/{type}/{identifier}.xml",
        "/{type}/{identifier}",
        "/{type}/{identifier}"
      },
      method = RequestMethod.GET,
      produces = {
        MediaType.APPLICATION_XML_VALUE,
        HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML,
        HttpHeaders.CONTENT_TYPE_RDF_XML
      })
  public ResponseEntity<String> getXmlEntity(
      @RequestHeader(value = HttpHeaders.ACCEPT) String acceptHeader,
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE, required = false)
          String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);
    
    return createResponseRetrieve (type, identifier, profile, request,  FormatTypes.xml, languages, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);    
  }

  @ApiOperation(
      value = "Retrieve a known entity",
      nickname = "getEntitySchemaJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {
        "/{type}/base/{identifier}.schema.jsonld",
        "/{type}/base/{identifier}.schema.json",
        "/{type}/{identifier}.schema.jsonld",
        "/{type}/{identifier}.schema.json"
      },
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getEntitySchemaJsonLd(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE, required = false)
          String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);
    
    return createResponseRetrieve (type, identifier, profile, request,  FormatTypes.schema, languages, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    
  }

  @ApiOperation(
      value = "Register a new entity",
      nickname = "registerEntity",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/",
      produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
  public ResponseEntity<String> registerEntity(
      @RequestBody Entity europeanaProxyEntity, HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.CREATE, request);

    validateBodyEntity(europeanaProxyEntity);

    String creationRequestId = europeanaProxyEntity.getEntityId();

    if (StringUtils.isNotEmpty(creationRequestId)) {
      logger.debug("Registering new entity: externalId={}", creationRequestId);
    } else {
      // external id is mandatory in request body
      throw new HttpBadRequestException("Mandatory field missing in the request body: id");
    }

    // isAgregatedBy must not be set by the user, if provided the value is discarded
    if (europeanaProxyEntity.getIsAggregatedBy() != null) {
      europeanaProxyEntity.setIsAggregatedBy(null);
    }

    // check if id is already being used, if so return a 301
    List<String> corefs;
    if (europeanaProxyEntity.getSameReferenceLinks() == null) {
      corefs = Collections.singletonList(creationRequestId);
    } else {
      corefs = new ArrayList<>(europeanaProxyEntity.getSameReferenceLinks());
      corefs.add(creationRequestId);
    }

    Optional<EntityRecord> existingEntity =
        entityRecordService.findEntityDupplicationByCoreference(corefs, null);
    ResponseEntity<String> response = checkExistingEntity(existingEntity, creationRequestId);

    if (response != null) {
      return response;
    }

    DataSource dataSource = datasources.verifyDataSource(creationRequestId, false);

    // in case of Organization it must be the zoho Organization
    String creationRequestType = europeanaProxyEntity.getType();
    if (EntityTypes.Organization.getEntityType().equals(creationRequestType)
        && !creationRequestId.contains(DataSource.ZOHO_HOST)) {
      throw new HttpBadRequestException(
          String.format(
              "The Organization entity should come from Zoho and have the corresponding id format containing: %s",
              DataSource.ZOHO_HOST));
    }

    Entity datasourceResponse = dereferenceEntity(creationRequestId, creationRequestType);

    EntityRecord savedEntityRecord =
        entityRecordService.createEntityFromRequest(
            europeanaProxyEntity, datasourceResponse, dataSource);
    logger.debug(
        "Created Entity record for externalId={}; entityId={}",
        creationRequestId,
        savedEntityRecord.getEntityId());

    return launchTaskAndRetrieveEntity(
        request,
        savedEntityRecord.getEntity().getType(),
        getDatabaseIdentifier(savedEntityRecord.getEntityId()),
        savedEntityRecord,
        EntityProfile.internal.toString());
  }

  Entity dereferenceEntity(String creationRequestId, String creationRequestType)
      throws Exception, DatasourceNotKnownException, EntityMismatchException {

    Dereferencer dereferenceService =
        dereferenceServiceLocator.getDereferencer(creationRequestId, creationRequestType);

    Optional<Entity> datasourceResponseOptional =
        dereferenceService.dereferenceEntityById(creationRequestId);

    if (datasourceResponseOptional.isEmpty()) {
      throw new DatasourceNotKnownException(
          "Unsuccessful dereferenciation for externalId=" + creationRequestId);
    }

    Entity datasourceResponse = datasourceResponseOptional.get();

    if (!datasourceResponse.getType().equals(creationRequestType)) {
      throw new EntityMismatchException(
          String.format(
              "Datasource type '%s' does not match type '%s' in request",
              datasourceResponse.getType(), creationRequestType));
    }
    return datasourceResponse;
  }

  @ApiOperation(value = "Change provenance for an Entity", nickname = "changeProvenance")
  @PutMapping(
      value = "/{type}/{identifier}/management/source",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> changeProvenance(
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @RequestParam(value = WebEntityConstants.PATH_PARAM_URL) String url,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    if (!entityRecord.getEntity().getSameReferenceLinks().contains(url)) {
      throw new HttpBadRequestException(String.format(SAME_AS_NOT_EXISTS_MSG, url));
    }

    entityRecordService.changeExternalProxy(entityRecord, url);
    entityRecordService.update(entityRecord);
    return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
  }

  @ApiOperation(value = "Retrieve multiple entities", nickname = "retrieveEntities")
  @PostMapping(
      value = "/retrieve",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> retrieveEntities(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestBody List<String> urls,
      HttpServletRequest request)
      throws Exception {

    verifyReadAccess(request);

    return createResponseMultipleEntities(urls, request);
  }

  private ResponseEntity<String> createResponseRetrieve (String type, String identifier, String profile, HttpServletRequest request,  FormatTypes outFormat, String languages, String contentType) throws EuropeanaApiException {
    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), true);
    if (entityRecord.isDisabled()) {
      return redirectDeprecated(entityRecord, type, identifier.toLowerCase());
    }

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    // if request doesn't specify a valid EntityProfile, use external by default
    return generateResponseEntity(request, entityProfile, outFormat, languages, contentType, entityRecord, HttpStatus.OK);
    
  }
  private ResponseEntity<String> createResponseMultipleEntities(
      List<String> entityIds, HttpServletRequest request) throws EuropeanaApiException {
    List<EntityRecord> entityRecords = entityRecordService.retrieveMultipleByEntityIds(entityIds);
    if (entityRecords.isEmpty()) {
      throw new EntityNotFoundException(entityIds.toString());
    }

    // LinkedHashMap iterates keys() and values() in order of insertion. Using a map
    // improves sort performance significantly
    Map<String, EntityRecord> sortedEntityRecordMap = new LinkedHashMap<>(entityIds.size());
    for (String id : entityIds) {
      sortedEntityRecordMap.put(id, null);
    }

    for (EntityRecord entityRecord : entityRecords) {
      sortedEntityRecordMap.replace(entityRecord.getEntityId(), entityRecord);
    }

    // create response headers
    String contentType = HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
    org.springframework.http.HttpHeaders headers = createAllowHeader(request);
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);

    // remove null values in response
    List<EntityRecord> responseBody =
        sortedEntityRecordMap.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    String body = serialize(responseBody);
    return ResponseEntity.status(HttpStatus.OK).headers(headers).body(body);
  }

  /**
   * gets the first valid profile from profile param
   *
   * @param profileParamString profile request query param
   * @return valid EntityProfile in request
   */
  private List<EntityProfile> getEntityProfile(String profileParamString)
      throws HttpBadRequestException {

    if (!StringUtils.isNotEmpty(profileParamString)) {
      return List.of(DEFAULT_REQUEST_PROFILE);
    }

    List<String> requestProfiles =
        new ArrayList<>(List.of((profileParamString.split(QUERY_PARAM_PROFILE_SEPARATOR))));

    List<String> validProfiles =
        Arrays.stream(EntityProfile.values()).map(Enum::name).collect(Collectors.toList());

    requestProfiles.retainAll(validProfiles);

    // profile string contains an invalid value, or is internal,external (which can't be specified
    // together)
    if (requestProfiles.isEmpty()
        || (requestProfiles.size() > 1 && !requestProfiles.contains(EntityProfile.debug.name()))) {
      throw new HttpBadRequestException("Invalid profile query param " + profileParamString);
    }
    return requestProfiles.stream().map(EntityProfile::valueOf).collect(Collectors.toList());
  }

  private EntityRecord launchMetricsUpdateTask(EntityRecord entityRecord, boolean includeDisabled)
      throws Exception {
    // launch synchronous metrics update, then retrieve entity from DB afterwards
    entityUpdateService.runSynchronousMetricsUpdate(entityRecord.getEntityId());
    return entityRecordService.retrieveEntityRecord(entityRecord.getEntityId(), includeDisabled);
  }

  private ResponseEntity<String> launchTaskAndRetrieveEntity(
      HttpServletRequest request,
      String type,
      String identifier,
      EntityRecord entityRecord,
      String profile)
      throws Exception {
    // launch synchronous update, then retrieve entity from DB afterwards
    launchUpdateTask(entityRecord.getEntityId());
    entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    return generateResponseEntity(
        request,
        getEntityProfile(profile),
        FormatTypes.jsonld,
        null,
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
        entityRecord,
        HttpStatus.ACCEPTED);
  }

  private ResponseEntity<String> checkExistingEntity(
      Optional<EntityRecord> existingEntity, String entityCreationId)
      throws EntityRemovedException {

    if (existingEntity.isPresent()) {
      if (existingEntity.get().isDisabled()) {
        throw new EntityRemovedException(
            String.format(
                EXTERNAL_ID_REMOVED_MSG, entityCreationId, existingEntity.get().getEntityId()));
      }

      // return 301 redirect
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
          .location(
              UriComponentsBuilder.newInstance()
                  .path("/entity/{id}")
                  .buildAndExpand(
                      EntityRecordUtils.extractIdentifierFromEntityId(
                          existingEntity.get().getEntityId()))
                  .toUri())
          .build();
    }
    return null;
  }
  
  private ResponseEntity<String> redirectDeprecated(EntityRecord deprecatedEntity, String type, String identifier) throws EntityRemovedException {
    //check if there is an entity in the sameAs ref links starting with "http://data.europeana.eu/" and so, redirect to it 
    List<String> sameAsRefLinks = deprecatedEntity.getEntity().getSameReferenceLinks();
    if (sameAsRefLinks!=null) {
      Optional<String> sameAsLink = sameAsRefLinks.stream().filter(el -> el.contains(WebEntityFields.BASE_DATA_EUROPEANA_URI)).findFirst();      
      if (sameAsLink.isPresent()) {
        // return 301 redirect
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .location(URI.create(sameAsLink.get()))
            .build();
      }
    }
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    throw new EntityRemovedException(String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, entityUri));  
  }  

  private ResponseEntity<EntityIdResponse> scheduleBatchUpdates(
      HttpServletRequest request, List<String> entityIds, ScheduledTaskType updateType) {
    // get the entities to be scheduled, failed and skipped for update
    EntityIdResponse entityIdResponse = new EntityIdResponse();
    List<String> entityIdsToSchedule = updateEntityIdResponse(entityIdResponse, entityIds);
    entityUpdateService.scheduleTasks(entityIdsToSchedule, updateType);

    // set required headers for this endpoint
    org.springframework.http.HttpHeaders httpHeaders = createAllowHeader(request);
    httpHeaders.add(CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    return ResponseEntity.accepted().headers(httpHeaders).body(entityIdResponse);
  }

  /**
   * Schedules entity updates using a search query
   *
   * @param query search query
   * @param updateType update type to schedule
   * @throws SolrServiceException if error occurs during search
   */
  ResponseEntity<EntityIdResponse> scheduleUpdatesWithSearch(
      HttpServletRequest request, String query, ScheduledTaskType updateType)
      throws SolrServiceException {
    SolrSearchCursorIterator iterator =
        solrService.getSearchIterator(query, List.of(EntitySolrFields.TYPE, EntitySolrFields.ID));

    EntityIdResponse entityIdResponse = new EntityIdResponse();

    while (iterator.hasNext()) {
      List<SolrEntity<Entity>> solrEntities = iterator.next();

      List<String> entityIds =
          solrEntities.stream().map(SolrEntity::getEntityId).collect(Collectors.toList());

      // get the entities to be scheduled, failed and skipped for update
      List<String> entityIdsToSchedule = updateEntityIdResponse(entityIdResponse, entityIds);

      entityUpdateService.scheduleTasks(entityIdsToSchedule, updateType);
    }

    // set required headers for this endpoint
    org.springframework.http.HttpHeaders httpHeaders = createAllowHeader(request);
    httpHeaders.add(CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    return ResponseEntity.accepted().headers(httpHeaders).body(entityIdResponse);
  }

  /**
   * Generate the EntityIdResponse based on entity Ids to be processed for update
   *
   * @param entityIds
   * @return
   */
  private List<String> updateEntityIdResponse(
      EntityIdResponse entityIdResponse, List<String> entityIds) {
    // Get all existing EntityIds and their disabled status
    List<EntityIdDisabledStatus> statusList =
        entityRecordService.retrieveMultipleByEntityId(entityIds, false);

    // extract only the entityIds for easy comparison
    List<String> existingEntityIds =
        statusList.stream().map(EntityIdDisabledStatus::getEntityId).collect(Collectors.toList());

    // failures are entityIds that weren't retrieved
    List<String> failures =
        entityIds.stream().filter(e -> !existingEntityIds.contains(e)).collect(Collectors.toList());

    Map<Boolean, List<EntityIdDisabledStatus>> entityIdsByDisabled =
        statusList.stream().collect(groupingBy(EntityIdDisabledStatus::isDisabled));

    // get entityIds that can be scheduled (they are not disabled)
    List<EntityIdDisabledStatus> nonDisabledEntities = entityIdsByDisabled.get(false);
    List<String> toBeScheduled =
        CollectionUtils.isEmpty(nonDisabledEntities)
            ? Collections.emptyList()
            : nonDisabledEntities.stream()
                .map(EntityIdDisabledStatus::getEntityId)
                .collect(Collectors.toList());

    // updates skipped if EntityIdDisabledStatus.disabled=true
    List<EntityIdDisabledStatus> disabledEntities = entityIdsByDisabled.get(true);
    List<String> skipped =
        CollectionUtils.isEmpty(disabledEntities)
            ? Collections.emptyList()
            : disabledEntities.stream()
                .map(EntityIdDisabledStatus::getEntityId)
                .collect(Collectors.toList());

    entityIdResponse.updateValues(
        entityIds.size(), toBeScheduled, failures, skipped, emConfig.getEntityIdResponseMaxSize());
    return toBeScheduled;
  }

  private void launchUpdateTask(String entityId) throws Exception {
    entityUpdateService.runSynchronousUpdate(entityId);
  }

  /** Gets the database identifier from an EntityId string */
  private String getDatabaseIdentifier(String entityId) {
    // entity id is "http://data.europeana.eu/{type}/{identifier}"
    return entityId.substring(entityId.lastIndexOf("/") + 1);
  }
}
