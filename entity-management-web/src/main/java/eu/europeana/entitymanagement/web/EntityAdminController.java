package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FormatTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Validated
@RequestMapping("/entity")
public class EntityAdminController extends BaseRest {

    private static final Logger LOG = LogManager.getLogger(EntityAdminController.class);

    @Autowired
    private EntityRecordService entityRecordService;

    @Autowired
    private EntityManagementConfiguration emConfig;

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
    @ApiOperation(value = "Permanent Deletion of Entity", nickname = "deleteEntity", response = java.lang.Void.class)
    @DeleteMapping(value = "/{type}/{identifier}/management",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            HttpServletRequest request) throws HttpException, EuropeanaApiException {
        if (emConfig.isAuthEnabled()) {
            verifyWriteAccess(Operations.DELETE, request);
        }
        EntityRecord entityRecord = entityRecordService.retrieveEntityRecord(type, identifier.toLowerCase());
        LOG.debug("Deleting permanently entity : {}/{}", type, identifier);
        entityRecordService.delete(entityRecord.getEntityId());
        return ResponseEntity.noContent().build();
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
    @ApiOperation(value = "Migrate existing Entity", nickname = "migrateExistingEntity", response = java.lang.Void.class)
    @PostMapping(value = "/{type}/{identifier}/management",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> migrateExistingEntity(
            @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_TYPE) String type,
            @PathVariable(value = WebEntityConstants.PATH_PARAM_IDENTIFIER) String identifier,
            @RequestBody EntityPreview entityCreationRequest,
            HttpServletRequest request) throws HttpException, UnsupportedEntityTypeException, EuropeanaApiException {
        if (emConfig.isAuthEnabled()) {
            //TODO later change the verification method once DB migration is done
           // verifyWriteAccess(Operations.CREATE, request);
            verifyMigrationAccess(request);
        }
        // validate Entity Type
        EntityTypes.getByEntityType(type);
        LOG.debug("Migrating existing entity: {}", entityCreationRequest.getId());

        EntityRecord savedEntityRecord = entityRecordService
                .createEntityFromMigrationRequest(entityCreationRequest, type, identifier);
        LOG.debug("Created Entity record for {}; entityId={}", entityCreationRequest.getId(), savedEntityRecord.getEntityId());
        return generateResponseEntity(EntityProfile.internal.toString(), FormatTypes.jsonld, null, savedEntityRecord, HttpStatus.ACCEPTED);
    }

    /**
     * Method will authenticate Migration requests.
     * This is done to avoid the expiration of tokens
     *
     * TODO remove after DB migration
     *
     * @param request
     * @throws ApplicationAuthenticationException
     */
    private void verifyMigrationAccess(HttpServletRequest request) throws ApplicationAuthenticationException {
     if (request.getHeader(HttpHeaders.AUTHORIZATION) == null || request.getHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
       throw new ApplicationAuthenticationException("User is not authorised to perform this action", null);
     }
    if (!request.getHeader(HttpHeaders.AUTHORIZATION).equals(emConfig.getEnrichmentsMigrationPassword())) {
        throw new ApplicationAuthenticationException("Invalid token for migrating existing entity", null, null, HttpStatus.FORBIDDEN);
    }
    }
}
