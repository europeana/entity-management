package eu.europeana.entitymanagement.web;

<<<<<<< HEAD
import java.util.Date;
=======
import javax.validation.constraints.Pattern;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
>>>>>>> branch 'EA-2364-entity-retrieval' of https://github.com/europeana/entity-management.git

<<<<<<< HEAD
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.exception.InternalServerException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entity.web.controller.BaseRest;
import eu.europeana.entitymanagement.definitions.formats.FormatTypes;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.RankedEntity;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

=======
>>>>>>> branch 'EA-2364-entity-retrieval' of https://github.com/europeana/entity-management.git
/**
 * Example Rest Controller class with input validation
 */
@RestController
@Validated
public class EMController extends BaseRest {

    private static final String MY_REGEX = "^[a-zA-Z0-9_]*$";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid parameter.";

    /**
     * Test endpoint
     * @param someRequest an alpha-numerical String
     * @return just something, doesn't matter what
     */
    @GetMapping(value = "/{someRequest}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String handleMyApiRequest(
        @PathVariable(value = "someRequest")
            @Pattern(regexp = MY_REGEX, message = INVALID_REQUEST_MESSAGE) String someRequest) {
        return "It works!";
    }
    
	@ApiOperation(value = "Retrieve a known entity", nickname = "getEntity", response = java.lang.Void.class)
	@RequestMapping(value = {"/entity/{type}/{namespace}/{identifier}.jsonld"}, method = RequestMethod.GET,
			produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> getJsonLdEntity(
			@RequestParam(value = CommonApiConstants.PARAM_WSKEY, required=false) String wskey,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_NAMESPACE) String namespace,
			@PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
			HttpServletRequest request
			) throws HttpException  {
	    return createResponse(type, namespace, identifier, FormatTypes.jsonld, null, request);	
	    
	}

	private ResponseEntity<String> createResponse(String type, String namespace, String identifier, FormatTypes outFormat,  String contentType, HttpServletRequest request) throws HttpException{
	    try {
	    	
	    	verifyReadAccess(request);
        	Entity entity = entityService.retrieveByUrl(type, namespace, identifier);
        	String jsonLd = serialize(entity, outFormat);
        
    	    	Date timestamp = ((RankedEntity)entity).getTimestamp();
    	    	Date etagDate = (timestamp != null)? timestamp : new Date();
    	    	String etag = generateETag(etagDate
    	    		, outFormat.name()
		        , getApiVersion()
		        );
    	    	
    	    	MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
    	    	headers.add(HttpHeaders.ETAG, "" + etag);
    	    	headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
    	    	if(!outFormat.equals(FormatTypes.schema)) {
    	    		headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    	    		headers.add(HttpHeaders.LINK, HttpHeaders.VALUE_LDP_RESOURCE);
    	    	}
    	    	if(contentType != null && !contentType.isEmpty())
    	    	    headers.add(HttpHeaders.CONTENT_TYPE, contentType);
    
//        	System.out.println(jsonLd);
    	    	ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);
        	    	return response;
	    } catch (RuntimeException e) {
	    	//not found .. 
	    	throw new InternalServerException(e);
	    } catch (HttpException e) {
	    	//avoid wrapping http exception
	    	throw e;
	    } catch (Exception e) {
	    	throw new InternalServerException(e);
	    }	
	}

	

}
