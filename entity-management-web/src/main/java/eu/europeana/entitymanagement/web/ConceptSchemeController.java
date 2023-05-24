package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.web.http.HttpHeaders.LINK;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.definitions.WebFields;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.service.ConceptSchemeService;
import io.swagger.annotations.ApiOperation;

@RestController
@Validated
@ConditionalOnWebApplication
public class ConceptSchemeController extends BaseRest {

  // private final SolrService solrService;
  private final ConceptSchemeService emConceptSchemeService;

  public static final String INVALID_UPDATE_REQUEST_MSG =
      "Request must either specify a 'query' param or contain entity identifiers in body";

  @Autowired
  public ConceptSchemeController(
      ConceptSchemeService emConceptSchemeService, EntityManagementConfiguration emConfig) {
    this.emConceptSchemeService = emConceptSchemeService;
    this.emConfig = emConfig;
  }

  @ApiOperation(
      value = "Disable a concept scheme",
      nickname = "disableConceptScheme",
      response = java.lang.Void.class)
  @RequestMapping(
      value = {"/scheme/{identifier}"},
      method = RequestMethod.DELETE,
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> disableConceptScheme(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyWriteAccess(Operations.DELETE, request);

    long numericIdentifier = parseNumericIdentifier(identifier);

    ConceptScheme scheme = emConceptSchemeService.retrieveConceptScheme(numericIdentifier);

    String etag = generateETag(scheme.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());
    checkIfMatchHeader(etag, request);

    emConceptSchemeService.disableConceptScheme(scheme, true);

    return noContentResponse(request);
  }

  long parseNumericIdentifier(String identifier) throws EntityNotFoundException {
    try {
      return Long.parseLong(identifier);
    } catch (NumberFormatException e) {
      throw new EntityNotFoundException(
          "No concept scheme found for the given identifier: ", identifier, e);
    }
  }

  @ApiOperation(
      value = "Create an entity grouping/scheme",
      nickname = "createScheme",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/scheme/",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> createScheme(
      @RequestParam(
              value = CommonApiConstants.QUERY_PARAM_PROFILE,
              required = false,
              defaultValue = CommonApiConstants.PROFILE_MINIMAL)
          String profile,
      @RequestBody ConceptScheme conceptScheme,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.CREATE, request);

    //    validateBodyEntity(conceptScheme, true);

    emConceptSchemeService.createConceptScheme(conceptScheme);

    return generateResponse(request, conceptScheme, HttpStatus.CREATED);
  }

  @ApiOperation(
      value = "Retrieve an entity grouping/scheme",
      nickname = "getConceptSchemeJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {"/scheme/{identifier}.json", "/scheme/{identifier}.jsonld", "/scheme/{identifier}"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getConceptSchemeJsonLd(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(
              value = WebEntityConstants.QUERY_PARAM_PROFILE,
              required = false,
              defaultValue = CommonApiConstants.PROFILE_MINIMAL)
          String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws EuropeanaApiException, HttpException {

    verifyReadAccess(request);

    long numericIdentifier = parseNumericIdentifier(identifier);

    ConceptScheme scheme = emConceptSchemeService.retrieveConceptScheme(numericIdentifier, false);
    return generateResponse(request, scheme, HttpStatus.OK);
  }
  
  @ApiOperation(
      value = "Update concept scheme",
      nickname = "updateConceptScheme",
      response = java.lang.Void.class)
  @PutMapping(
      value = {"/scheme/{identifier}"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> updateConceptScheme(
      @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false, defaultValue = CommonApiConstants.PROFILE_MINIMAL) String profile,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestBody ConceptScheme newConceptScheme,
      HttpServletRequest request)
      throws Exception {

    verifyWriteAccess(Operations.UPDATE, request);

    long numericIdentifier = parseNumericIdentifier(identifier);
    
    ConceptScheme existingScheme = emConceptSchemeService.retrieveConceptScheme(numericIdentifier, false);

    String etag = generateETag(existingScheme.getModified(), FormatTypes.jsonld.name(), getApiVersion());
    checkIfMatchHeader(etag, request);

    existingScheme.setDefinition(new HashMap<>(newConceptScheme.getDefinition()));
    existingScheme.setPrefLabel(new HashMap<>(newConceptScheme.getPrefLabel()));
    existingScheme.setModified(new Date());
//  validateBodyEntity(existingScheme, true);
    
    emConceptSchemeService.storeConceptScheme(existingScheme);
    return generateResponse(request, existingScheme, HttpStatus.OK);
  }
  
  protected ResponseEntity<String> generateResponse(
      HttpServletRequest request, ConceptScheme scheme, HttpStatus status)
      throws EuropeanaApiException {

    org.springframework.http.HttpHeaders headers = generateHeaders(scheme, request);

    try {
      String body = jsonLdSerializer.serializeObject(scheme);
      return ResponseEntity.status(status).headers(headers).body(body);
    } catch (IOException e) {
      throw new EuropeanaApiException("Error serializing concept scheme.", e);
    }
  }

  /**
   * generates response headers
   * @param scheme - object to be serialized
   * @param request - the http request 
   * @return response headers
   */
  org.springframework.http.HttpHeaders generateHeaders(ConceptScheme scheme,
      HttpServletRequest request) {
    String etag = generateETag(scheme.getModified(), WebFields.FORMAT_JSONLD, getApiVersion());

    // create headers
    org.springframework.http.HttpHeaders headers = createAllowHeader(request);
    headers.add(LINK, HttpHeaders.VALUE_LDP_RESOURCE);
    headers.add(EMHttpHeaders.ETAG, etag);
    headers.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);

    if(RequestMethod.POST.toString().equals(request.getMethod())) {
      headers.add(EMHttpHeaders.CACHE_CONTROL, EMHttpHeaders.VALUE_NO_CAHCHE_STORE_REVALIDATE);
    }
    return headers;
  }
}
