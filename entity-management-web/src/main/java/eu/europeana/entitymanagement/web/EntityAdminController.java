package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledRemovalType;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import io.swagger.annotations.ApiOperation;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/entity")
public class EntityAdminController extends BaseRest {

  private static final Logger LOG = LogManager.getLogger(EntityAdminController.class);

  private final EntityRecordService entityRecordService;
  private final SolrService solrService;
  private final EntityUpdateService entityUpdateService;
  private final EntityManagementConfiguration emConfig;

  @Autowired
  public EntityAdminController(
      EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService,
      SolrService solrService,
      EntityManagementConfiguration emConfig) {
    this.entityRecordService = entityRecordService;
    this.entityUpdateService = entityUpdateService;
    this.solrService = solrService;
    this.emConfig = emConfig;
  }

  @ApiOperation(value = "Permanent Deletion of Entity", nickname = "deleteEntity")
  @DeleteMapping(
      value = "/{type}/{identifier}/management",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> deleteEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestParam(value = WebEntityConstants.QUERY_PARAM_PROFILE, required = false)
          String profile,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.DELETE, request);
    }
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    if (!entityRecordService.existsByEntityId(entityUri)) {
      throw new EntityNotFoundException(entityUri);
    }

    boolean isSynchronous = WebEntityConstants.PARAM_PROFILE_SYNC.equals(profile);

    LOG.info("Permanently deleting entityId={}, isSynchronous={}", entityUri, isSynchronous);

    if (isSynchronous) {
      // delete from Solr before Mongo, so Solr errors won't leave DB in an inconsistent state
      solrService.deleteById(List.of(entityUri));
      entityRecordService.delete(entityUri);
    } else {
      entityUpdateService.scheduleTasks(
          Collections.singletonList(entityUri), ScheduledRemovalType.PERMANENT_DELETION);
    }

    return noContentResponse(request);
  }

  /**
   * Migrate existing Entity
   *
   * @param wskey
   * @param type type of entity
   * @param identifier entity id
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(
      value = "Migrate existing Entity",
      nickname = "migrateExistingEntity",
      response = java.lang.Void.class)
  @PostMapping(
      value = "/{type}/{identifier}/management",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> migrateExistingEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      @RequestBody EntityPreview entityCreationRequest,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {
    if (emConfig.isAuthEnabled()) {
      // TODO later change the verification method once DB migration is done
      // verifyWriteAccess(Operations.CREATE, request);
      verifyMigrationAccess(request);
    }
    try {
      // get the entity type based on path param
      type = EntityTypes.getByEntityType(type).getEntityType();
    } catch (UnsupportedEntityTypeException e) {
      throw new EntityCreationException("Entity type invalid or not supported: " + type, e);
    }

    EntityRecord savedEntityRecord =
        entityRecordService.createEntityFromMigrationRequest(
            entityCreationRequest, type, identifier);
    LOG.info(
        "Created Entity record for {}; entityId={}",
        entityCreationRequest.getId(),
        savedEntityRecord.getEntityId());
    return generateResponseEntity(
        request,
        List.of(EntityProfile.internal),
        FormatTypes.jsonld,
        null,
        null,
        savedEntityRecord,
        HttpStatus.ACCEPTED);
  }

  @ApiOperation(
      value = "Retrieve a list of entities for which an update failed.",
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
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {

    if (emConfig.isAuthEnabled()) {
      verifyReadAccess(request);
    }

    if (pageSize > 1000) {
      pageSize = 1000;
    }

    List<String> entityIds = failedTaskService.getEntityIdsWithFailures(page * pageSize, pageSize);

    return generateResponseFailedUpdates(request, entityIds, wskey);
  }

  /**
   * Method will authenticate Migration requests. This is done to avoid the expiration of tokens
   *
   * <p>TODO remove after DB migration
   *
   * @param request
   * @throws ApplicationAuthenticationException
   */
  private void verifyMigrationAccess(HttpServletRequest request)
      throws ApplicationAuthenticationException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasLength(authorization) || !authorization.startsWith("Bearer")) {
      throw new ApplicationAuthenticationException(
          "User is not authorised to perform this action", null);
    }

    // Authorization header is "Bearer <token_value>"
    if (!authorization.substring(7).equals(emConfig.getEnrichmentsMigrationPassword())) {
      throw new ApplicationAuthenticationException(
          "Invalid token for migrating existing entity", null, null, HttpStatus.FORBIDDEN);
    }
  }
}
