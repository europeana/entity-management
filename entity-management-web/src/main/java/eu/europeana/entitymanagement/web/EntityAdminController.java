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
import java.util.Optional;
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
  private final EntityUpdateService entityUpdateService;
  private final EntityManagementConfiguration emConfig;

  @Autowired
  public EntityAdminController(
      EntityRecordService entityRecordService,
      EntityUpdateService entityUpdateService,
      EntityManagementConfiguration emConfig) {
    this.entityRecordService = entityRecordService;
    this.entityUpdateService = entityUpdateService;
    this.emConfig = emConfig;
  }

  /**
   * Method to publish to Enrichment
   *
   * @param wskey
   * @param type type of entity
   * @param identifier entity id
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(
      value = "Permanent Deletion of Entity",
      nickname = "deleteEntity",
      response = java.lang.Void.class)
  @DeleteMapping(
      value = "/{type}/{identifier}/management",
      produces = {HttpHeaders.CONTENT_TYPE_JSONLD, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> deleteEntity(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
      @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
      HttpServletRequest request)
      throws HttpException, EuropeanaApiException {
    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.DELETE, request);
    }
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    Optional<EntityRecord> entityRecordOptional = entityRecordService.retrieveByEntityId(entityUri);
    if (entityRecordOptional.isEmpty()) {
      throw new EntityNotFoundException(entityUri);
    }
    EntityRecord entityRecord = entityRecordOptional.get();

    LOG.info("Permanently deleting entityId={}", entityRecord.getEntityId());
    entityUpdateService.scheduleTasks(
        Collections.singletonList(entityRecord.getEntityId()),
        ScheduledRemovalType.PERMANENT_DELETION);

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
