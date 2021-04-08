package eu.europeana.entitymanagement.web;

import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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


/**
 * Example Rest Controller class with input validation TODO: catch the
 * exceptions from the used functions and return the adequate response to the
 * user
 */
@RestController
@Validated
@RequestMapping("/entity")
public class EMController extends BaseRest {

  private final EntityRecordService entityRecordService;
  private final MetisDereferenceService dereferenceService;
  private final DataSources datasources;
  private final BatchService batchService;

	private static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has already been removed";
	private static final String EXTERNAL_ID_REMOVED_MSG = "Entity id '%s' already exists as '%s', which has been removed";

  @Autowired
	public EMController(EntityRecordService entityRecordService,
			MetisDereferenceService dereferenceService, DataSources datasources,
			BatchService batchService) {
		this.entityRecordService = entityRecordService;
		this.dereferenceService = dereferenceService;
		this.datasources = datasources;
		this.batchService = batchService;
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

		EntityRecord entityRecord = retrieveEntityRecord(type, identifier.toLowerCase());

		Entity entity = entityRecord.getEntity();
	    Date etagDate = (entity == null || entity.getIsAggregatedBy() == null ? new Date()
		    : entity.getIsAggregatedBy().getModified());
	    String etag = generateETag(etagDate, FormatTypes.jsonld.name(), getApiVersion());

	    checkIfMatchHeader(etag, request);

	    entityRecordService.disableEntityRecord(entityRecord);

	    return ResponseEntity.noContent().build();
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
	    @RequestBody EntityPreview entityCreationRequest,
	    HttpServletRequest request) throws Exception {

    	// TODO: Re-enable authentication
    	// verifyReadAccess(request);

		 EntityRecord entityRecord = retrieveEntityRecord(type, identifier);

		 Date timestamp = (entityRecord.getEntity().getIsAggregatedBy() != null) ? entityRecord.getEntity().getIsAggregatedBy().getModified() : null;
			Date etagDate = (timestamp != null)? timestamp : new Date();
			String etag = generateETag(etagDate, FormatTypes.jsonld.name(), getApiVersion());

			try {
				checkIfMatchHeader(etag, request);
			} catch (HttpException e) {
				throw new EtagMismatchException("If-Match header value does not match generated ETag for entity");
			}

			if(entityCreationRequest.getId()!=null) {
				entityRecord.getEuropeanaProxy().getEntity().setEntityId(entityCreationRequest.getId());
			}
			if(entityCreationRequest.getAltLabel()!=null) {
				entityRecord.getEuropeanaProxy().getEntity().setAltLabel(entityCreationRequest.getAltLabel());
			}
    		if(entityCreationRequest.getDepiction()!=null) {
					entityRecord.getEuropeanaProxy().getEntity().setDepiction(entityCreationRequest.getDepiction());
    		}
    		if(entityCreationRequest.getPrefLabel()!=null) {
					entityRecord.getEuropeanaProxy().getEntity().setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
    		}

    		Date modificationDate = new Date();
    		if (entityRecord.getEuropeanaProxy().getProxyIn()!=null) {
					entityRecord.getEuropeanaProxy().getProxyIn().setModified(modificationDate);
    		}

    		entityRecordService.update(entityRecord);
				return launchTaskAndRetrieveEntity(type, identifier, entityRecord, profile);
    }


	@ApiOperation(value = "Update an entity from external data source", nickname = "updateEntityFromDatasource", response = java.lang.Void.class)
	@PostMapping(value = "/{type}/{identifier}/management/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateFromExternalSource(
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "internal") String profile
	) throws Exception {
		EntityRecord entityRecord = retrieveEntityRecord(type, identifier);

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
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier)
				throws EuropeanaApiException {
	return createResponse(profile, type, identifier, FormatTypes.jsonld, null);

    }
    
    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}","/{type}/{identifier}" }, 
    method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getEntity(
    	@RequestHeader(value = "Accept-header", defaultValue = "application/json") String acceptHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws EuropeanaApiException {
    	
    	if (acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
    		return createResponse(profile, type, identifier, FormatTypes.jsonld, null);
    	}
    	else if (acceptHeader.contains(MediaType.APPLICATION_XML_VALUE)) {
    		return createResponse(profile, type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
    	}
    	else {    		
		return ResponseEntity.badRequest().header("info:", "Please specify the Accept-header value (application/json or application/xml)").build();
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
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier)
				throws EuropeanaApiException {
	return createResponse(profile, type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML);
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<String> registerEntity(
				@RequestBody EntityPreview entityCreationRequest)
				throws Exception {
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
				logger.debug("Entity registration id={} - no matching datasource configured",
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

			EntityRecord savedEntityRecord = entityRecordService
					.createEntityFromRequest(entityCreationRequest,
							metisResponse);

			launchUpdateTask(savedEntityRecord.getEntityId(), true);
			return ResponseEntity.accepted().body(jsonLdSerializer.serialize(savedEntityRecord,
					EntityProfile.internal));
		}


    private ResponseEntity<String> createResponse(String profile, String type, String identifier, FormatTypes outFormat,
	    String contentType) throws EuropeanaApiException {
	// TODO: Re-enable authentication
	// verifyReadAccess(request);

	MultiValueMap<String, String> headers;

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

			EntityRecord entityRecord = retrieveEntityRecord(type, identifier);

			Date etagDate = (entityRecord.getEntity() == null || entityRecord.getEntity().getIsAggregatedBy() == null
		? new Date()
		: entityRecord.getEntity().getIsAggregatedBy().getModified());
	String etag = generateETag(etagDate, outFormat.name(), getApiVersion());

	headers = new LinkedMultiValueMap<String, String>(5);
	headers.add(HttpHeaders.ETAG, "" + etag);
	headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
	if (!outFormat.equals(FormatTypes.schema)) {
	    headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	    headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
	}
	if (contentType != null && !contentType.isEmpty())
	    headers.add(HttpHeaders.CONTENT_TYPE, contentType);

	String body = serialize(entityRecord, outFormat, profile);

			return new ResponseEntity<>(body, headers, HttpStatus.OK);
  }

	private EntityRecord retrieveEntityRecord(String type, String identifier)
			throws EuropeanaApiException {
		String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
		Optional<EntityRecord> entityRecordOptional = entityRecordService
				.retrieveEntityRecordByUri(entityUri);
		if (entityRecordOptional.isEmpty()) {
			throw new EntityNotFoundException(entityUri);
		}

		EntityRecord entityRecord = entityRecordOptional.get();
		if (entityRecord.isDisabled()) {
			throw new EntityRemovedException(String.format(ENTITY_ID_REMOVED_MSG, entityUri));
		}
		return entityRecord;
	}

	private ResponseEntity<String> launchTaskAndRetrieveEntity(String type, String identifier,
			EntityRecord entityRecord, String profile) throws Exception {
		// launch synchronous update, then retrieve entity from DB afterwards
		launchUpdateTask(entityRecord.getEntityId(), false);
		entityRecord = retrieveEntityRecord(type, identifier);

		return ResponseEntity.accepted().body(jsonLdSerializer.serialize(entityRecord, profile));
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
}
