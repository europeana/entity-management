package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PROFILE_SEPARATOR;
import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_QUERY;
import static java.util.stream.Collectors.groupingBy;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
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
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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

@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final SolrService solrService;
  private final DereferenceServiceLocator dereferenceServiceLocator;
  private final DataSources datasources;
  private final EntityUpdateService entityUpdateService;
  private final EntityManagementConfiguration emConfig;

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
    this.emConfig = emConfig;
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
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.DELETE, request);
    }
    EntityRecord entityRecord =
        entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), false);

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
    long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());
    checkIfMatchHeaderWithQuotes(etag, request);

    entityUpdateService.scheduleTasks(
        Collections.singletonList(entityRecord.getEntityId()), ScheduledRemovalType.DEPRECATION);

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
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }
    EntityRecord entityRecord =
        entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), true);
    if (!entityRecord.isDisabled()) {

      return createResponse(
          request,
          entityProfile,
          type,
          null,
          identifier,
          FormatTypes.jsonld,
          HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    }
    logger.info("Re-enabling entityId={}", entityRecord.getEntityId());
    entityRecordService.enableEntityRecord(entityRecord);
    // entity needs to be added back to the solr index
    solrService.storeEntity(createSolrEntity(entityRecord));

    return createResponse(
        request,
        entityProfile,
        type,
        null,
        identifier,
        FormatTypes.jsonld,
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
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
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }
    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    // TODO: Re-enable authentication
    // verifyReadAccess(request);

    // check that  type from update request matches existing entity's
    if (!entityRecord.getEntity().getType().equals(updateRequestEntity.getType())) {
      throw new HttpBadRequestException(
          String.format(
              "Request type %s does not match Entity type", updateRequestEntity.getType()));
    }

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
    long timestamp = isAggregatedBy != null ? isAggregatedBy.getModified().getTime() : 0L;

    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());

    checkIfMatchHeaderWithQuotes(etag, request);

    entityRecordService.replaceEuropeanaProxy(updateRequestEntity, entityRecord);
    entityRecordService.update(entityRecord);

    // update the consolidated version in mongo and solr
    return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
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
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }
    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);
    // update from external data source is not available for static data sources
    entityRecordService.verifyDataSource(
        entityRecord.getExternalProxies().get(0).getProxyId(), false);
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
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }

    // query param takes precedence over request body
    if (StringUtils.hasLength(query)) {
      entityUpdateService.scheduleUpdatesWithSearch(query, ScheduledUpdateType.FULL_UPDATE);
      return returnEmptyAcceptedResponse(request);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    // TODO: consider removing entities from static data sources
    return scheduleBatchUpdates(request, entityIds, ScheduledUpdateType.FULL_UPDATE);
  }

  @ApiOperation(
      value = "Update metrics for given entities",
      nickname = "updateMultipleEntityFromDatasource",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/management/metrics",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityIdResponse> triggerMetricsUpdateMultipleEntities(
      @RequestBody(required = false) List<String> entityIds,
      @RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
      HttpServletRequest request)
      throws Exception {
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }

    // query param takes precedence over request body
    if (StringUtils.hasLength(query)) {
      entityUpdateService.scheduleUpdatesWithSearch(query, ScheduledUpdateType.METRICS_UPDATE);
      return returnEmptyAcceptedResponse(request);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    return scheduleBatchUpdates(request, entityIds, ScheduledUpdateType.METRICS_UPDATE);
  }

  @ApiOperation(
      value = "Retrieve a known entity",
      nickname = "getEntityJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {
        "/{type}/base/{identifier}.jsonld",
        "/{type}/{identifier}.jsonld",
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

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    if (emConfig.isAuthEnabled()) {
      verifyReadAccess(request);
    }
    return createResponse(
        request,
        entityProfile,
        type,
        languages,
        identifier,
        FormatTypes.jsonld,
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
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
    List<EntityProfile> entityProfile = getEntityProfile(profile);
    if (emConfig.isAuthEnabled()) {
      verifyReadAccess(request);
    }

    // always return application/rdf+xml Content-Type
    return createResponse(
        request,
        entityProfile,
        type,
        languages,
        identifier,
        FormatTypes.xml,
        HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
  }

  @ApiOperation(
      value = "Retrieve a known entity",
      nickname = "getEntitySchemaJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {"/{type}/base/{identifier}.schema.jsonld", "/{type}/{identifier}.schema.jsonld"},
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

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    if (emConfig.isAuthEnabled()) {
      verifyReadAccess(request);
    }
    return createResponse(
        request,
        entityProfile,
        type,
        languages,
        identifier,
        FormatTypes.schema,
        HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
  }

  @ApiOperation(
      value = "Register a new entity",
      nickname = "registerEntity",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/",
      produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
  public ResponseEntity<String> registerEntity(
      @Valid @RequestBody EntityPreview entityCreationRequest, HttpServletRequest request)
      throws Exception {

    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.CREATE, request);
    }
    String creationRequestId = entityCreationRequest.getId();
    logger.info("Registering new entity: externalId={}", creationRequestId);

    // check if id is already being used, if so return a 301
    Optional<EntityRecord> existingEntity =
        entityRecordService.findMatchingCoreference(Collections.singletonList(creationRequestId));
    ResponseEntity<String> response = checkExistingEntity(existingEntity, creationRequestId);

    if (response != null) {
      return response;
    }

    DataSource dataSource =
        entityRecordService.verifyDataSource(entityCreationRequest.getId(), false);

    // in case of Organization it must be the zoho Organization
    String creationRequestType = entityCreationRequest.getType();
    if (EntityTypes.Organization.getEntityType().equals(creationRequestType)
        && !creationRequestId.contains(DataSources.ZOHO_ID)) {
      throw new HttpBadRequestException(
          String.format(
              "The Organization entity should come from Zoho and have the corresponding id format containing: %s",
              DataSources.ZOHO_ID));
    }

    Entity datasourceResponse = dereferenceEntity(creationRequestId, creationRequestType);

    if (datasourceResponse != null && datasourceResponse.getSameReferenceLinks() != null) {
      existingEntity =
          entityRecordService.findMatchingCoreference(datasourceResponse.getSameReferenceLinks());
      response = checkExistingEntity(existingEntity, creationRequestId);
      if (response != null) {
        return response;
      }
    }

    EntityRecord savedEntityRecord =
        entityRecordService.createEntityFromRequest(
            entityCreationRequest, datasourceResponse, dataSource);
    logger.info(
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
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.UPDATE, request);
    }
    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    if (!entityRecord.getEntity().getSameReferenceLinks().contains(url)) {
      throw new HttpBadRequestException(String.format(SAME_AS_NOT_EXISTS_MSG, url));
    }

    entityRecordService.changeExternalProxy(entityRecord, url);
    entityRecordService.update(entityRecord);
    return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
  }

  private ResponseEntity<String> createResponse(
      HttpServletRequest request,
      List<EntityProfile> entityProfile,
      String type,
      String languages,
      String identifier,
      FormatTypes outFormat,
      String contentType)
      throws EuropeanaApiException {
    // validate profile

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);
    // if request doesn't specify a valid EntityProfile, use external by default
    return generateResponseEntity(
        request, entityProfile, outFormat, languages, contentType, entityRecord, HttpStatus.OK);
  }

  /**
   * gets the first valid profile from profile param
   *
   * @param profileParamString profile request query param
   * @return valid EntityProfile in request
   */
  private List<EntityProfile> getEntityProfile(String profileParamString)
      throws HttpBadRequestException {

    if (!StringUtils.hasLength(profileParamString)) {
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

  private ResponseEntity<EntityIdResponse> scheduleBatchUpdates(
      HttpServletRequest request, List<String> entityIds, ScheduledTaskType updateType) {
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

    // get entityIds that can be scheduled (disabled = false)
    List<EntityIdDisabledStatus> nonDisabledEntities = entityIdsByDisabled.get(false);
    List<String> toBeScheduled =
        CollectionUtils.isEmpty(nonDisabledEntities)
            ? Collections.emptyList()
            : nonDisabledEntities.stream()
                .map(EntityIdDisabledStatus::getEntityId)
                .collect(Collectors.toList());

    // updates skipped if disabled=true
    List<EntityIdDisabledStatus> disabledEntities = entityIdsByDisabled.get(true);
    List<String> skipped =
        CollectionUtils.isEmpty(disabledEntities)
            ? Collections.emptyList()
            : disabledEntities.stream()
                .map(EntityIdDisabledStatus::getEntityId)
                .collect(Collectors.toList());

    entityUpdateService.scheduleTasks(toBeScheduled, updateType);

    // set required headers for this endpoint
    org.springframework.http.HttpHeaders httpHeaders = createAllowHeader(request);
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    return ResponseEntity.accepted()
        .headers(httpHeaders)
        .body(new EntityIdResponse(entityIds.size(), toBeScheduled, failures, skipped));
  }

  private ResponseEntity<EntityIdResponse> returnEmptyAcceptedResponse(HttpServletRequest request) {
    // set required headers
    org.springframework.http.HttpHeaders httpHeaders = createAllowHeader(request);
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    return ResponseEntity.accepted().headers(httpHeaders).build();
  }

  private void launchUpdateTask(String entityId) throws Exception {
    logger.info("Launching synchronous update for entityId={}", entityId);
    entityUpdateService.runSynchronousUpdate(entityId);
  }

  /** Gets the database identifier from an EntityId string */
  private String getDatabaseIdentifier(String entityId) {
    // entity id is "http://data.europeana.eu/{type}/{identifier}"
    return entityId.substring(entityId.lastIndexOf("/") + 1);
  }
}
