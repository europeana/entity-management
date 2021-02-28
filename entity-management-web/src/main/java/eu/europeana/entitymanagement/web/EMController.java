package eu.europeana.entitymanagement.web;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
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

    public static final String BASE_URI_DATA = "http://data.europeana.eu/";

    @Resource(name = AppConfig.BEAN_ENTITY_RECORD_SERVICE)
    private EntityRecordService entityRecordService;

    @Resource(name = AppConfig.BEAN_METIS_DEREF_SERVICE)
    private MetisDereferenceService dereferenceService;

    @Resource(name = AppConfig.BEAN_EM_DATA_SOURCES)
    private DataSources datasources;

    @ApiOperation(value = "Disable an entity", nickname = "disableEntity", response = java.lang.Void.class)
    @RequestMapping(value = "/{type}/{namespace}/{identifier}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> disableEntity(
    	@RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException {
    	
    	String entityUri = getEntityUri(type, namespace, identifier.toLowerCase());
    	Optional<EntityRecord> existingEntity = entityRecordService.retrieveEntityRecordByUri(entityUri);
    	if (existingEntity.isPresent() && !existingEntity.get().getDisabled()) {
    		
			Date timestamp = (existingEntity.get().getEntity().getIsAggregatedBy() != null) ? existingEntity.get().getEntity().getIsAggregatedBy().getModified() : null;
			Date etagDate = (timestamp != null)? timestamp : new Date();
			String etag = generateETag(etagDate, FormatTypes.jsonld.name(), getApiVersion());
			
			checkIfMatchHeader(etag, request);
			
			/*
			 * TODO: disable the record in the index, too
			 */
			entityRecordService.disableEntityRecord(existingEntity.get());
			
		    return ResponseEntity.status(HttpStatus.ACCEPTED).header("info:", "The request is executed successfully.").build();
    	}
    	else if (existingEntity.isPresent() && existingEntity.get().getDisabled()){
    		return ResponseEntity.status(HttpStatus.ACCEPTED).header("info:", "The entity is already disabled.").build();
    	}
    	else {
    		return ResponseEntity.notFound().header("info:", "There is no entity with the given identifier to be disabled.").build();
    	}

    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/{namespace}/{identifier}.jsonld" }, method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getJsonLdEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = true) String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) {
	return createResponse(profile, type, namespace, identifier, FormatTypes.jsonld, null, request);

    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml", response = java.lang.Void.class)
    @RequestMapping(value = { "/{type}/{namespace}/{identifier}.xml" }, method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML,
	    MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getXmlEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = true) String profile,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) {
	return createResponse(profile, type, namespace, identifier, FormatTypes.xml,
		HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, request);
    }

    @ApiOperation(value = "Register a new entity", nickname = "registerEntity", response = java.lang.Void.class)
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityRecord> registerEntity(@RequestBody EntityPreview entityCreationRequest) {
	// check if id is already being used, if so return a 301
	Optional<EntityRecord> existingEntity = entityRecordService
		.retrieveEntityRecordByUri(entityCreationRequest.getId());
	if (existingEntity.isPresent()) {
	    // return 301 redirect
	    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
		    .location(UriComponentsBuilder.newInstance().path("/{id}.{format}").buildAndExpand(
			    extractBaseUriFromEntityId(existingEntity.get().getEntityId()), FormatTypes.jsonld).toUri())
		    .build();
	}

	// return 400 error if ID does not match a configured datasource
	if (!datasources.hasDataSource(entityCreationRequest.getId())) {
	    return ResponseEntity.badRequest().build();
	}

	/*
	 * deferefence using Europeana TODO: call the Europeana service to get the
	 * entity
	 */
//        BaseEntity europeanaResponse = null;
	// dereference using Metis. return HTTP 400 for HTTP4XX responses and HTTP 504
	// for other error responses
	Entity metisResponse;
	try {
	    metisResponse = dereferenceService.dereferenceEntityById(entityCreationRequest.getId());
	} catch (HttpBadRequestException e) {
	    return ResponseEntity.badRequest().header("info:", e.getMessage()).build();
	}catch (EntityCreationException e) {
	    // TODO: change to appropriate error message, sitch to the use of GlobalExceptionHandler
	    return ResponseEntity.badRequest().header("info:", e.getMessage()).build();
	    
	}

	existingEntity = entityRecordService.retrieveMetisCoreferenceSameAs(metisResponse.getSameAs());

	if (existingEntity.isPresent()) {
	    // return 301 redirect
	    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
		    .location(UriComponentsBuilder.newInstance().path("/{id}.{format}").buildAndExpand(
			    extractBaseUriFromEntityId(existingEntity.get().getEntityId()), FormatTypes.jsonld).toUri())
		    .build();

	}

	EntityRecord savedEntity;
	try {
	    savedEntity = entityRecordService.createEntityFromRequest(entityCreationRequest, metisResponse);
	} catch (EntityCreationException e) {
	    // TODO: maybe here return the unprocessable entity instead of bad request
	    return ResponseEntity.badRequest().header("info:", e.getMessage()).build();
	}
	return ResponseEntity.accepted().body(savedEntity);
    }

    private ResponseEntity<String> createResponse(String profile, String type, String namespace, String identifier,
	    FormatTypes outFormat, String contentType, HttpServletRequest request) {
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

	String entityUri = getEntityUri(type, namespace, identifier);
	Optional<EntityRecord> entityRecordOptional = entityRecordService.retrieveEntityRecordByUri(entityUri);
	if (entityRecordOptional.isEmpty()) {
	    return ResponseEntity.notFound().header("info:", "The entity with the required parameters does not exist.")
		    .build();
	}

	EntityRecord entityRecord = entityRecordOptional.get();

	Date timestamp = (entityRecord != null) ? entityRecord.getEntity().getIsAggregatedBy().getModified() : null;
	Date etagDate = (timestamp != null) ? timestamp : new Date();
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

    private String getEntityUri(String type, String namespace, String identifier) {
	StringBuilder stringBuilder = new StringBuilder();

	stringBuilder.append(BASE_URI_DATA);
	if (StringUtils.isNotEmpty(type))
	    stringBuilder.append(type.toLowerCase()).append("/");
	if (StringUtils.isNotEmpty(namespace))
	    stringBuilder.append(namespace.toLowerCase()).append("/");
	if (StringUtils.isNotEmpty(identifier))
	    stringBuilder.append(identifier);

	return stringBuilder.toString();
    }

    private String extractBaseUriFromEntityId(String entityId) {
	return entityId.replace(BASE_URI_DATA, "");
    }
}
