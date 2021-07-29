package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.model.BatchUpdateType;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdResponse;
import eu.europeana.entitymanagement.exception.EntityMismatchException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_QUERY;


@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

	private final EntityRecordService entityRecordService;
	private final SolrService solrService;
	private final MetisDereferenceService dereferenceService;
	private final DataSources datasources;
	private final EntityUpdateService entityUpdateService;
	private final EntityManagementConfiguration emConfig;

	private static final String EXTERNAL_ID_REMOVED_MSG = "Entity id '%s' already exists as '%s', which has been removed";
	private static final String SAME_AS_NOT_EXISTS_MSG = "Url '%s' does not exist in entity owl:sameAs";
	public static final String INVALID_UPDATE_REQUEST_MSG = "Request must either specify a 'query' param or contain entity identifiers in body";


  @Autowired
	public EMController(EntityRecordService entityRecordService,
						SolrService solrService, MetisDereferenceService dereferenceService, DataSources datasources,
						EntityUpdateService entityUpdateService,
						EntityManagementConfiguration emConfig) {
		this.entityRecordService = entityRecordService;
	  this.solrService = solrService;
	  this.dereferenceService = dereferenceService;
		this.datasources = datasources;
		this.entityUpdateService = entityUpdateService;
		this.emConfig = emConfig;
	}

	@ApiOperation(value = "Disable an entity", nickname = "disableEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}",
	    "/{type}/{identifier}" }, method = RequestMethod.DELETE, produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> disableEntity(
	    @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException, EuropeanaApiException {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.DELETE, request);
		}
		EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), false);

		Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
		long timestamp = isAggregatedBy != null ?
				isAggregatedBy.getModified().getTime() :
				0L;

	    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());
		checkIfMatchHeaderWithQuotes(etag, request);
	    entityRecordService.disableEntityRecord(entityRecord);

		return noContentResponse(request);
	}

	@ApiOperation(value = "Re-enable an entity", nickname = "enableEntity", response = java.lang.Void.class)
	@RequestMapping(value = { "/{type}/base/{identifier}",
			"/{type}/{identifier}" }, method = RequestMethod.POST, produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> enableEntity(
			@RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			HttpServletRequest request) throws HttpException, EuropeanaApiException {

		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase(), true);
		if (!entityRecord.isDisabled()) {

			return createResponse(request, profile, type, null, identifier, FormatTypes.jsonld, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
		}
		logger.info("Re-enabling entityId={}", entityRecord.getEntityId());
		entityRecordService.enableEntityRecord(entityRecord);
		return createResponse(request, profile, type, null, identifier, FormatTypes.jsonld, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
	}

    @ApiOperation(value = "Update an entity", nickname = "updateEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}", "/{type}/{identifier}" },method = RequestMethod.PUT, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateEntity(
    	@RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    @RequestBody Entity updateRequestEntity,
	    HttpServletRequest request) throws Exception {
			if (emConfig.isAuthEnabled()) {
				verifyWriteAccess(Operations.UPDATE, request);
			}
		 EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

    	// TODO: Re-enable authentication
    	//verifyReadAccess(request);

			// check that  type from update request matches existing entity's
		if(!entityRecord.getEntity().getType().equals(updateRequestEntity.getType())){
			throw new HttpBadRequestException(String.format("Request type %s does not match Entity type",
					updateRequestEntity.getType()));
		}

			Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
			long timestamp = isAggregatedBy != null ?
					isAggregatedBy.getModified().getTime() :
					0L;

		 String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());

		 checkIfMatchHeaderWithQuotes(etag, request);

			entityRecordService.replaceEuropeanaProxy(updateRequestEntity, entityRecord);
			entityRecordService.update(entityRecord);

			// this replaces the existing entity
			solrService.storeEntity(createSolrEntity(entityRecord.getEntity()));
			logger.info("Updated Solr document for entityId={}", entityRecord.getEntityId());

			return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
    }


	@ApiOperation(value = "Update an entity from external data source", nickname = "updateEntityFromDatasource", response = java.lang.Void.class)
	@PostMapping(value = "/{type}/{identifier}/management/update", produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> triggerSingleEntityFullUpdate(
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);
		return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
	}

	@ApiOperation(value = "Update multiple entities from external data source", nickname = "updateMultipleEntityFromDatasource", response = java.lang.Void.class)
	@PostMapping(value = "/management/update", produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<EntityIdResponse> triggerFullUpdateMultipleEntities(
			@RequestBody (required = false) List<String> entityIds,
			@RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}

		// query param takes precedence over request body
		if(StringUtils.hasLength(query)){
			entityUpdateService.scheduleUpdatesWithSearch(query, BatchUpdateType.FULL);
			return ResponseEntity.accepted().build();
		}

		if(CollectionUtils.isEmpty(entityIds)){
			throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
		}

		List<String> existingEntityIds = entityRecordService.retrieveMultipleByEntityId(entityIds);
		// get entityIds in request that weren't retrieved from db
		List<String> failures = entityIds.stream().filter(e -> !existingEntityIds.contains(e))
				.collect(Collectors.toList());

		entityUpdateService.scheduleUpdates(existingEntityIds, BatchUpdateType.FULL);
		return  ResponseEntity.accepted().body(new EntityIdResponse(entityIds.size(), existingEntityIds, failures));
	}


	@ApiOperation(value = "Update metrics for given entities", nickname = "updateMultipleEntityFromDatasource", response = java.lang.Void.class)
	@PostMapping(value = "/management/metrics", produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<EntityIdResponse> triggerMetricsUpdateMultipleEntities(
			@RequestBody (required = false) List<String> entityIds,
			@RequestParam(value = QUERY_PARAM_QUERY, required = false) String query,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}

		// query param takes precedence over request body
		if(StringUtils.hasLength(query)){
			entityUpdateService.scheduleUpdatesWithSearch(query, BatchUpdateType.METRICS);
			return ResponseEntity.accepted().build();
		}

		if(CollectionUtils.isEmpty(entityIds)){
			throw new HttpBadRequestException(INVALID_UPDATE_REQUEST_MSG);
		}

		List<String> existingEntityIds = entityRecordService.retrieveMultipleByEntityId(entityIds);
		List<String> failures = entityIds.stream()
				.filter(e -> !existingEntityIds.contains(e))
				.collect(Collectors.toList());

		entityUpdateService.scheduleUpdates(existingEntityIds, BatchUpdateType.METRICS);
		return  ResponseEntity.accepted().body(new EntityIdResponse(entityIds.size(), existingEntityIds, failures));
	}

	@ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd", response = java.lang.Void.class)
    @GetMapping(value = { "/{type}/base/{identifier}.jsonld",
	    "/{type}/{identifier}.jsonld", "/{type}/{identifier}" }, produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getJsonLdEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE, required = false) String languages,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			HttpServletRequest request)
				throws EuropeanaApiException, HttpException {
		if (emConfig.isAuthEnabled()) {
			verifyReadAccess(request);
		}
	return createResponse(request, profile, type, languages, identifier, FormatTypes.jsonld,
			HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);

    }


    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}.xml",
	    "/{type}/{identifier}.xml", "/{type}/{identifier}" }, method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_XML_VALUE,
		    HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML})
    public ResponseEntity<String> getXmlEntity(
			@RequestHeader(value = HttpHeaders.ACCEPT) String acceptHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_LANGUAGE, required = false) String languages,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
				HttpServletRequest request)
				throws EuropeanaApiException, HttpException {
		if (emConfig.isAuthEnabled()) {
				verifyReadAccess(request);
			}

		// always return application/rdf+xml Content-Type
	return createResponse(request, profile, type, languages, identifier, FormatTypes.xml,
			HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
    @PostMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE_JSONLD})
		public ResponseEntity<String> registerEntity(
			@Valid @RequestBody EntityPreview entityCreationRequest, HttpServletRequest request)
				throws Exception {

			if (emConfig.isAuthEnabled()) {
				verifyWriteAccess(Operations.CREATE, request);
			}
			logger.info("Registering new entity: externalId={}", entityCreationRequest.getId());
	
			// check if id is already being used, if so return a 301
			Optional<EntityRecord> existingEntity = entityRecordService
					.findMatchingCoreference(entityCreationRequest.getId());
			ResponseEntity<String> response = checkExistingEntity(existingEntity,
					entityCreationRequest.getId());

			if (response != null) {
				return response;
			}

			// return 400 error if ID does not match a configured datasource
			if (!datasources.hasDataSource(entityCreationRequest.getId())) {
				throw new HttpBadRequestException(String
						.format("id %s does not match a configured datasource", entityCreationRequest.getId()));
			}

			Entity metisResponse = dereferenceService
					.dereferenceEntityById(entityCreationRequest.getId());

			if(!metisResponse.getType().equals(entityCreationRequest.getType())){
				throw new EntityMismatchException(String.format("Metis type '%s' does not match type '%s' in request",
						metisResponse.getType(), entityCreationRequest.getType()));
			}

			if (metisResponse.getSameAs() != null) {
				existingEntity = entityRecordService
						.retrieveMetisCoreferenceSameAs(metisResponse.getSameAs());
				response = checkExistingEntity(existingEntity, entityCreationRequest.getId());
				if (response != null) {
					return response;
				}
			}

			EntityRecord savedEntityRecord = entityRecordService
					.createEntityFromRequest(entityCreationRequest, metisResponse);
			logger.info("Created Entity record for externalId={}; entityId={}", entityCreationRequest.getId(), savedEntityRecord.getEntityId());

			solrService.storeEntity(createSolrEntity(savedEntityRecord.getEntity()));
			logger.info("Indexed entity to Solr: externalId={}; entityId={}", entityCreationRequest.getId(), savedEntityRecord.getEntityId());


		return launchTaskAndRetrieveEntity(request, savedEntityRecord.getEntity().getType(),
					getDatabaseIdentifier(savedEntityRecord.getEntityId()), savedEntityRecord,
					EntityProfile.internal.toString());
		}

	@ApiOperation(value = "Change provenance for an Entity", nickname = "changeProvenance")
	@PutMapping(value = "/{type}/{identifier}/management/source", produces = {HttpHeaders.CONTENT_TYPE_JSONLD,
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> changeProvenance(
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile,
			@RequestParam(value = WebEntityConstants.PATH_PARAM_URL) String url,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

		if(!entityRecord.getEntity().getSameAs().contains(url)){
			throw new HttpBadRequestException(String.format(SAME_AS_NOT_EXISTS_MSG, url));
		}

		entityRecordService.changeExternalProxy(entityRecord, url);
		entityRecordService.update(entityRecord);
		return launchTaskAndRetrieveEntity(request, type, identifier, entityRecord, profile);
	}

    private ResponseEntity<String> createResponse(HttpServletRequest request, String profile, String type, String languages, String identifier, FormatTypes outFormat,
												  String contentType) throws EuropeanaApiException {
			/*
	 * verify the parameters
	 */
	boolean valid_profile = false;
	for (EntityProfile ep : EntityProfile.values()) {
	    if (ep.name().equals(profile)) {
		valid_profile = true;
		break;
	    }
	}
	if (!valid_profile) {
	    throw new HttpBadRequestException("Invalid profile");
	}

			EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier,false);
			return generateResponseEntity(request, profile, outFormat, languages, contentType, entityRecord,
					HttpStatus.OK);
		}

	private ResponseEntity<String> launchTaskAndRetrieveEntity(HttpServletRequest request, String type, String identifier,
															   EntityRecord entityRecord, String profile) throws Exception {
		// launch synchronous update, then retrieve entity from DB afterwards
		launchUpdateTask(entityRecord.getEntityId());
		entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

		return generateResponseEntity(request, profile, FormatTypes.jsonld, null, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8,
				entityRecord, HttpStatus.ACCEPTED);
	}

	private ResponseEntity<String> checkExistingEntity(Optional<EntityRecord> existingEntity,
			String entityCreationId)
			throws EntityRemovedException {

		if (existingEntity.isPresent()) {
			if (existingEntity.get().isDisabled()) {
				throw new EntityRemovedException(String
						.format(EXTERNAL_ID_REMOVED_MSG, entityCreationId, existingEntity.get().getEntityId()));
			}

			// return 301 redirect
			return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
					.location(UriComponentsBuilder.newInstance().path("/entity/{id}")
							.buildAndExpand(
							EntityRecordUtils.extractIdentifierFromEntityId(existingEntity.get().getEntityId())).toUri())
					.build();
		}
		return null;
	}

	private void launchUpdateTask(String entityId)
      throws Exception {
    logger.info("Launching synchronous update for entityId={}", entityId);
    entityUpdateService.runSynchronousUpdate(entityId);
  }

	/**
	 * Gets the database identifier from an EntityId string
	 */
	private String getDatabaseIdentifier(String entityId) {
		//entity id is "http://data.europeana.eu/{type}/{identifier}"
		return entityId.substring(entityId.lastIndexOf("/") + 1);
	}
}
