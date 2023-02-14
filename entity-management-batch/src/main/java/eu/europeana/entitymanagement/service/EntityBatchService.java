package eu.europeana.entitymanagement.service;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.common.config.AppConfig;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.service.BaseEntityRecordService;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(AppConfig.BEAN_ENTITY_BATCH_RECORD_SERVICE)
public class EntityBatchService extends BaseEntityRecordService {

  protected final EntityRecordRepository entityRecordRepository;

  protected final SolrService solrService;

  private static final Logger logger = LogManager.getLogger(EntityBatchService.class);

  @Autowired
  public EntityBatchService(
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources,
      SolrService solrService) {
    super(emConfiguration, datasources);
    this.entityRecordRepository = entityRecordRepository;
    this.solrService = solrService;
  }

  /**
   * Fetches records matching the provided filter(s)
   *
   * @param start
   * @param count
   * @param queryFilters
   * @return
   */
  public List<EntityRecord> findEntitiesWithFilter(int start, int count, Filter[] queryFilters) {
    return this.entityRecordRepository.findWithFilters(start, count, queryFilters);
  }

  /**
   * This method sets the deleted field in the database, it does not remove from solr. This method
   * needs to be used only within the batch item writer
   *
   * @param entityIds list of records to disable
   */
  public void disableBulk(List<String> entityIds) {
    UpdateResult updateResult = entityRecordRepository.disableBulk(entityIds);
    logger.debug(
        "Deprecated {} entities: entityIds={}", updateResult.getModifiedCount(), entityIds);
  }

  /**
   * This method saves the entites in bulk
   *
   * @param records list of entities to be saved
   * @return
   */
  public List<EntityRecord> saveBulkEntityRecords(List<EntityRecord> records) {
    return entityRecordRepository.saveBulk(records);
  }

  /**
   * This method deletes entities permanently from database and from solr index if the
   * deleteFromSolr flag is set to true
   *
   * @param entityIds the ids of the entities to be deleted
   * @param deleteFromSolr flag indicating if the
   * @return the number of entities deleted into the database
   * @throws SolrServiceException if an error occurs when deleteing from solr
   */
  public long deleteBulk(List<String> entityIds, boolean deleteFromSolr)
      throws SolrServiceException {
    if (deleteFromSolr && !entityIds.isEmpty()) {
      solrService.deleteById(entityIds, true);
      logger.debug("Deleted {} entityRecords from Solr: entityIds={}", entityIds.size(), entityIds);
    }

    long deleteCount = entityRecordRepository.deleteBulk(entityIds);
    if (deleteCount > 0) {
      logger.debug("Deleted {} entityRecords from database: entityIds={}", deleteCount, entityIds);
    }
    return deleteCount;
  }

  /**
   * Gets coreferenced entity with the given id (sameAs or exactMatch value in the Consolidated
   * version)
   *
   * @param uris co-reference uris
   * @param entityId indicating the the record for the given entityId should not be retrieved as
   *     matchingCoreference
   * @return Optional containing matching record, or empty optional if none found.
   */
  public Optional<EntityRecord> findEntityDupplicationByCoreference(
      List<String> uris, String entityId) {
    return entityRecordRepository.findEntityDupplicationByCoreference(uris, entityId);
  }

  public void performReferentialIntegrity(Entity entity) {

    // TODO: consider refactoring the implementation of this method by creating a new class (e.g.
    // ReferentialIntegrityProcessor)
    performCommonReferentialIntegrity(entity);
    switch (EntityTypes.valueOf(entity.getType())) {
      case Concept:
        performReferentialIntegrityConcept((Concept) entity);
        break;
      case Agent:
        performReferentialIntegrityAgent((Agent) entity);
        break;
      case Place:
        performReferentialIntegrityPlace((Place) entity);
        break;
      case TimeSpan:
        performReferentialIntegrityTimespan((TimeSpan) entity);
        break;
      case Organization:
        break;
      default:
        break;
    }
  }

  private void performCommonReferentialIntegrity(Entity entity) {
    /*
     * the common fields for all entity types that are references
     */
    // for the field hasPart
    List<String> hasPartField = entity.getHasPart();
    entity.setHasPart(replaceWithInternalReferences(hasPartField));

    // for the field isPartOf
    List<String> isPartOfField = entity.getIsPartOfArray();
    entity.setIsPartOfArray(replaceWithInternalReferences(isPartOfField));

    // for the field isRelatedTo
    List<String> isRelatedToField = entity.getIsRelatedTo();
    entity.setIsRelatedTo(replaceWithInternalReferences(isRelatedToField));
  }

  private void performReferentialIntegrityConcept(Concept entity) {
    // for the field broader
    List<String> broaderField = entity.getBroader();
    entity.setBroader(replaceWithInternalReferences(broaderField));

    // for the field narrower
    List<String> narrowerField = entity.getNarrower();
    entity.setNarrower(replaceWithInternalReferences(narrowerField));

    // for the field related
    List<String> relatedField = entity.getRelated();
    entity.setRelated(replaceWithInternalReferences(relatedField));
  }

  private void performReferentialIntegrityAgent(Agent entity) {
    // for the field placeOfBirth
    List<String> placeOfBirthField = entity.getPlaceOfBirth();
    entity.setPlaceOfBirth(replaceWithInternalReferences(placeOfBirthField));

    // for the field placeOfDeath
    List<String> placeOfDeathField = entity.getPlaceOfDeath();
    entity.setPlaceOfDeath(replaceWithInternalReferences(placeOfDeathField));
    // for the field professionOrOccupation
    List<String> professionOrOccupationField = entity.getProfessionOrOccupation();
    entity.setProfessionOrOccupation(replaceWithInternalReferences(professionOrOccupationField));

    // for the field hasMet
    List<String> hasMetField = entity.getHasMet();
    entity.setHasMet(replaceWithInternalReferences(hasMetField));

    // for the field wasPresentAt
    List<String> wasPresentField = entity.getWasPresentAt();
    entity.setWasPresentAt(replaceWithInternalReferences(wasPresentField));

    // for the field date
    List<String> dateField = entity.getDate();
    entity.setDate(replaceWithInternalReferences(dateField));
  }

  private void performReferentialIntegrityPlace(Place entity) {
    // for the field isNextInSequence
    List<String> isNextInSequenceField = entity.getIsNextInSequence();
    entity.setIsNextInSequence(replaceWithInternalReferences(isNextInSequenceField));
  }

  private void performReferentialIntegrityTimespan(TimeSpan entity) {
    // for the field isNextInSequence
    List<String> isNextInSequenceField = entity.getIsNextInSequence();
    entity.setIsNextInSequence(replaceWithInternalReferences(isNextInSequenceField));
  }

  private List<String> replaceWithInternalReferences(List<String> originalReferences) {
    if (originalReferences == null) {
      return null;
    }
    List<String> updatedReferences = new ArrayList<String>();
    for (String entry : originalReferences) {
      addValueOrInternalReference(updatedReferences, entry);
    }

    if (updatedReferences.isEmpty()) {
      return null;
    }
    return updatedReferences;
  }

  protected void addValueOrInternalReference(List<String> updatedReferences, String value) {
    if (value.startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI) || !UriValidator.isUri(value)) {
      // value is internal reference or string literal
      updatedReferences.add(value);
    } else {
      // value is external URI, replace it with internal reference if they are accessible
      Optional<EntityRecord> record =
          findEntityDupplicationByCoreference(Collections.singletonList(value), null);
      record.ifPresent(entityRecord -> updatedReferences.add(entityRecord.getEntityId()));
    }
  }
}
