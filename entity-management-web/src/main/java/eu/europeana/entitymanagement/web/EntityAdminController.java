package eu.europeana.entitymanagement.web;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.definitions.exception.DateParsingException;
import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.definitions.batch.model.FailedTask;
import eu.europeana.entitymanagement.definitions.batch.model.TaskType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.auth.EMOperations;
import eu.europeana.entitymanagement.web.model.ZohoSyncReport;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.service.EntitySynchronizationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Validated
@RequestMapping("/entity")
@ConditionalOnWebApplication
public class EntityAdminController extends BaseRest {

  private static final Logger LOG = LogManager.getLogger(EntityAdminController.class);

  private final EntityRecordService entityRecordService;
  private final EntitySynchronizationService entitySyncService;

  @Autowired
  public EntityAdminController(
      EntityRecordService entityRecordService,
      EntitySynchronizationService entitySyncService) {
    this.entityRecordService = entityRecordService;
    this.entitySyncService = entitySyncService;
  }

  @ApiOperation(value = "Permanent Deletion of Entity", nickname = "deleteEntity")
  @DeleteMapping(
      value = "/{type}/{identifier}/management",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> deleteEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false) String profile,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyWriteAccess(Operations.DELETE, request);

    String entityUri = null;
    try {
      entityUri = EntityRecordUtils.buildEntityIdUri(EntityTypes.getByEntityType(type), identifier);
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityNotFoundException("/" + type + "/" + identifier, e);
    }

    if (!entityRecordService.existsByEntityId(entityUri)) {
      throw new EntityNotFoundException(entityUri);
    }

    LOG.debug("Permanently deleting entityId={} synchronously", entityUri);
    entityRecordService.delete(entityUri);
    return noContentResponse(request);
  }

  @ApiOperation(
      value = "Retrieve a list of entities for which an update failed. taskType is one of: full_update, metrics_update, registration",
      nickname = "getEntitiesUpdateFailedJsonLd",
      response = java.lang.Void.class)
  @GetMapping(
      value = {"/management/failed"},
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getEntitiesUpdateFailedJsonLd(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(
              value = WebEntityConstants.QUERY_PARAM_PAGE,
              required = false,
              defaultValue = "0")
          int page,
      @RequestParam(
              value = WebEntityConstants.QUERY_PARAM_PAGE_SIZE,
              required = false,
              defaultValue = "10")
          int pageSize,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_TASK_TYPE, required = false) 
          TaskType taskType,        
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyReadAccess(request);

    if (pageSize > 1000) {
      pageSize = 1000;
    }

    List<String> entityIds = failedTaskService.getEntityIdsWithFailures(taskType, page * pageSize, pageSize);

    return generateResponseFailedUpdates(request, entityIds, wskey);
  }
  
  
  @ApiOperation(
      value = "Retrieve a FailedTask by entity id",
      nickname = "getFailedTask",
      response = java.lang.Void.class)
  @GetMapping(
      value = {"/management/failedtask"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getFailedTask(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_URI, required = false) 
          String uri,        
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyReadAccess(request);
    Optional<FailedTask> failedTaskOptional = failedTaskService.getFailure(uri);

    return generateFailedTaskResponse(failedTaskOptional, request);
  }

  
  protected ResponseEntity<String> generateFailedTaskResponse(
      Optional<FailedTask> failedTaskOptional, HttpServletRequest request) throws EuropeanaApiException {

    org.springframework.http.HttpHeaders headers = createAllowHeader(request);
    //headers.add(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8);

    if(failedTaskOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    try {
      String body = jsonLdSerializer.serializeObject(failedTaskOptional.get());
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(body);
    } catch (IOException e) {
      throw new EuropeanaApiException("Error serializing failed task", e);
    }
  }
  
  
  /**
   * Synchronize Organizations from Zoho
   *
   * @param type type of entity
   * @param identifier entity id
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(
      value = "Synchronize Organizations from Zoho",
      nickname = "zohoSync",
      response = java.lang.Void.class)
  @PostMapping(value = "/management/zohosync", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> zohoSync(
      @ApiParam(
              name = WebEntityConstants.SINCE,
              required = true,
              format = "ISO DateTime",
              example = "1970-01-01T00:00:00Z")
          @RequestParam
          String since,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    verifyWriteAccess(EMOperations.OPERATION_ZOHO_SYNC, request);

    OffsetDateTime modifiedSince = validateSince(since);
    ZohoSyncReport zohoSyncReport = entitySyncService.synchronizeZohoOrganizations(modifiedSince);

    return generateZohoSyncResponse(request, zohoSyncReport);
  }

  private OffsetDateTime validateSince(String since) throws HttpBadRequestException {
    if (since == null) {
      return Instant.EPOCH.atOffset(ZoneOffset.UTC);
    }

    try {
      return DateUtils.parseToOffsetDateTime(since);
    } catch (DateParsingException e) {
      throw new HttpBadRequestException(
          "Request param 'since' is not an ISO DateTime: " + since, e);
    }
  }
}
