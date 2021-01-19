package eu.europeana.entitymanagement.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaApiCredentials;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaAuthenticationToken;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entitymanagement.definitions.formats.FormatTypes;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.RankedEntity;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.vocabulary.UserRoles;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;
import io.swagger.annotations.ApiOperation;

/**
 * Example Rest Controller class with input validation
 */
@RestController
@Validated
public class EMController extends BaseRest {

    @Autowired
    EntityRecordService entityRecordService;

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityJsonLd", response = java.lang.Void.class)
    @RequestMapping(value = {
	    "/entity/{type}/{namespace}/{identifier}.jsonld" }, method = RequestMethod.GET, produces = {
		    HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getJsonLdEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException {
	return createResponse(type, namespace, identifier, FormatTypes.jsonld, null, request);

    }

    @ApiOperation(value = "Retrieve a known entity", nickname = "getEntityXml", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/{type}/{namespace}/{identifier}.xml" }, method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, HttpHeaders.CONTENT_TYPE_RDF_XML,
	    MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> getXmlEntity(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
	    @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
	    HttpServletRequest request) throws HttpException {
	return createResponse(type, namespace, identifier, FormatTypes.xml,
		HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML, request);
    }

    private ResponseEntity<String> createResponse(String type, String namespace, String identifier,
	    FormatTypes outFormat, String contentType, HttpServletRequest request) throws HttpException {
	try {

//	    	verifyReadAccess(request);

	    EntityRecord entity = entityRecordService.retrieveEntityRecordByUri(type, namespace, identifier);

	    String jsonLd = serialize(entity, outFormat);

	    Date timestamp = ((RankedEntity) entity.getEntity()).getTimestamp();
	    Date etagDate = (timestamp != null) ? timestamp : new Date();
	    String etag = generateETag(etagDate, outFormat.name(), getApiVersion());

	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
	    headers.add(HttpHeaders.ETAG, "" + etag);
	    headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
	    if (!outFormat.equals(FormatTypes.schema)) {
		headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
		headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
	    }
	    if (contentType != null && !contentType.isEmpty())
		headers.add(HttpHeaders.CONTENT_TYPE, contentType);

	    ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);
	    return response;
	} catch (RuntimeException e) {
	    // not found ..
	    throw new InternalServerException(e);
	} catch (HttpException e) {
	    // avoid wrapping http exception
	    throw e;
	} catch (Exception e) {
	    throw new InternalServerException(e);
	}
    }

    public Authentication verifyReadAccess(HttpServletRequest request) throws ApplicationAuthenticationException {
	// TODO: reenable authorization
	// return super.verifyReadAccess(request);
	// authentication is temporary disabled
	return null;
    }

    public Authentication verifyWriteAccess(String operation, HttpServletRequest request)
	    throws ApplicationAuthenticationException {
	// TODO: reenable authorization
	// return super.verifyReadAccess(request);
	// authentication is temporary disabled
	EuropeanaApiCredentials europeanaApiCredentials = new EuropeanaApiCredentials(EuropeanaApiCredentials.USER_ANONYMOUS);
	List<GrantedAuthority>authorities = new ArrayList<GrantedAuthority>();
	authorities.add(new SimpleGrantedAuthority(UserRoles.USER.name()));
	return new EuropeanaAuthenticationToken(authorities, "entitymanagement", "apidemo", 
		europeanaApiCredentials);
    }
}
