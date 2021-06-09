package eu.europeana.entitymanagement.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdResponse;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.EtagMismatchException;
import eu.europeana.entitymanagement.exception.FunctionalRuntimeException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import io.swagger.annotations.ApiOperation;

/*
 * Suggestions:
 * 1. please use @RequestMapping consistently and avoid @PostMapping or @GetMapping (the annotation are used to analyze the methods signatures)
 */

@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final MetisDereferenceService dereferenceService;
  private final DataSources datasources;
  private final BatchService batchService;
  private final EntityManagementConfiguration emConfig;
  static Map<String,Set<String>> methodsRESTTypes = new HashMap<>();

  private static final String EXTERNAL_ID_REMOVED_MSG = "Entity id '%s' already exists as '%s', which has been removed";

  @Autowired
	public EMController(EntityRecordService entityRecordService,
			MetisDereferenceService dereferenceService, DataSources datasources,
			BatchService batchService,
			EntityManagementConfiguration emConfig) {
		this.entityRecordService = entityRecordService;
		this.dereferenceService = dereferenceService;
		this.datasources = datasources;
		this.batchService = batchService;
		this.emConfig = emConfig;
		getMethodRESTTypes(methodsRESTTypes, EMController.class);
	}

	@ApiOperation(value = "Disable an entity", nickname = "disableEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}",
	    "/{type}/{identifier}" }, method = RequestMethod.DELETE)
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

	    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion(), "external");
	    checkIfMatchHeader(etag, request);
	    entityRecordService.disableEntityRecord(entityRecord);
	    return ResponseEntity.noContent().build();
    }

	@ApiOperation(value = "Re-enable an entity", nickname = "enableEntity", response = java.lang.Void.class)
	@RequestMapping(value = { "/{type}/base/{identifier}",
			"/{type}/{identifier}" }, method = RequestMethod.POST, produces = { HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
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

		String methodRequestMappingValue=null;
		try {
			methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
		}

		if (!entityRecord.isDisabled()) {
			return createResponse(profile, type, identifier, FormatTypes.jsonld, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
		}
		logger.info("Re-enabling entityId={}", entityRecord.getEntityId());
		entityRecordService.enableEntityRecord(entityRecord);
		return createResponse(profile, type, identifier, FormatTypes.jsonld, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
	}

    @ApiOperation(value = "Update an entity", nickname = "updateEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}", "/{type}/{identifier}" },method = RequestMethod.PUT,
    produces = { HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
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

			// check that  type from update request matches existing entity's
		if(!entityRecord.getEntity().getType().equals(updateRequestEntity.getType())){
			throw new HttpBadRequestException(String.format("Request type %s does not match Entity type",
					updateRequestEntity.getType()));
		}

			Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
			long timestamp = isAggregatedBy != null ?
					isAggregatedBy.getModified().getTime() :
					0L;

		 String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion(), profile);

			try {
				checkIfMatchHeader(etag, request);
			} catch (HttpException e) {
				throw new EtagMismatchException("If-Match header value does not match generated ETag for entity");
			}

			entityRecordService.replaceEuropeanaProxy(updateRequestEntity, entityRecord);
			entityRecordService.update(entityRecord);
			
			String methodRequestMappingValue=null;
			try {
				methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
			}
			return launchTaskAndRetrieveEntity(type, identifier, entityRecord, profile, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
    }

	@ApiOperation(value = "Update an entity from external data source", nickname = "updateEntityFromDatasource", response = java.lang.Void.class)
	@RequestMapping(value = "/{type}/{identifier}/management/update", method = RequestMethod.POST)
	public ResponseEntity<String> updateFromExternalSource(
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);
		String methodRequestMappingValue=null;
		try {
			methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
		}
		return launchTaskAndRetrieveEntity(type, identifier, entityRecord, profile, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
	}

	@ApiOperation(value = "Update multiple entities from external data source", nickname = "updateMultipleEntityFromDatasource", response = java.lang.Void.class)
	@RequestMapping(value = "/management/update", method = RequestMethod.POST)
	public ResponseEntity<EntityIdResponse> updateMultipleExternalSource(
			@RequestBody List<String> entityIds,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}

		List<String> existingEntityIds = entityRecordService.retrieveMultipleByEntityId(entityIds);
		// get entityIds in request that weren't retrieved from db
		List<String> failures = entityIds.stream().filter(e -> !existingEntityIds.contains(e))
				.collect(Collectors.toList());

		// runAsynchronously since we're not including updated entities in response
		launchUpdateTask(existingEntityIds, true);

		return  ResponseEntity.accepted().body(new EntityIdResponse(entityIds.size(), existingEntityIds, failures));
	}

	@ApiOperation(value = "Update metrics for given entities", nickname = "updateMultipleEntityFromDatasource", response = java.lang.Void.class)
	@RequestMapping(value = "/management/metrics", method = RequestMethod.POST)
	public ResponseEntity<EntityIdResponse> updateMetricsMultiple(
			@RequestBody List<String> entityIds,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}

		List<String> existingEntityIds = entityRecordService.retrieveMultipleByEntityId(entityIds);
		List<String> failures = entityIds.stream()
				.filter(e -> !existingEntityIds.contains(e))
				.collect(Collectors.toList());
		launchUpdateMetrics(existingEntityIds);

		return  ResponseEntity.accepted().body(new EntityIdResponse(entityIds.size(), existingEntityIds, failures));
	}

	@CrossOrigin(exposedHeaders= {HttpHeaders.ALLOW, HttpHeaders.VARY, HttpHeaders.LINK, HttpHeaders.ETAG})
	@ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}.jsonld",
	    "/{type}/{identifier}.jsonld" }, method = RequestMethod.GET, produces = { HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getJsonLdEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			HttpServletRequest request)
				throws EuropeanaApiException, HttpException {
		if (emConfig.isAuthEnabled()) {
			verifyReadAccess(request);
		}

		String methodRequestMappingValue=null;
		try {
			methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
		}

		return createResponse(profile, type, identifier, FormatTypes.jsonld, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
		

    }

	@CrossOrigin(exposedHeaders= {HttpHeaders.ALLOW, HttpHeaders.VARY, HttpHeaders.LINK, HttpHeaders.ETAG})
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}","/{type}/{identifier}" }, 
    method = RequestMethod.GET, produces = { HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getEntity(
    	@RequestHeader(value = HttpHeaders.ACCEPT) String acceptHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws EuropeanaApiException, HttpException {
			if (emConfig.isAuthEnabled()) {
				verifyReadAccess(request);
			}

			String methodRequestMappingValue=null;
			try {
				methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
			}

			if (acceptHeader.contains(HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML)) {
				return createResponse(profile, type, identifier, FormatTypes.xml, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
			}
			else if (acceptHeader.contains(HttpHeaders.CONTENT_TYPE_JSONLD) || acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE) || acceptHeader.contains(MediaType.ALL_VALUE)){	
				return createResponse(profile, type, identifier, FormatTypes.jsonld, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
			}
			else {
				return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Requested formats in the Accept header not supported.");
			}
    }

	@CrossOrigin(exposedHeaders= {HttpHeaders.ALLOW, HttpHeaders.VARY, HttpHeaders.LINK, HttpHeaders.ETAG})
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}.xml",
	    "/{type}/{identifier}.xml" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getXmlEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
				HttpServletRequest request)
				throws EuropeanaApiException, HttpException {
			if (emConfig.isAuthEnabled()) {
				verifyReadAccess(request);
			}
			
			String methodRequestMappingValue=null;
			try {
				methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
			}

			return createResponse(profile, type, identifier, FormatTypes.xml, String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
			
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> registerEntity(
				@RequestBody EntityPreview entityCreationRequest, HttpServletRequest request)
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

			String methodRequestMappingValue=null;
			try {
				methodRequestMappingValue = this.getClass().getMethod(new Object() {}.getClass().getEnclosingMethod().getName(), new Object() {}.getClass().getEnclosingMethod().getParameterTypes()).getAnnotation(RequestMapping.class).value()[0].split("\\.", 2)[0];
			}
			catch (NoSuchMethodException | SecurityException e) {
				throw new FunctionalRuntimeException("An exception occured during getting the method @RequestMapping value", e);
			}

			return launchTaskAndRetrieveEntity(savedEntityRecord.getEntity().getType(),
					getDatabaseIdentifier(savedEntityRecord.getEntityId()), savedEntityRecord,
					EntityProfile.internal.toString(), String.join(",", methodsRESTTypes.get(methodRequestMappingValue)));
		}


    private ResponseEntity<String> createResponse(String profile, String type, String identifier, FormatTypes outFormat,
	    String allowHeaderParams) throws EuropeanaApiException {
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
			return generateResponseEntity(profile, outFormat, entityRecord, HttpStatus.OK, allowHeaderParams);
		}

	private ResponseEntity<String> launchTaskAndRetrieveEntity(String type, String identifier,
			EntityRecord entityRecord, String profile, String allowHeaderParams) throws Exception {
		// launch synchronous update, then retrieve entity from DB afterwards
		launchUpdateTask(Collections.singletonList(entityRecord.getEntityId()), false);
		entityRecord = entityRecordService.retrieveEntityRecord(type, identifier, false);

		return generateResponseEntity(profile, FormatTypes.jsonld, entityRecord, HttpStatus.ACCEPTED, allowHeaderParams);
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
					.location(UriComponentsBuilder.newInstance().path("/entity/{id}.{format}").buildAndExpand(
							EntityRecordUtils.extractIdentifierFromEntityId(existingEntity.get().getEntityId()),
							FormatTypes.jsonld).toUri())
					.build();
		}
		return null;
	}

	private void launchUpdateTask(List<String> entityIds, boolean runAsynchronously)
      throws Exception {
    logger.info("Launching update task for entityIds={}. async={}", entityIds, runAsynchronously);
    batchService.launchSpecificEntityUpdate(entityIds, runAsynchronously);
  }

	private void launchUpdateMetrics(List<String> entityIds)
			throws Exception {
		logger.info("Launching Update Metrics task for entityIds={}", entityIds);
		batchService.launchEntityMetricsUpdate(entityIds, true);
	}

	/**
	 * Gets the database identifier from an EntityId string
	 */
	private String getDatabaseIdentifier(String entityId) {
		//entity id is "http://data.europeana.eu/{type}/{identifier}"
		return entityId.substring(entityId.lastIndexOf("/") + 1);
	}
}
