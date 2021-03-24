package eu.europeana.entitymanagement.web;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordUtils;
import eu.europeana.entitymanagement.web.service.impl.MetisDereferenceService;
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

    @Resource(name = AppConfig.BEAN_ENTITY_RECORD_SERVICE)
    private EntityRecordService entityRecordService;

    @Resource(name = AppConfig.BEAN_METIS_DEREF_SERVICE)
    private MetisDereferenceService dereferenceService;

    @Resource(name = AppConfig.BEAN_EM_DATA_SOURCES)
    private DataSources datasources;

    @Resource(name = AppConfig.BEAN_EM_CONFIGURATION)
    EntityManagementConfiguration emConfiguration;

    @ApiOperation(value = "Disable an entity", nickname = "disableEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}",
	    "/{type}/{identifier}" }, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> disableEntity(
	    @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException {

	String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier.toLowerCase());
	Optional<EntityRecord> entityRecord = entityRecordService.retrieveEntityRecordByUri(entityUri);
	if (entityRecord.isPresent() && !entityRecord.get().getDisabled()) {

	    Entity entity = entityRecord.get().getEntity();
	    Date etagDate = (entity == null || entity.getIsAggregatedBy() == null ? new Date()
		    : entity.getIsAggregatedBy().getModified());
	    String etag = generateETag(etagDate, FormatTypes.jsonld.name(), getApiVersion());

	    checkIfMatchHeader(etag, request);

	    /*
	     * TODO: disable the record in the index, too
	     */
	    entityRecordService.disableEntityRecord(entityRecord.get());

	    return ResponseEntity.noContent().header("info:", "The entity was disabled successfully.").build();
	} else if (entityRecord.isPresent() && entityRecord.get().getDisabled()) {
	    return ResponseEntity.status(HttpStatus.GONE).header("info:", "The entity is already disabled.").build();
	} else {
	    return ResponseEntity.notFound()
		    .header("info:", "There is no entity with the given identifier to be disabled.").build();
	}

    }

    @ApiOperation(value = "Update an entity", nickname = "updateEntity", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/base/{identifier}", "/{type}/{identifier}" },method = RequestMethod.PUT, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<EntityRecord> updateEntity(
    	@RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = true, defaultValue = "external") String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    @RequestBody EntityPreview entityCreationRequest,
	    HttpServletRequest request) {

    	// TODO: Re-enable authentication
    	// verifyReadAccess(request);

    	Optional<EntityRecord> existingRecord = entityRecordService.retrieveEntityRecordByUri(EntityRecordUtils.buildEntityIdUri(type, identifier));
    	if(existingRecord.isPresent() && existingRecord.get().getEuropeanaProxy()!=null) {

    		Date timestamp = (existingRecord.get().getEntity().getIsAggregatedBy() != null) ? existingRecord.get().getEntity().getIsAggregatedBy().getModified() : null;
			Date etagDate = (timestamp != null)? timestamp : new Date();
			String etag = generateETag(etagDate, FormatTypes.jsonld.name(), getApiVersion());

			try {
				checkIfMatchHeader(etag, request);
			} catch (HttpException e) {
				return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).header("info:", "The value of the If-Match HTTP header does not allign with the given ETag value generated from the timestamp.").build();
			}

			//TODO: call the UpdateTask for updating the entity if it is meant to be used for that
			if(entityCreationRequest.getId()!=null) {
				existingRecord.get().getEuropeanaProxy().getEntity().setEntityId(entityCreationRequest.getId());
			}
			if(entityCreationRequest.getAltLabel()!=null) {
				existingRecord.get().getEuropeanaProxy().getEntity().setAltLabel(entityCreationRequest.getAltLabel());
			}
    		if(entityCreationRequest.getDepiction()!=null) {
    			existingRecord.get().getEuropeanaProxy().getEntity().setDepiction(entityCreationRequest.getDepiction());
    		}
    		if(entityCreationRequest.getPrefLabel()!=null) {
    			existingRecord.get().getEuropeanaProxy().getEntity().setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
    		}

    		Date modificationDate = new Date();
    		if (existingRecord.get().getEuropeanaProxy().getProxyIn()!=null) {
    			existingRecord.get().getEuropeanaProxy().getProxyIn().setModified(modificationDate);
    		}

    		entityRecordService.update(existingRecord.get());

    		return ResponseEntity.accepted().body(existingRecord.get());
    	}

    	return ResponseEntity.notFound().header("info:", "The given entity record does not exist.").build();

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
	    HttpServletRequest request) {
	return createResponse(profile, type, identifier, FormatTypes.jsonld, null, request);

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
	    HttpServletRequest request) {
	return createResponse(profile, type, identifier, FormatTypes.xml, HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML,
		request);
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityRecord> registerEntity(@RequestBody EntityPreview entityCreationRequest)
	    throws EuropeanaApiException {
	// check if id is already being used, if so return a 301
	Optional<EntityRecord> existingEntity = entityRecordService
		.findMatchingCoreference(entityCreationRequest.getId());
	if (existingEntity.isPresent()) {
	    // return 301 redirect
	    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
		    .location(UriComponentsBuilder.newInstance().path("/{id}.{format}").buildAndExpand(
			    EntityRecordUtils.extractIdentifierFromEntityId(existingEntity.get().getEntityId()), FormatTypes.jsonld).toUri())
		    .build();
	}

	// return 400 error if ID does not match a configured datasource
	if (!datasources.hasDataSource(entityCreationRequest.getId())) {
	    return ResponseEntity.badRequest().build();
	}

	Entity metisResponse = dereferenceService.dereferenceEntityById(entityCreationRequest.getId());
	if (metisResponse.getSameAs() != null) {
		existingEntity = entityRecordService.retrieveMetisCoreferenceSameAs(metisResponse.getSameAs());
	}

	if (existingEntity.isPresent()) {
	    // return 301 redirect
	    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
		    .location(UriComponentsBuilder.newInstance().path("/{id}.{format}").buildAndExpand(
			    EntityRecordUtils.extractIdentifierFromEntityId(existingEntity.get().getEntityId()), FormatTypes.jsonld).toUri())
		    .build();

	}

	EntityRecord savedEntity = entityRecordService.createEntityFromRequest(entityCreationRequest,
		metisResponse.getType());
	return ResponseEntity.accepted().body(savedEntity);
    }
    
  
    private ResponseEntity<String> createResponse(String profile, String type, String identifier, FormatTypes outFormat,
	    String contentType, HttpServletRequest request) {
	// TODO: Re-enable authentication
	// verifyReadAccess(request);

	MultiValueMap<String, String> headers;
	ResponseEntity<String> response;

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
	    return ResponseEntity.badRequest().header("info:", "The profile parameter is invalid.").build();
	}

	String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
	Optional<EntityRecord> entityRecordOptional = entityRecordService.retrieveEntityRecordByUri(entityUri);
	if (entityRecordOptional.isEmpty()) {
	    return ResponseEntity.notFound().header("info:", "The entity with the required parameters does not exist.")
		    .build();
	}

	EntityRecord entityRecord = entityRecordOptional.get();

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

	response = new ResponseEntity<String>(body, headers, HttpStatus.OK);
	return response;
    }
   
}
