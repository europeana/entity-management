package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.EtagMismatchException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import io.swagger.annotations.ApiOperation;


@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final MetisDereferenceService dereferenceService;
  private final DataSources datasources;
  private final BatchService batchService;
  private final EntityManagementConfiguration emConfig;

	private static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has been removed";
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
	}

	@ApiOperation(value = "Disable an entity", nickname = "disableEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}",
	    "/{type}/{identifier}" }, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> disableEntity(
	    @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException, EuropeanaApiException {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.DELETE, request);
		}
		EntityRecord entityRecord = retrieveEntityRecord(type, identifier.toLowerCase(), false);

		Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
		long timestamp = isAggregatedBy != null ?
				isAggregatedBy.getModified().getTime() :
				0L;

	    String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());
	    checkIfMatchHeader(etag, request);
	    entityRecordService.disableEntityRecord(entityRecord);
	    return ResponseEntity.noContent().build();
    }

	@ApiOperation(value = "Re-enable an entity", nickname = "enableEntity", response = java.lang.Void.class)
	@RequestMapping(value = { "/{type}/base/{identifier}",
			"/{type}/{identifier}" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> enableEntity(
			@RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			HttpServletRequest request) throws HttpException, EuropeanaApiException {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = retrieveEntityRecord(type, identifier.toLowerCase(), true);
		if (!entityRecord.isDisabled()) {
			return createResponse(profile, type, identifier, FormatTypes.jsonld, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
		}
		logger.debug("Re-enabling entity : {}/{}", type, identifier);
		entityRecordService.enableEntityRecord(entityRecord);
		return createResponse(profile, type, identifier, FormatTypes.jsonld, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);
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
		 EntityRecord entityRecord = retrieveEntityRecord(type, identifier, false);

			Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();
			long timestamp = isAggregatedBy != null ?
					isAggregatedBy.getModified().getTime() :
					0L;

		 String etag = computeEtag(timestamp, FormatTypes.jsonld.name(), getApiVersion());

			try {
				checkIfMatchHeader(etag, request);
			} catch (HttpException e) {
				throw new EtagMismatchException("If-Match header value does not match generated ETag for entity");
			}

			entityRecordService.updateEuropeanaProxy(updateRequestEntity, entityRecord);
			entityRecordService.update(entityRecord);
			return launchTaskAndRetrieveEntity(type, identifier, entityRecord, profile);
    }


	@ApiOperation(value = "Update an entity from external data source", nickname = "updateEntityFromDatasource", response = java.lang.Void.class)
	@PostMapping(value = "/{type}/{identifier}/management/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateFromExternalSource(
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile,
			HttpServletRequest request
	) throws Exception {
		if (emConfig.isAuthEnabled()) {
			verifyWriteAccess(Operations.UPDATE, request);
		}
		EntityRecord entityRecord = retrieveEntityRecord(type, identifier, false);
		return launchTaskAndRetrieveEntity(type, identifier, entityRecord, profile);
	}



	@ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}.jsonld",
	    "/{type}/{identifier}.jsonld" }, method = RequestMethod.GET, produces = { HttpHeaders.CONTENT_TYPE_JSONLD,
		    MediaType.APPLICATION_JSON_VALUE })
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
	return createResponse(profile, type, identifier, FormatTypes.jsonld, null);

    }
    
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}","/{type}/{identifier}" }, 
    method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getXmlEntity(
    	@RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = "application/json") String acceptHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws EuropeanaApiException, HttpException{
			if (emConfig.isAuthEnabled()) {
				verifyReadAccess(request);
			}
	logger.debug("Retrieve entity with content negotiation:{}/{}, with accept header {}", type, identifier, acceptHeader);
	if (acceptHeader.contains(HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML)) {
	    //if rdf/XML is explicitly requested
	    return createResponse(profile, type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
	}
	if (acceptHeader.contains(MediaType.APPLICATION_XML_VALUE)) {
	    //if XML is explicitly requested
	    return createResponse(profile, type, identifier, FormatTypes.xml, MediaType.APPLICATION_XML_VALUE);
	} else {	
	    //otherwise return default
	    return createResponse(profile, type, identifier, FormatTypes.jsonld, null);
	}
    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}.xml",
	    "/{type}/{identifier}.xml" }, method = RequestMethod.GET, produces = {
		    HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML,
		    MediaType.APPLICATION_XML_VALUE })
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
	return createResponse(profile, type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<String> registerEntity(
				@RequestBody EntityPreview entityCreationRequest, HttpServletRequest request)
				throws Exception {
			if (emConfig.isAuthEnabled()) {
				verifyWriteAccess(Operations.CREATE, request);
			}
			logger.debug("Registering new entity: {}", entityCreationRequest.getId());
	
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
				logger.debug("Entity registration: {} - no matching datasource configured",
						entityCreationRequest.getId());
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

			logger.debug("Saving record for {}", entityCreationRequest.getId());

			EntityRecord savedEntityRecord = entityRecordService
					.createEntityFromRequest(entityCreationRequest, metisResponse);

			logger.debug("Created Entity record for {}; entityId={}", entityCreationRequest.getId(), savedEntityRecord.getEntityId());

			return launchTaskAndRetrieveEntity(savedEntityRecord.getEntity().getType(),
					getDatabaseIdentifier(savedEntityRecord.getEntityId()), savedEntityRecord,
					EntityProfile.internal.toString());
		}


    private ResponseEntity<String> createResponse(String profile, String type, String identifier, FormatTypes outFormat,
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

			EntityRecord entityRecord = retrieveEntityRecord(type, identifier,false);
			logger.debug("Entity retrieved entityId={}, using {} format", entityRecord.getEntityId(), outFormat);
			return generateResponseEntity(profile, outFormat, contentType, entityRecord, HttpStatus.OK);
		}

	private ResponseEntity<String> generateResponseEntity(String profile, FormatTypes outFormat,
			String contentType, EntityRecord entityRecord, HttpStatus status) {

		Aggregation isAggregatedBy = entityRecord.getEntity().getIsAggregatedBy();

		long timestamp = isAggregatedBy != null ?
				isAggregatedBy.getModified().getTime() :
				0L;

		String etag = computeEtag(timestamp, outFormat.name(), getApiVersion());

		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
		if (!outFormat.equals(FormatTypes.schema)) {
			headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
			headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
		}
		if (contentType != null && !contentType.isEmpty())
			headers.add(HttpHeaders.CONTENT_TYPE, contentType);

		String body = serialize(entityRecord, outFormat, profile);
		return ResponseEntity.status(status).headers(headers).eTag(etag).body(body);
	}

	private EntityRecord retrieveEntityRecord(String type, String identifier, boolean enableEntity)
			throws EuropeanaApiException {
		String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
		Optional<EntityRecord> entityRecordOptional = entityRecordService
				.retrieveEntityRecordByUri(entityUri);
		if (entityRecordOptional.isEmpty()) {
			throw new EntityNotFoundException(entityUri);
		}

		EntityRecord entityRecord = entityRecordOptional.get();
		if (entityRecord.isDisabled() && !enableEntity) {
			throw new EntityRemovedException(String.format(ENTITY_ID_REMOVED_MSG, entityUri));
		}
		return entityRecord;
	}

	private ResponseEntity<String> launchTaskAndRetrieveEntity(String type, String identifier,
			EntityRecord entityRecord, String profile) throws Exception {
		// launch synchronous update, then retrieve entity from DB afterwards
		launchUpdateTask(entityRecord.getEntityId(), false);
		entityRecord = retrieveEntityRecord(type, identifier, false);

		return generateResponseEntity(profile, FormatTypes.jsonld, null, entityRecord, HttpStatus.ACCEPTED);
	}

	private ResponseEntity<String> checkExistingEntity(Optional<EntityRecord> existingEntity,
			String entityCreationId)
			throws EntityRemovedException {

		if (existingEntity.isPresent()) {
			logger.debug("Entity registration id={} - matching coreference found; entityId={}",
					entityCreationId, existingEntity.get().getEntityId());
			if (existingEntity.get().isDisabled()) {
				logger.debug("Entity registration - entityId={} is disabled",
						existingEntity.get().getEntityId());
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

	private void launchUpdateTask(String entityUri, boolean runAsynchronously)
      throws Exception {
    logger.info("Launching update task for entityId={}. async={}", entityUri, runAsynchronously);
    batchService.launchSingleEntityUpdate(entityUri, runAsynchronously);
  }

	/**
	 * Gets the database identifier from an EntityId string
	 */
	private String getDatabaseIdentifier(String entityId) {
		//entity id is "http://data.europeana.eu/{type}/{identifier}"
		return entityId.substring(entityId.lastIndexOf("/") + 1);
	}

	/**
	 * Generates a unique hex string based on the input params
	 * TODO: move logic to {@link eu.europeana.api.commons.web.controller.BaseRestController#generateETag(Date, String, String)}
	 */
	private String computeEtag(long timestamp, String format, String version){
		return DigestUtils.md5Hex(String.format("%s:%s:%s", timestamp, format, version));
	}
}
