package eu.europeana.entitymanagement.web;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.exception.HttpException;

import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.model.EnrichmentResponse;
import eu.europeana.entitymanagement.service.EnrichmentService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/entity/management")
public class EnrichmentController extends BaseRest{

  private static final Logger LOG = LogManager.getLogger(EnrichmentController.class);

  @Autowired
  private EnrichmentService entityEnrichmentService;

 @Autowired
  private EntityRecordService entityRecordService;

 @Autowired
  private EntityManagementConfiguration emConfig;

  /**
   * Method to publish to Enrichment
   *
   * @param wskey
   * @param entityList list of entity Uri's
   * @param request
   * @return
   * @throws HttpException
   */
  @ApiOperation(value = "Publish to enrichment", nickname = "publishEnrichment", response = java.lang.Void.class)
  @PostMapping(value = "/enrichment",produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EnrichmentResponse> publishEnrichment(
      @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
      @RequestBody List<String> entityList,
      HttpServletRequest request) throws ApplicationAuthenticationException {

    if (emConfig.isAuthEnabled()) {
      verifyWriteAccess(Operations.CREATE, request);
    }
    return publishToEnrichment(entityList);
  }

  /**
   * Retrieves the entity via entity uri and publishes to the Enrichment
   * return the count of the successfully published entities.
   *
   * @param entityList
   * @return
   */
  private ResponseEntity<EnrichmentResponse> publishToEnrichment(List<String> entityList) {
    List<String> entityPublished = new ArrayList<>();
    for (String entityUri: entityList) {
      Optional<EntityRecord> entityRecordOptional = entityRecordService.retrieveEntityRecordByUri(entityUri);
      try {
        if (entityRecordOptional.isEmpty()) {
          throw new EntityNotFoundException(entityUri);
        }
        EntityRecord entityRecord = entityRecordOptional.get();

        if (entityRecord.isDisabled()) {
          throw new EntityRemovedException(entityUri);
        }
        entityEnrichmentService.saveEnrichment(entityRecord);
        entityPublished.add(entityUri);
      } catch (EntityNotFoundException | EntityRemovedException  e) {
        LOG.error("Error publishing the enrichment for entity. {} ", e.getMessage());
      }
    }
    return new ResponseEntity<>(prepareEnrichmentResponse(entityList, entityPublished), HttpStatus.OK);
  }

    private EnrichmentResponse prepareEnrichmentResponse(List<String> entityList, List<String> entitiesPublished) {
     List<String> successful = null;
     List<String> failed = null;
     long expected = entityList.size();
     if (!entitiesPublished.isEmpty()) {
          successful = entitiesPublished;
      }
      entityList.removeAll(entitiesPublished);
      if (!entityList.isEmpty()) {
          failed = entityList;
      }
      return  new EnrichmentResponse(expected, successful, failed);
    }
}
