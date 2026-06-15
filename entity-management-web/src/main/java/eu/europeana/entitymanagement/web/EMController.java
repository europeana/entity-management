package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants.QUERY_PARAM_PROFILE_SEPARATOR;
import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.JOB_DESCRIPTION_FACTORY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_QUERY;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import eu.europeana.entitymanagement.batch.config.JobDescriptionFactory;
import eu.europeana.entitymanagement.batch.model.JobDescription;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdResponse;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.exception.DatasourceNotKnownException;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.exception.MultipleChoicesException;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import io.swagger.annotations.ApiOperation;

@RestController
@Validated
@ConditionalOnWebApplication
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final DereferenceServiceLocator dereferenceServiceLocator;
  private final DataSources datasources;
  private final EntityUpdateService entityUpdateService;
  private final JobDescriptionFactory jobDescriptionFactory;

  public static final String INVALID_UPDATE_REQUEST_MSG =
      "Request must either specify a 'query' param or contain entity identifiers in body";

  // profile used if none included in request
  public static final EntityProfile DEFAULT_REQUEST_PROFILE = EntityProfile.external;

  /**
   * Constructor to build the controller
   * @param entityRecordService the service to retrieve entities from database
   * @param solrService the service for indexing
   * @param dereferenceServiceLocator service for dereferencing external uros
   * @param datasources datasources configurations
   * @param entityUpdateService service for batch updating of entities
   * @param jobDescriptionFactory factory to fetch the updates to be run
   */
  @Autowired
  public EMController(EntityRecordService entityRecordService, SolrService solrService,
                      DereferenceServiceLocator dereferenceServiceLocator, DataSources datasources,
                      EntityUpdateService entityUpdateService,
                      @Qualifier(JOB_DESCRIPTION_FACTORY) JobDescriptionFactory jobDescriptionFactory) {
    this.entityRecordService = entityRecordService;
    this.dereferenceServiceLocator = dereferenceServiceLocator;
    this.datasources = datasources;
    this.entityUpdateService = entityUpdateService;
    this.jobDescriptionFactory = jobDescriptionFactory;
  }

  @ApiOperation(value = "Disable an entity", nickname = "disableEntity",
      response = Void.class)
  @DeleteMapping(value = {"/entity/{type}/{identifier}"}, 
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> disableEntity(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      HttpServletRequest request) throws HttpException, EuropeanaApiException {

    verifyWriteAccess(Operations.DELETE, request);

    EntityRecord entityRecord = null;
    try {
      entityRecord = entityRecordService.retrieveEntityRecord(EntityTypes.getByEntityType(type),
          identifier, profile, false);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();

    String etag =
        generateETag(isAggregatedBy.getModified(), FormatTypes.jsonld.name(), getApiVersion());
    checkIfMatchHeader(etag, request);

    String entityId = entityRecord.getEntityId();
    logger.debug("Synchronously Deprecating entityId={}", entityId);

    // delete from Solr before Mongo, so Solr errors won't leave DB in an inconsistent state
    entityRecordService.disableEntityRecord(entityRecord, true);

    return noContentResponse(request);
  }

  @ApiOperation(value = "Re-enable an entity", nickname = "enableEntity",
      response = Void.class)
  @PostMapping(value = {"/entity/{type}/{identifier}"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> enableEntity(
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false, defaultValue = "internal") String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request) throws Exception {

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    verifyWriteAccess(Operations.UPDATE, request);
    validateProfile(profile);

    EntityTypes enType = null;
    try {
      enType = EntityTypes.getByEntityType(type);
    } catch (UnsupportedEntityTypeException e1) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e1);
    }

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, true);

    if (!entityRecord.isDisabled()) {
      return generateResponseEntityForEntityRecord(request, entityProfile, FormatTypes.jsonld, null,
          HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, entityRecord, HttpStatus.OK);
    }
    logger.debug("Re-enabling entityId={}", entityRecord.getEntityId());
    entityRecordService.enableEntityRecord(entityRecord);

    entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, false);

    return launchTaskAndRetrieveEntity(request,
            enType,
            getDatabaseIdentifier(entityRecord.getEntityId()),
            entityRecord,
            EntityProfile.internal.toString(),
            false,
            jobDescriptionFactory.get(TaskType.full_update));
  }

  @ApiOperation(value = "Update an entity", nickname = "updateEntity",
      response = Void.class)
  @PutMapping(value = {"/entity/{type}/{identifier}"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> updateEntity(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestBody Entity updateRequestEntity, HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    validateBodyEntity(updateRequestEntity, false);

    EntityTypes enType = null;
    try {
      enType = EntityTypes.getByEntityType(type);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }
    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, false);

    // check that type from update request matches existing entity's
    if (!entityRecord.getEntity().getType().equals(updateRequestEntity.getType())) {
      throw new HttpBadRequestException(String.format("Request type %s does not match Entity type",
          updateRequestEntity.getType()));
    }

    verifyCoreferencesForUpdate(updateRequestEntity, entityRecord);

    Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
    String etag =
        generateETag(isAggregatedBy.getModified(), FormatTypes.jsonld.name(), getApiVersion());

    checkIfMatchHeader(etag, request);

    entityRecordService.replaceEuropeanaProxy(updateRequestEntity, entityRecord);
    entityRecordService.update(entityRecord);
    try {
      return launchTaskAndRetrieveEntity(request, EntityTypes.getByEntityType(type), identifier,
              entityRecord, profile, false, jobDescriptionFactory.get(TaskType.meta_update));
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }
  }

  /**
   * Verifies co references for the entity request sent for update.
   * @param updateRequestEntity
   * @param entityRecord
   * @throws HttpBadRequestException
   */
  private void verifyCoreferencesForUpdate(Entity updateRequestEntity, EntityRecord entityRecord)
      throws HttpBadRequestException {
    //EA- 4369 if entity is organization or Aggregator, the value sent in sameAS is ignored for the update request
    // and is overridden by the zoho Proxy . This also leads to not checking any Co-references
    if (EntityTypes.isOrganizationType(entityRecord.getEntity().getType())) {
      overrideSamAsWithZoho(updateRequestEntity, entityRecord);
      return;
    }

    // Check if all the URIs present in the “sameAs” (of provided entity) dot not clash with another
    // Entity and if all Proxies are reflected on the “sameAs”, if not respond with HTTP 400;
    // check provided coreferences, consider static data sources
    if (updateRequestEntity.getSameReferenceLinks() == null
        || updateRequestEntity.getSameReferenceLinks().isEmpty()) {
      throw new HttpBadRequestException(
          "The mandatory coreferences (sameAs or exactMatch field) are missing in the request body");
    }

    List<EntityRecord> existingEntities = entityRecordService.findEntitiesByCoreference(
        updateRequestEntity.getSameReferenceLinks(), entityRecord.getEntityId(), false);

    if (! existingEntities.isEmpty()) {
      throw new HttpBadRequestException(
          "The entity coreferences (sameAs or exactMatch field) contains an entry indicating the provided input as being a dupplicate of : "
              + EntityRecordUtils.getEntityIds(existingEntities));
    }

    // //check if existing proxy ids are present in the same as
    List<String> proxyIds = entityRecord.getExternalProxyIds();
    // check the proxies which are not available in the sameAs
    proxyIds.removeAll(updateRequestEntity.getSameReferenceLinks());
    if (!proxyIds.isEmpty()) {
      throw new HttpBadRequestException(
          "The coreferences (sameAs or exactMatch field) of the request body does not contain the following proxies of the existing record:"
              + proxyIds);
    }
  }

  /**
   * update entity request 'sameAs' field with the zoho proxy id
   * @param updateRequestEntity update request sent
   * @param entityRecord entity record (existing in db)
   * @throws HttpBadRequestException
   */
  private void overrideSamAsWithZoho(Entity updateRequestEntity, EntityRecord entityRecord) throws HttpBadRequestException {
    if (entityRecord.getZohoProxy() != null) {
      updateRequestEntity.setSameReferenceLinks(
              Collections.singletonList(entityRecord.getZohoProxy().getProxyId()));
    } else {
      throw new HttpBadRequestException(
              "There is no Zoho proxy present for the " + entityRecord.getEntity().getType() +
                      " entity with id " + entityRecord.getEntityId());
    }
  }


  @ApiOperation(value = "Update an entity from external data source",
      nickname = "updateEntityFromDatasource", response = Void.class)
  @PostMapping(value = "/entity/{type}/{identifier}/management/update",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> triggerSingleEntityFullUpdate(
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    EntityTypes enType = null;
    try {
      enType = EntityTypes.getByEntityType(type);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }

    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, false);
    // update from external data source is not available for static data sources
    datasources.verifyDataSource(entityRecord.getExternalProxies().get(0).getProxyId(), false);
    return launchTaskAndRetrieveEntity(request, enType, identifier, entityRecord, profile, false,
            jobDescriptionFactory.get(TaskType.full_update));
  }

  @ApiOperation(value = "Update multiple entities from external data source",
      nickname = "updateMultipleEntityFromDatasource", response = Void.class)
  @PostMapping(value = "/entity/management/update",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityIdResponse> triggerFullUpdateMultipleEntities(
      @RequestBody(required = false) List<String> entityIds,
      @RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
      HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    // query param takes precedence over request body
    if (StringUtils.isNotEmpty(query)) {
      return scheduleUpdatesWithSearch(request, query, TaskType.full_update);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    return scheduleBatchUpdates(request, entityIds, TaskType.full_update);
  }

  @ApiOperation(value = "Update metrics for given entities", nickname = "updateMetricsForEntities",
      response = Void.class)
  @PostMapping(value = "/entity/management/metrics",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EntityIdResponse> triggerMetricsUpdateMultipleEntities(
      @RequestBody(required = false) List<String> entityIds,
      @RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
      HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    // query param takes precedence over request body
    if (StringUtils.isNotEmpty(query)) {
      return scheduleUpdatesWithSearch(request, query, TaskType.metrics_update);
    }

    if (CollectionUtils.isEmpty(entityIds)) {
      throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
    }

    return scheduleBatchUpdates(request, entityIds, TaskType.metrics_update);
  }

  /**
   * Synchronize Organizations from Zoho
   *
   * @param type type of entity
   * @param identifier entity id
   * @param action used with either “enable” or “disable” to mark an entity to be used or not for
   *        enrichment using labels.
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(value = "Enable or disable the use of the entity for enrichment",
      nickname = "enableDisableForEnrich", response = Void.class)
  @PostMapping(value = "/entity/{type}/{identifier}/management/enrich",
      produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
  public ResponseEntity<String> enableDisableForEnrich(
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = "action") String action,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false,
          defaultValue = "internal") String profile,
      HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);
    validateProfile(profile);
    validateAction(action);

    EntityRecord entityRecord = entityRecordService
        .updateUsedForEnrichment(EntityTypes.getByEntityType(type), identifier, profile, action);

    return launchTaskAndRetrieveEntity(request,
            EntityTypes.getByEntityType(entityRecord.getEntity().getType()),
            getDatabaseIdentifier(entityRecord.getEntityId()),
            entityRecord,
            EntityProfile.internal.toString(),
            true,
            new JobDescription(
                     TaskType.metrics_update
                    , null
                    , JobDescription.PERSISTENCE_ITEM_WRITERS));
  }

  private void validateAction(String action) throws HttpBadRequestException {
    if (!StringUtils.equalsAnyIgnoreCase(action, WebEntityConstants.ACTION_ENABLE,
        WebEntityConstants.ACTION_DISABLE)) {
      throw new HttpBadRequestException(
          "Invalid value for param action: " + action + ". Supported values are ["
              + WebEntityConstants.ACTION_ENABLE + ", " + WebEntityConstants.ACTION_DISABLE + "]");
    }
  }

  @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd",
      response = Void.class)
  @GetMapping(
      value = {"/entity/{type}/base/{identifier}.jsonld", "/entity/{type}/base/{identifier}.json",
          "/entity/{type}/{identifier}.jsonld", "/entity/{type}/{identifier}.json",
          "/entity/{type}/base/{identifier}", "/entity/{type}/{identifier}"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getJsonLdEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE,
          required = false) String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request) throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);

    try {
      return createResponseForRetrieve(EntityTypes.getByEntityType(type), identifier, profile, request,
          FormatTypes.jsonld, languages, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }
  }

  @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml",
      response = Void.class)
  @RequestMapping(
      value = {"/entity/{type}/base/{identifier}.xml", "/entity/{type}/{identifier}.xml",
          "/entity/{type}/{identifier}"},
      method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE,
          HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML})
  public ResponseEntity<String> getXmlEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE,
          required = false) String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request) throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);

    try {
      return createResponseForRetrieve(EntityTypes.getByEntityType(type), identifier, profile, request,
          FormatTypes.xml, languages, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }
  }

  @ApiOperation(value = "Retrieve a known entity", nickname = "getEntitySchemaJsonLd",
      response = Void.class)
  @GetMapping(
      value = {"/entity/{type}/base/{identifier}.schema.jsonld",
          "/entity/{type}/base/{identifier}.schema.json",
          "/entity/{type}/{identifier}.schema.jsonld", "/entity/{type}/{identifier}.schema.json"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getEntitySchemaJsonLd(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
          required = false) String profile,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE,
          required = false) String languages,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request) throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);

    try {
      return createResponseForRetrieve(EntityTypes.getByEntityType(type), identifier, profile, request,
          FormatTypes.schema, languages, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }
  }

  @ApiOperation(value = "Register a new entity", nickname = "registerEntity",
      response = Void.class)
  @PostMapping(value = "/entity/",
      produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
  public ResponseEntity<String> registerEntity(
    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
            required = false, defaultValue = "internal") String profile,
    @RequestBody Entity europeanaProxyEntity,
      HttpServletRequest request) throws Exception {

    verifyWriteAccess(Operations.CREATE, request);

    validateProfile(profile);
    validateBodyEntity(europeanaProxyEntity, false);

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

    List<EntityRecord> existingEntities =
        entityRecordService.findEntitiesByCoreference(corefs, null, false);
    // verify disabled or redirected
    ResponseEntity<String> response =
        checkExistingEntity(existingEntities, creationRequestId);

    if (response != null) {
      return response;
    }

    DataSource dataSource = datasources.verifyDataSource(creationRequestId, false);

    // in case of Organization it must be the zoho Organization
    String creationRequestType = europeanaProxyEntity.getType();
    if(EntityTypes.isOrganizationType(creationRequestType) 
        && !creationRequestId.contains(WebEntityFields.ZOHO_CRM_HOST)) {
      throw new HttpBadRequestException(String.format(
          "The Organization entity should come from Zoho and have the corresponding id format containing: %s",
          WebEntityFields.ZOHO_CRM_HOST));
    }

    Entity datasourceResponse = dereferenceEntity(creationRequestId, creationRequestType);

    EntityRecord savedEntityRecord = entityRecordService
        .createEntityFromRequest(europeanaProxyEntity, datasourceResponse, dataSource, null);
    logger.debug("Created Entity record for externalId={}; entityId={}", creationRequestId,
        savedEntityRecord.getEntityId());

    return launchTaskAndRetrieveEntity(request,
            EntityTypes.getByEntityType(savedEntityRecord.getEntity().getType()),
            getDatabaseIdentifier(savedEntityRecord.getEntityId()),
            savedEntityRecord,
            EntityProfile.internal.toString(),
            false,
            jobDescriptionFactory.get(TaskType.full_update));
  }

  Entity dereferenceEntity(String creationRequestId, String creationRequestType) throws Exception {
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
          String.format("Datasource type '%s' does not match type '%s' in request",
              datasourceResponse.getType(), creationRequestType));
    }
    return datasourceResponse;
  }

//  @ApiOperation(value = "Change provenance for an Entity", nickname = "changeProvenance")
//  @PutMapping(value = "/entity/{type}/{identifier}/management/source",
//      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
//  public ResponseEntity<String> changeProvenance(
//      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
//      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
//      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
//          required = false, defaultValue = "internal") String profile,
//      @RequestParam(value = WebEntityConstants.PATH_PARAM_URL) String url,
//      HttpServletRequest request) throws Exception {
//
//    verifyWriteAccess(Operations.UPDATE, request);
//    validateProfile(profile);
//
//    EntityTypes enType = EntityTypes.getByEntityType(type);
//    EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, false);
//
//    if (!entityRecord.getEntity().getSameReferenceLinks().contains(url)) {
//      throw new HttpBadRequestException(String.format(SAME_AS_NOT_EXISTS_MSG, url));
//    }
//
//    entityRecordService.changeExternalProxy(entityRecord, url);
//    entityRecordService.update(entityRecord);
//    return launchTaskAndRetrieveEntity(request, enType, identifier, entityRecord, profile, false,
//            jobDescriptionFactory.get(TaskType.full_update));
//  }



    @ApiOperation(value = "Change provenance for an Entity", nickname = "changeProvenance")
    @PutMapping(value = "/entity/{type}/{identifier}/management/source",
            produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> changeProvenance(
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE,
                    required = false, defaultValue = "internal") String profile,
            @RequestBody List<String> urls,
            HttpServletRequest request) throws Exception {

        verifyWriteAccess(Operations.UPDATE, request);
        validateProfile(profile);

        EntityTypes enType = EntityTypes.getByEntityType(type);
        EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(enType, identifier, profile, false);

        entityRecordService.checkIfSameAsExists(entityRecord, urls);

        // get the list of already existing proxies, as the list will get updated later
      List<EntityProxy> externalProxies = entityRecord.getExternalProxies();
      for (String url : urls) {
          entityRecordService.changeExternalProxy(entityRecord, url, externalProxies);
      }

      // remove any datasource proxy that is not present in the url
      entityRecordService.removeExternalProxy(entityRecord, urls);

      // sort the proxies in the order of the url
      EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();
      urls.add(entityRecord.getProxies().indexOf(europeanaProxy), europeanaProxy.getProxyId());
      entityRecord.getProxies().sort(Comparator.comparing(v -> urls.indexOf(v.getProxyId())));

      entityRecordService.update(entityRecord);
      return launchTaskAndRetrieveEntity(request, enType, identifier, entityRecord, profile, false,
                jobDescriptionFactory.get(TaskType.full_update));
    }

  @ApiOperation(value = "Retrieve multiple entities", nickname = "retrieveEntities")
  @PostMapping(value = "/entity/retrieve",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> retrieveEntities(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false) String profiles,
      @RequestBody List<String> urls, HttpServletRequest request) throws Exception {

    verifyReadAccess(request);

    return createResponseMultipleEntities(urls, profiles, request);
  }

  private ResponseEntity<String> createResponseForRetrieve(EntityTypes type, String identifier,
      String profile, HttpServletRequest request, FormatTypes outFormat, String languages,
      String contentType) throws EuropeanaApiException {

    EntityRecord entityRecord = null;
    
    try {
      //retrieve record
      entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, profile, true);
    } catch (EntityNotFoundException ex) {
      //if not found, verify co-references
      String redirectUri = entityRecordService.getRedirectUriWhenNotFound(type, identifier, ex, request);
      // return 301 redirect
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI.create(redirectUri))
          .build();
    }

    if (entityRecord.isDisabled()) {
      //if disabled verify co-references
      String redirectUri = entityRecordService.getRedirectUriWhenDeprecated(entityRecord, identifier, request);
      // return 301 redirect
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI.create(redirectUri))
          .build();
    }

    List<EntityProfile> entityProfile = getEntityProfile(profile);
    // if request doesn't specify a valid EntityProfile, use external by default
    return generateResponseEntityForEntityRecord(request, entityProfile, outFormat, languages,
        contentType, entityRecord, HttpStatus.OK);
  }

  private ResponseEntity<String> createResponseMultipleEntities(List<String> entityIds, String profiles,
      HttpServletRequest request) throws EuropeanaApiException {
    List<EntityRecord> entityRecords =
        entityRecordService.retrieveMultipleByEntityIdsOrCoreference(entityIds, profiles);

    // create response headers
    String contentType = HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
    org.springframework.http.HttpHeaders headers = createAllowHeader(request);
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);

    String body = serialize(entityRecords);
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

    String[] splitAndTrimProfile=Arrays.stream(profileParamString.split(QUERY_PARAM_PROFILE_SEPARATOR)).map(String::trim).toArray(String[]::new);
    
    List<String> requestProfiles = new ArrayList<>(List.of((splitAndTrimProfile)));

    List<String> validProfiles =
        Arrays.stream(EntityProfile.values()).map(Enum::name).collect(Collectors.toList());

    requestProfiles.retainAll(validProfiles);

    // profile string contains an invalid value, or is internal,external (which can't be specified
    // together)
    if (requestProfiles.isEmpty()
        || (requestProfiles.contains(EntityProfile.internal.name()) && requestProfiles.contains(EntityProfile.external.name()))) {
      throw new HttpBadRequestException("Invalid profile query param " + profileParamString);
    }
    return requestProfiles.stream().map(EntityProfile::valueOf).collect(Collectors.toList());
  }

  private ResponseEntity<String> launchTaskAndRetrieveEntity(HttpServletRequest request,
                                                             EntityTypes type, String identifier, EntityRecord entityRecord, String profile,
                                                             boolean includeDisabled,
                                                             JobDescription jobDescription) throws Exception {

    // launch synchronous update, then retrieve entity from DB afterwards
    entityUpdateService.runSynchronousUpdate(entityRecord.getEntityId(), jobDescription);

    entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, profile, includeDisabled);

    return generateResponseEntityForEntityRecord(request, getEntityProfile(profile),
        FormatTypes.jsonld, null, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, entityRecord,
        HttpStatus.OK);
  }

  private ResponseEntity<String> checkExistingEntity(List<EntityRecord> existingEntities,
      String entityCreationId) throws MultipleChoicesException {

    if (existingEntities == null || existingEntities.isEmpty()) {
      return null;
    } else if (existingEntities.size() > 1) {
      // something went wrong, there should be no dupplicates in the database
      throw new MultipleChoicesException(
          String.format(EntityRecordUtils.MULTIPLE_CHOICES_FOR_REDIRECTION_MSG, entityCreationId,
              EntityRecordUtils.getEntityIds(existingEntities).toString()));
    } else {

//      EA-4322 - disabled entites should as well be redirected
//      // existingEntities contains only one dupplicate
//      if (existingEntities.get(0).isDisabled()) {
//        throw new EntityRemovedException(String.format(EXTERNAL_ID_REMOVED_MSG, entityCreationId,
//            existingEntities.get(0).getEntityId()));
//      }

      // return 301 redirect
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
          .location(UriComponentsBuilder.newInstance().path("/entity/{id}")
              .buildAndExpand(EntityRecordUtils
                  .extractEntityPathFromEntityId(existingEntities.get(0).getEntityId()))
              .toUri())
          .build();
    }
  }

  private ResponseEntity<EntityIdResponse> scheduleBatchUpdates(HttpServletRequest request,
      List<String> entityIds, TaskType updateType) {
    // get the entities to be scheduled, failed and skipped for update
    EntityIdResponse entityIdResponse = new EntityIdResponse();
    List<String> entityIdsToSchedule = entityUpdateService.updateEntityIdResponse(entityIdResponse, entityIds);
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
  ResponseEntity<EntityIdResponse> scheduleUpdatesWithSearch(HttpServletRequest request,
      String query, TaskType updateType) throws SolrServiceException {
    EntityIdResponse entityIdResponse = entityUpdateService.scheduleUpdatesWithSearch(query, updateType);

    // set required headers for this endpoint
    org.springframework.http.HttpHeaders httpHeaders = createAllowHeader(request);
    httpHeaders.add(CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
    return ResponseEntity.accepted().headers(httpHeaders).body(entityIdResponse);
  }

}
