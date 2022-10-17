package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaProxyId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getIsAggregatedById;
import static java.time.Instant.now;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.exception.EntityAlreadyExistsException;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.exception.HttpUnprocessableException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

  private final EntityRecordRepository entityRecordRepository;

  final EntityManagementConfiguration emConfiguration;

  private final DataSources datasources;

  private final SolrService solrService;

  private static final Logger logger = LogManager.getLogger(EntityRecordService.class);

  private static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has been removed";

  // Fields to be ignored during consolidation ("type" is final, so it cannot be updated)
  private static final List<String> ignoredMergeFields = List.of("type");

  @Autowired
  public EntityRecordService(
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources,
      SolrService solrService) {
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
    this.solrService = solrService;
  }

  public boolean existsByEntityId(String entityId) {
    return entityRecordRepository.existsByEntityId(entityId);
  }

  public Optional<EntityRecord> retrieveByEntityId(String entityId) {
    return Optional.ofNullable(entityRecordRepository.findByEntityId(entityId));
  }

  /**
   * Retives Multiple Entities at a time. NOTE : If the entity does not exist or is deprecated, do
   * not include it in the response hence, excludeDisabled is set to true.
   *
   * @param entityIds
   * @return
   */
  public List<EntityRecord> retrieveMultipleByEntityIds(List<String> entityIds) {
    return entityRecordRepository.findByEntityIds(entityIds, true, true);
  }

  public EntityRecord retrieveEntityRecord(String type, String identifier, boolean retrieveDisabled)
      throws EuropeanaApiException {
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    Optional<EntityRecord> entityRecordOptional = this.retrieveByEntityId(entityUri);
    if (entityRecordOptional.isEmpty()) {
      throw new EntityNotFoundException(entityUri);
    }

    EntityRecord entityRecord = entityRecordOptional.get();
    if (!retrieveDisabled && entityRecord.isDisabled()) {
      throw new EntityRemovedException(String.format(ENTITY_ID_REMOVED_MSG, entityUri));
    }
    return entityRecord;
  }

  public List<EntityIdDisabledStatus> retrieveMultipleByEntityId(
      List<String> entityIds, boolean excludeDisabled) {
    return entityRecordRepository.getEntityIds(entityIds, excludeDisabled);
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

  public EntityRecord saveEntityRecord(EntityRecord er) {
    return entityRecordRepository.save(er);
  }

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
   * Mark entity record as disabled and remove from solr
   *
   * @param er the entity record
   * @param forceSolrCommit indicating if a solr commit should be explicitly (synchronuously)
   *     executed
   * @throws EntityUpdateException incase of solr access errors
   */
  public void disableEntityRecord(EntityRecord er, boolean forceSolrCommit)
      throws EntityUpdateException {
    try {
      solrService.deleteById(List.of(er.getEntityId()), forceSolrCommit);
    } catch (SolrServiceException e) {
      throw new EntityUpdateException("Cannot delete solr record with id: " + er.getEntityId(), e);
    }
    er.setDisabled(new Date());
    saveEntityRecord(er);
  }

  /**
   * Re-Enable an already existing entity record.
   *
   * @param entityRecord entity record to update
   * @throws EntityUpdateException in case of solr access errors
   */
  public void enableEntityRecord(EntityRecord entityRecord) throws EntityUpdateException {
    // disabled records have a date value (indicating when they were disabled)
    entityRecord.setDisabled(null);
    saveEntityRecord(entityRecord);

    // entity needs to be added back to the solr index
    try {
      solrService.storeEntity(createSolrEntity(entityRecord));
    } catch (SolrServiceException e) {
      throw new EntityUpdateException(
          "Cannot create solr record for entity with id: " + entityRecord.getEntityId(), e);
    }
  }

  /**
   * Updates an already existing entity record.
   *
   * @param entityRecord entity record to update
   * @return updated entity
   */
  public EntityRecord update(EntityRecord entityRecord) {
    return this.saveEntityRecord(entityRecord);
  }

  /**
   * Delete an already existing entity record permanently.
   *
   * @param entityId entity record to delete
   * @return the number of deleted objects
   * @throws SolrServiceException if the deletion from solr is not executed successfully
   */
  public long delete(String entityId) throws SolrServiceException {
    // delete from Solr before Mongo, so Solr errors won't leave DB in an inconsistent state
    solrService.deleteById(List.of(entityId), true);

    return entityRecordRepository.deleteForGood(entityId);
  }

  /**
   * Creates an {@link EntityRecord} from an {@link Entity}, which is then persisted. Note : This
   * method is used for creating Entity for Migration requests
   *
   * @param europeanaProxyEntity
   * @param type type of entity
   * @param identifier id of entity
   * @return Saved Entity record
   * @throws EntityCreationException if an error occurs
   * @throws HttpUnprocessableException
   * @throws HttpBadRequestException
   */
  public EntityRecord createEntityFromMigrationRequest(
      Entity europeanaProxyEntity, String type, String identifier)
      throws EntityAlreadyExistsException, HttpBadRequestException, HttpUnprocessableException,
          EntityModelCreationException {

    String externalProxyId = europeanaProxyEntity.getEntityId();

    // Fail quick if no datasource is configured
    DataSource externalDatasource = datasources.verifyDataSource(externalProxyId, true);

    Date timestamp = new Date();

    Entity entity = EntityObjectFactory.createConsolidatedEntityObject(type);
    String entityId = generateEntityId(entity.getType(), identifier);
    // check if entity already exists
    // this is avoid MongoDb exception for duplicate key
    checkIfEntityAlreadyExists(entityId);
    entity.setEntityId(entityId);
    /*
     * sameAs will be replaced during consolidation; however we set this here to prevent duplicate
     * registrations if consolidation fails
     */
    entity.setSameReferenceLinks(new ArrayList<>(List.of(externalProxyId)));
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntityId(entityId);
    entityRecord.setEntity(entity);

    europeanaProxyEntity.setEntityId(entityId);

    // create metis Entity
    Entity metisEntity = EntityObjectFactory.createProxyEntityObject(type);

    // set proxies
    // europeana proxy first
    setEuropeanaMetadata(
        europeanaProxyEntity,
        entityId,
        new ArrayList<>(List.of(externalProxyId)),
        entityRecord,
        timestamp);
    // external proxy second
    setExternalProxy(
        metisEntity, externalProxyId, entityId, externalDatasource, entityRecord, timestamp, 1);

    upsertEntityAggregation(entityRecord, entityId, timestamp);
    return entityRecordRepository.save(entityRecord);
  }

  /**
   * Creates an {@link EntityRecord} from an {@link Entity}, which is then persisted.
   *
   * @param europeanaProxyEntity Entity for the europeana proxy
   * @param datasourceResponse Entity obtained from de-referencing
   * @param dataSource the data source identified for the given entity id
   * @return Saved Entity record
   * @throws EntityCreationException if an error occurs
   */
  public EntityRecord createEntityFromRequest(
      Entity europeanaProxyEntity, Entity datasourceResponse, DataSource dataSource)
      throws EntityCreationException {

    // Fail quick if no datasource is configured
    String externalEntityId = europeanaProxyEntity.getEntityId();

    Date timestamp = new Date();
    Entity entity;
    try {
      entity = EntityObjectFactory.createConsolidatedEntityObject(europeanaProxyEntity);
    } catch (EntityModelCreationException e) {
      throw new EntityCreationException(e.getMessage(), e);
    }
    boolean isZohoOrg =
        ZohoUtils.isZohoOrganization(externalEntityId, datasourceResponse.getType());
    String entityId = generateEntityId(datasourceResponse, isZohoOrg);

    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntityId(entityId);
    entity.setEntityId(entityId);

    /*
     * sameAs will be replaced during consolidation; however we set this here to prevent duplicate
     * registrations if consolidation fails
     */
    List<String> sameAs =
        buildSameAsReferenceLinks(externalEntityId, datasourceResponse, europeanaProxyEntity);
    entity.setSameReferenceLinks(sameAs);
    entityRecord.setEntity(entity);

    // set Europeana Proxy
    // copy the newly generated europeana id
    europeanaProxyEntity.setEntityId(entityId);
    List<String> dereferencedCorefs = buildDereferencedCorefs(datasourceResponse, externalEntityId);
    setEuropeanaMetadata(
        europeanaProxyEntity, entityId, dereferencedCorefs, entityRecord, timestamp);

    // create external proxy
    setExternalProxy(
        datasourceResponse, externalEntityId, entityId, dataSource, entityRecord, timestamp, 1);

    // create second external proxy for Zoho organizations with Wikidata
    Optional<String> wikidataId;
    if (isZohoOrg
        && (wikidataId = WikidataUtils.getWikidataId(datasourceResponse.getSameReferenceLinks()))
            .isPresent()) {

      String wikidataOrganizationId = wikidataId.get();
      // note appendWikidataProxy is also calling the upsertEntityAggregation method, which is
      // redundant with the next call,
      // for the simplicity of the code, we can leve the dupplicate call as everything is in memory
      appendWikidataProxy(
          entityRecord, wikidataOrganizationId, datasourceResponse.getType(), timestamp);

      // add the wikidata ID to europeana proxy entity
      europeanaProxyEntity.addSameReferenceLink(wikidataOrganizationId);
    }

    // create aggregation object
    upsertEntityAggregation(entityRecord, entityId, timestamp);

    return entityRecordRepository.save(entityRecord);
  }

  List<String> buildDereferencedCorefs(Entity datasourceResponse, String externalEntityId) {
    List<String> corefs = new ArrayList<>();
    corefs.add(externalEntityId);

    // in case of wikidata redirections (id in response different from the externalEntityId) add the
    // new external ID to corefs as well
    if (!corefs.contains(datasourceResponse.getAbout())) {
      corefs.add(datasourceResponse.getAbout());
    }

    return corefs;
  }

  /**
   * Creates a wikidata proxy and appends it to the proxy list, also updates the aggregates list for
   * the consolidated version
   *
   * @param entityRecord the entity record to be updated
   * @param wikidataProxyId the wikidata entity id
   * @param entityType the entity type
   * @param timestamp the timestamp set as created and modified dates
   * @throws EntityCreationException if the creation is not successfull
   * @return the new created Entity Proxy object
   */
  public EntityProxy appendWikidataProxy(
      EntityRecord entityRecord, String wikidataProxyId, String entityType, Date timestamp)
      throws EntityCreationException {
    try {
      // entity metadata will be populated during update task
      Entity wikidataProxyEntity = EntityObjectFactory.createProxyEntityObject(entityType);

      Optional<DataSource> wikidataDatasource = getDataSource(wikidataProxyId);
      // exception is thrown in factory method if wikidataDatasource is empty
      int proxyNr = entityRecord.getProxies().size();
      EntityProxy wikidataProxy =
          setExternalProxy(
              wikidataProxyEntity,
              wikidataProxyId,
              entityRecord.getEntityId(),
              wikidataDatasource.get(),
              entityRecord,
              timestamp,
              proxyNr);

      // add wikidata uri to entity sameAs
      entityRecord.getEntity().addSameReferenceLink(wikidataProxyId);
      // add to entityIsAggregatedBy, use upsertMethod
      upsertEntityAggregation(entityRecord, entityType, timestamp);

      return wikidataProxy;
    } catch (EntityModelCreationException e) {
      throw new EntityCreationException(e.getMessage(), e);
    }
  }

  String generateEntityId(Entity datasourceResponse, boolean isZohoOrg) {
    // only in case of Zoho Organization use the provided id from de-referencing
    String entityId = null;
    if (isZohoOrg) {
      // zoho id is mandatory and unique identifier for zoho Organizations
      String zohoId = EntityRecordUtils.getIdFromUrl(datasourceResponse.getEntityId());
      entityId = EntityRecordUtils.buildEntityIdUri(datasourceResponse.getType(), zohoId);
    } else {
      entityId = generateEntityId(datasourceResponse.getType(), null);
    }
    return entityId;
  }

  List<String> buildSameAsReferenceLinks(
      String externalProxyId, Entity datasourceResponse, Entity europeanaProxyEntity) {
    // entity id might be different than proxyId in case of redirections
    SortedSet<String> sameAsUrls = new TreeSet<String>();
    sameAsUrls.add(datasourceResponse.getEntityId());
    sameAsUrls.add(externalProxyId);
    if (datasourceResponse.getSameReferenceLinks() != null) {
      sameAsUrls.addAll(datasourceResponse.getSameReferenceLinks());
    }
    // add the sameAs from europeanaProxy
    if (europeanaProxyEntity.getSameReferenceLinks() != null) {
      sameAsUrls.addAll(europeanaProxyEntity.getSameReferenceLinks());
    }

    List<String> sameAs = new ArrayList<String>(sameAsUrls);
    return sameAs;
  }

  private Optional<DataSource> getDataSource(String externalProxyId)
      throws EntityCreationException {
    Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(externalProxyId);
    if (externalDatasourceOptional.isEmpty()) {
      throw new EntityCreationException("No configured datasource for id " + externalProxyId);
    }
    return externalDatasourceOptional;
  }

  /**
   * Checks if Entity already exists
   *
   * @param entityId
   * @throws EntityAlreadyExistsException
   */
  private void checkIfEntityAlreadyExists(String entityId) throws EntityAlreadyExistsException {
    Optional<EntityRecord> entityRecordOptional = retrieveByEntityId(entityId);
    if (entityRecordOptional.isPresent()) {
      throw new EntityAlreadyExistsException(entityId);
    }
  }

  /**
   * generates the EntityId If entityId is present, generate entity id uri with entityId else
   * generates a auto increment id ex: http://data.europeana.eu/<entitytype>/<entityId> OR
   * http://data.europeana.eu/<entitytype>/<dbId>
   *
   * @param entityType
   * @param entityId
   * @return the generated EntityId
   */
  private String generateEntityId(String entityType, String entityId) {
    if (entityId != null) {
      return EntityRecordUtils.buildEntityIdUri(entityType, entityId);
    } else {
      long dbId = entityRecordRepository.generateAutoIncrement(entityType);
      return EntityRecordUtils.buildEntityIdUri(entityType, String.valueOf(dbId));
    }
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

  /** @deprecated */
  @Deprecated(since = "", forRemoval = false)
  private Map<String, List<String>> replaceWithInternalReferences(
      Map<String, List<String>> originalReferences) {
    if (originalReferences == null) {
      return null;
    }

    Map<String, List<String>> updatedReferenceMap = new HashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : originalReferences.entrySet()) {
      List<String> updatedReferences = new ArrayList<String>();

      for (String value : entry.getValue()) {
        addValueOrInternalReference(updatedReferences, value);
      }

      if (!updatedReferences.isEmpty()) {
        updatedReferenceMap.put(entry.getKey(), updatedReferences);
      }
    }

    if (updatedReferenceMap.isEmpty()) {
      return null;
    }

    return updatedReferenceMap;
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

  private void addValueOrInternalReference(List<String> updatedReferences, String value) {
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

  /**
   * This function merges the metadata data from the provided entities and returns the consolidated
   * version
   *
   * @throws EntityCreationException
   */
  public Entity mergeEntities(Entity primary, Entity secondary)
      throws EuropeanaApiException, EntityModelCreationException {

    // TODO: consider refactoring of this implemeentation by creating a new class
    // EntityReconciliator
    /*
     * The primary entity corresponds to the entity in the Europeana proxy. The secondary entity
     * corresponds to the entity in the external proxy.
     */
    List<Field> fieldsToCombine =
        EntityUtils.getAllFields(primary.getClass()).stream()
            .filter(f -> !ignoredMergeFields.contains(f.getName()))
            .collect(Collectors.toList());
    return combineEntities(primary, secondary, fieldsToCombine, true);
  }

  public void updateConsolidatedVersion(EntityRecord entityRecord, Entity consolidatedEntity) {

    entityRecord.setEntity(consolidatedEntity);
    upsertEntityAggregation(entityRecord, consolidatedEntity.getEntityId(), new Date());
  }

  /**
   * Replaces Europeana proxy metadata with the provided entity metadata.
   *
   * <p>EntityId and SameAs values are not affected
   *
   * @param updateRequestEntity entity to replace with
   * @param entityRecord entity record
   */
  public void replaceEuropeanaProxy(final Entity updateRequestEntity, EntityRecord entityRecord) {
    EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();

    String entityId = europeanaProxy.getEntity().getEntityId();

    // copy EntityId from existing Europeana proxy metadata
    europeanaProxy.setEntity(updateRequestEntity);
    europeanaProxy.getEntity().setEntityId(entityId);

    europeanaProxy.getProxyIn().setModified(Date.from(now()));
  }

  /**
   * Reconciles metadata between two entities.
   *
   * @param primary Primary entity. Metadata from this entity takes precedence
   * @param secondary Secondary entity. Metadata from this entity is only used if no matching field
   *     is contained within the primary entity.
   * @param fieldsToCombine metadata fields to reconcile
   * @param accumulate if true, metadata from the secondary entity are added to the matching
   *     collection (eg. maps, lists and arrays) within the primary . If accumulate is false, the
   *     "primary" content overwrites the "secondary"
   */
  private Entity combineEntities(
      Entity primary, Entity secondary, List<Field> fieldsToCombine, boolean accumulate)
      throws EuropeanaApiException, EntityModelCreationException {
    Entity consolidatedEntity =
        EntityObjectFactory.createConsolidatedEntityObject(primary.getType());

    try {

      /*
       * store the preferred label in the secondary entity that is different from the preferred
       * label in the primary entity to the alternative labels of the consolidated entity
       */
      Map<Object, Object> prefLabelsForAltLabels = new HashMap<>();

      for (Field field : fieldsToCombine) {

        Class<?> fieldType = field.getType();
        String fieldName = field.getName();

        if (fieldType.isArray()) {
          Object[] mergedArray = mergeArrays(primary, secondary, field, accumulate);
          consolidatedEntity.setFieldValue(field, mergedArray);

        } else if (List.class.isAssignableFrom(fieldType)) {

          List<Object> fieldValuePrimaryObjectList = (List<Object>) primary.getFieldValue(field);
          List<Object> fieldValueSecondaryObjectList =
              (List<Object>) secondary.getFieldValue(field);
          mergeList(
              consolidatedEntity,
              fieldValuePrimaryObjectList,
              fieldValueSecondaryObjectList,
              field,
              accumulate);

        } else if (isStringOrPrimitive(fieldType)) {
          Object fieldValuePrimaryObjectPrimitiveOrString = primary.getFieldValue(field);
          Object fieldValueSecondaryObjectPrimitiveOrString = secondary.getFieldValue(field);

          if (fieldValuePrimaryObjectPrimitiveOrString == null
              && fieldValueSecondaryObjectPrimitiveOrString != null) {
            consolidatedEntity.setFieldValue(field, fieldValueSecondaryObjectPrimitiveOrString);
          } else if (fieldValuePrimaryObjectPrimitiveOrString != null) {
            consolidatedEntity.setFieldValue(field, fieldValuePrimaryObjectPrimitiveOrString);
          }

        } else if (Date.class.isAssignableFrom(fieldType)) {
          Object fieldValuePrimaryObjectDate = primary.getFieldValue(field);
          Object fieldValueSecondaryObjectDate = secondary.getFieldValue(field);

          if (fieldValuePrimaryObjectDate == null && fieldValueSecondaryObjectDate != null) {
            consolidatedEntity.setFieldValue(
                field, new Date(((Date) fieldValueSecondaryObjectDate).getTime()));
          } else if (fieldValuePrimaryObjectDate != null) {
            consolidatedEntity.setFieldValue(
                field, new Date(((Date) fieldValuePrimaryObjectDate).getTime()));
          }

        } else if (Map.class.isAssignableFrom(fieldType)) {
          combineEntities(
              consolidatedEntity,
              primary,
              secondary,
              prefLabelsForAltLabels,
              field,
              fieldName,
              accumulate);
        } else if (WebResource.class.isAssignableFrom(fieldType)) {
          mergeWebResources(primary, secondary, field, consolidatedEntity);
        } else if (Address.class.isAssignableFrom(fieldType)) {
          mergeAddress(primary, secondary, field, consolidatedEntity);
        }
      }

      mergeSkippedPrefLabels(consolidatedEntity, prefLabelsForAltLabels, fieldsToCombine);

    } catch (IllegalAccessException e) {
      throw new EntityUpdateException(
          "Metadata consolidation failed to access required properties!", e);
    }

    return consolidatedEntity;
  }

  /**
   * Will merge the Web Resources
   *
   * @param primary
   * @param secondary
   * @param field
   * @param consolidatedEntity
   * @throws IllegalAccessException
   */
  private void mergeWebResources(
      Entity primary, Entity secondary, Field field, Entity consolidatedEntity)
      throws IllegalAccessException {
    WebResource primaryWebResource = (WebResource) primary.getFieldValue(field);
    WebResource secondaryWebResource = (WebResource) secondary.getFieldValue(field);
    if (primaryWebResource == null && secondaryWebResource != null) {
      consolidatedEntity.setFieldValue(field, new WebResource(secondaryWebResource));
    } else if (primaryWebResource != null) {
      consolidatedEntity.setFieldValue(field, new WebResource(primaryWebResource));
    }
  }

  /**
   * Will combine the address
   *
   * @param primary
   * @param secondary
   * @param field
   * @param consolidatedEntity
   * @throws IllegalAccessException
   */
  private void mergeAddress(
      Entity primary, Entity secondary, Field field, Entity consolidatedEntity)
      throws IllegalAccessException {
    Address primaryAddress = (Address) primary.getFieldValue(field);
    Address secondaryAddress = (Address) secondary.getFieldValue(field);
    if (primaryAddress == null && secondaryAddress != null) {
      consolidatedEntity.setFieldValue(field, new Address(secondaryAddress));
    } else if (primaryAddress != null) {
      consolidatedEntity.setFieldValue(field, new Address(primaryAddress));
    }
  }

  boolean isStringOrPrimitive(Class<?> fieldType) {
    return String.class.isAssignableFrom(fieldType)
        || fieldType.isPrimitive()
        || Float.class.isAssignableFrom(fieldType)
        || Integer.class.isAssignableFrom(fieldType);
  }

  void combineEntities(
      Entity consolidatedEntity,
      Entity primary,
      Entity secondary,
      Map<Object, Object> prefLabelsForAltLabels,
      Field field,
      String fieldName,
      boolean accumulate)
      throws IllegalAccessException {
    // TODO: refactor implemetation

    Map<Object, Object> fieldValuePrimaryObjectMap =
        (Map<Object, Object>) primary.getFieldValue(field);
    Map<Object, Object> fieldValueSecondaryObjectMap =
        (Map<Object, Object>) secondary.getFieldValue(field);
    Map<Object, Object> fieldValuePrimaryObject = initialiseObjectMap(fieldValuePrimaryObjectMap);
    Map<Object, Object> fieldValueSecondaryObject =
        initialiseObjectMap(fieldValueSecondaryObjectMap);

    if (CollectionUtils.isEmpty(fieldValuePrimaryObject)
        && !CollectionUtils.isEmpty(fieldValueSecondaryObject)) {
      fieldValuePrimaryObject.putAll(fieldValueSecondaryObject);

    } else if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)
        && !CollectionUtils.isEmpty(fieldValueSecondaryObject)
        && accumulate) {
      for (Map.Entry elemSecondary : fieldValueSecondaryObject.entrySet()) {
        Object key = elemSecondary.getKey();
        /*
         * if the map value is a list, merge the lists of the primary and the secondary object
         * without duplicates
         */
        mergePrimarySecondaryListWitoutDuplicates(
            fieldValuePrimaryObject, key, elemSecondary, fieldName, prefLabelsForAltLabels);
      }
    }
    if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    }
  }

  /**
   * Merges the Primary and secondary list without duplicates
   *
   * @param fieldValuePrimaryObject
   * @param key
   * @param elemSecondary
   * @param fieldName
   * @param prefLabelsForAltLabels
   */
  private void mergePrimarySecondaryListWitoutDuplicates(
      Map<Object, Object> fieldValuePrimaryObject,
      Object key,
      Map.Entry elemSecondary,
      String fieldName,
      Map<Object, Object> prefLabelsForAltLabels) {
    if (fieldValuePrimaryObject.containsKey(key)
        && List.class.isAssignableFrom(elemSecondary.getValue().getClass())) {
      List<Object> listSecondaryObject = (List<Object>) elemSecondary.getValue();
      List<Object> listPrimaryObject =
          new ArrayList<>((List<Object>) fieldValuePrimaryObject.get(key));
      boolean listPrimaryObjectChanged = false;
      for (Object elemSecondaryList : listSecondaryObject) {
        if (!listPrimaryObject.contains(elemSecondaryList)) {
          listPrimaryObject.add(elemSecondaryList);
          if (listPrimaryObjectChanged == false) {
            listPrimaryObjectChanged = true;
          }
        }
      }

      if (listPrimaryObjectChanged) {
        fieldValuePrimaryObject.put(key, listPrimaryObject);
      }
    }
    // keep the different preferred labels in the secondary object for the
    // alternative label in the consolidated object
    else if (fieldValuePrimaryObject.containsKey(key)
        && fieldName.toLowerCase().contains("pref")
        && fieldName.toLowerCase().contains("label")) {
      Object primaryObjectPrefLabel = fieldValuePrimaryObject.get(key);
      if (!primaryObjectPrefLabel.equals(elemSecondary.getValue())) {
        prefLabelsForAltLabels.put(key, elemSecondary.getValue());
      }
    } else if (!fieldValuePrimaryObject.containsKey(key)) {
      fieldValuePrimaryObject.put(key, elemSecondary.getValue());
    }
  }

  private Map<Object, Object> initialiseObjectMap(Map<Object, Object> fieldValueObjectMap) {
    if (fieldValueObjectMap != null) {
      return new HashMap<>(fieldValueObjectMap);
    }
    return new HashMap<>();
  }

  void mergeSkippedPrefLabels(
      Entity consilidatedEntity,
      Map<Object, Object> prefLabelsForAltLabels,
      List<Field> allEntityFields)
      throws IllegalAccessException {
    /*
     * adding the preferred labels from the secondary object to the alternative labels of
     * consolidated object
     */
    if (prefLabelsForAltLabels.size() > 0) {
      for (Field field : allEntityFields) {
        String fieldName = field.getName();
        if (isFieldAltLabel(fieldName)) {
          Map<Object, Object> altLabelConsolidatedMap =
              (Map<Object, Object>) consilidatedEntity.getFieldValue(field);
          Map<Object, Object> altLabelPrimaryObject =
              initialiseAltLabelMap(altLabelConsolidatedMap);
          boolean altLabelPrimaryValueChanged = false;
          altLabelPrimaryValueChanged =
              addValuesToAltLabel(
                  prefLabelsForAltLabels, altLabelPrimaryObject, altLabelPrimaryValueChanged);
          if (altLabelPrimaryValueChanged) {
            consilidatedEntity.setFieldValue(field, altLabelPrimaryObject);
          }
          break;
        }
      }
    }
  }

  private boolean addValuesToAltLabel(
      Map<Object, Object> prefLabelsForAltLabels,
      Map<Object, Object> altLabelPrimaryObject,
      boolean altLabelPrimaryValueChanged) {
    for (Map.Entry<Object, Object> prefLabel : prefLabelsForAltLabels.entrySet()) {
      String keyPrefLabel = (String) prefLabel.getKey();
      List<Object> altLabelPrimaryObjectList =
          (List<Object>) altLabelPrimaryObject.get(keyPrefLabel);
      List<Object> altLabelPrimaryValue = initialiseAltLabelList(altLabelPrimaryObjectList);
      if (shouldValuesBeAddedToAltLabel(altLabelPrimaryValue, prefLabel)) {
        altLabelPrimaryValue.add(prefLabel.getValue());
        if (altLabelPrimaryValueChanged == false) {
          altLabelPrimaryValueChanged = true;
        }
        altLabelPrimaryObject.put(keyPrefLabel, altLabelPrimaryValue);
      }
    }
    return altLabelPrimaryValueChanged;
  }

  private boolean isFieldAltLabel(String fieldName) {
    return fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label");
  }

  private boolean shouldValuesBeAddedToAltLabel(
      List<Object> altLabelPrimaryValue, Map.Entry<Object, Object> prefLabel) {
    return altLabelPrimaryValue.isEmpty()
        || (!altLabelPrimaryValue.isEmpty()
            && !altLabelPrimaryValue.contains(prefLabel.getValue()));
  }

  private Map<Object, Object> initialiseAltLabelMap(Map<Object, Object> altLabelConsolidatedMap) {
    if (altLabelConsolidatedMap != null) {
      return new HashMap<>(altLabelConsolidatedMap);
    }
    return new HashMap<>();
  }

  private List<Object> initialiseAltLabelList(List<Object> altLabelPrimaryObjectList) {
    if (altLabelPrimaryObjectList != null) {
      return new ArrayList<>(altLabelPrimaryObjectList);
    }
    return new ArrayList<>();
  }

  void mergeList(
      Entity consolidatedEntity,
      List<Object> fieldValuePrimaryObjectList,
      List<Object> fieldValueSecondaryObjectList,
      Field field,
      boolean accumulate)
      throws IllegalAccessException {
    List<Object> fieldValuePrimaryObject = null;
    List<Object> fieldValueSecondaryObject = null;
    if (fieldValuePrimaryObjectList != null) {
      fieldValuePrimaryObject = new ArrayList<Object>(fieldValuePrimaryObjectList);
    }
    if (fieldValueSecondaryObjectList != null) {
      fieldValueSecondaryObject = new ArrayList<Object>(fieldValueSecondaryObjectList);
    }

    if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
      if (accumulate) {
        for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
          if (!fieldValuePrimaryObject.contains(secondaryObjectListObject)) {
            fieldValuePrimaryObject.add(secondaryObjectListObject);
          }
        }
        consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
      } else {
        consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
      }
      return;
    } else if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
      consolidatedEntity.setFieldValue(field, fieldValueSecondaryObject);
      return;
    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject == null) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
      return;
    }
  }

  Object[] mergeArrays(Entity primary, Entity secondary, Field field, boolean append)
      throws IllegalAccessException {
    Object[] primaryArray = (Object[]) primary.getFieldValue(field);
    Object[] secondaryArray = (Object[]) secondary.getFieldValue(field);

    if (primaryArray == null && secondaryArray == null) {
      return null;
    } else if (primaryArray == null) {
      // return a clone of the secondary
      return secondaryArray.clone();
    } else if (secondaryArray == null || !append) {
      // return a clone of the primary if we're not appending
      return primaryArray.clone();
    }

    // merge arrays
    Set<Object> mergedAndOrdered = new TreeSet<>(Arrays.asList(primaryArray));
    mergedAndOrdered.addAll(Arrays.asList(secondaryArray));

    return mergedAndOrdered.toArray(Arrays.copyOf(primaryArray, 0));
  }

  public void dropRepository() {
    this.entityRecordRepository.dropCollection();
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

  private void upsertEntityAggregation(EntityRecord entityRecord, String entityId, Date timestamp) {
    Aggregation aggregation = entityRecord.getEntity().getIsAggregatedBy();
    if (aggregation == null) {
      aggregation = createNewAggregation(entityId, timestamp);
      entityRecord.getEntity().setIsAggregatedBy(aggregation);
    } else {
      aggregation.setModified(timestamp);
    }

    updateEntityAggregatesList(aggregation, entityRecord, entityId);
  }

  private Aggregation createNewAggregation(String entityId, Date timestamp) {
    Aggregation isAggregatedBy = new Aggregation();
    isAggregatedBy.setId(getIsAggregatedById(entityId));
    isAggregatedBy.setCreated(timestamp);
    isAggregatedBy.setModified(timestamp);
    return isAggregatedBy;
  }

  private void updateEntityAggregatesList(
      Aggregation aggregation, EntityRecord entityRecord, String entityId) {
    // aggregates is mutable in case we need to append to it later
    List<String> aggregates = new ArrayList<>();
    aggregates.add(getEuropeanaAggregationId(entityId));
    if (entityRecord.getExternalProxies() != null) {
      for (int i = 0; i < entityRecord.getExternalProxies().size(); i++) {
        aggregates.add(getDatasourceAggregationId(entityRecord.getEntityId(), i + 1));
      }
    }
    aggregation.setAggregates(aggregates);
  }

  private void setEuropeanaMetadata(
      Entity europeanaProxyMetadata,
      String entityId,
      List<String> corefs,
      EntityRecord entityRecord,
      Date timestamp) {
    Aggregation europeanaAggr = new Aggregation();
    Optional<DataSource> europeanaDataSource = datasources.getEuropeanaDatasource();
    europeanaAggr.setId(getEuropeanaAggregationId(entityId));
    // europeana datasource is checked on startup, so it cannot be empty here
    europeanaAggr.setRights(europeanaDataSource.get().getRights());
    europeanaAggr.setSource(europeanaDataSource.get().getUrl());
    europeanaAggr.setCreated(timestamp);
    europeanaAggr.setModified(timestamp);

    EntityProxy europeanaProxy = new EntityProxy();
    europeanaProxy.setProxyId(getEuropeanaProxyId(entityId));
    europeanaProxy.setProxyFor(entityId);
    europeanaProxy.setProxyIn(europeanaAggr);
    // update co-references
    addSameReferenceLinks(europeanaProxyMetadata, corefs);
    europeanaProxy.setEntity(europeanaProxyMetadata);

    entityRecord.addProxy(europeanaProxy);
  }

  private EntityProxy setExternalProxy(
      Entity metisResponse,
      String proxyId,
      String entityId,
      DataSource externalDatasource,
      EntityRecord entityRecord,
      Date timestamp,
      int aggregationId) {
    Aggregation datasourceAggr = new Aggregation();
    datasourceAggr.setId(getDatasourceAggregationId(entityId, aggregationId));
    datasourceAggr.setCreated(timestamp);
    datasourceAggr.setModified(timestamp);
    datasourceAggr.setRights(externalDatasource.getRights());
    datasourceAggr.setSource(externalDatasource.getUrl());

    EntityProxy datasourceProxy = new EntityProxy();
    datasourceProxy.setProxyId(proxyId);
    datasourceProxy.setProxyFor(entityId);
    datasourceProxy.setProxyIn(datasourceAggr);
    datasourceProxy.setEntity(metisResponse);

    entityRecord.addProxy(datasourceProxy);
    return datasourceProxy;
  }

  /**
   * Recreates the external proxy on an Entity, using the newProxyId value as its proxyId
   *
   * @param entityRecord entity record
   * @param newProxyId new proxyId value
   * @throws EuropeanaApiException on exception
   */
  public void changeExternalProxy(EntityRecord entityRecord, String newProxyId)
      throws EuropeanaApiException, EntityModelCreationException {

    DataSource dataSource = datasources.verifyDataSource(newProxyId, true);
    List<EntityProxy> externalProxies = entityRecord.getExternalProxies();

    if (externalProxies.size() > 1) {
      // Changing provenance isn't supported if entity has multiple external proxies (eg. for Zoho
      // orgs)
      throw new HttpBadRequestException("Changing provenance not supported for entity");
    }

    EntityProxy externalProxy = externalProxies.get(0);
    String entityType = externalProxy.getEntity().getType();

    entityRecord.getProxies().remove(externalProxy);

    setExternalProxy(
        EntityObjectFactory.createProxyEntityObject(entityType),
        newProxyId,
        entityRecord.getEntityId(),
        dataSource,
        entityRecord,
        new Date(),
        1);
  }

  /**
   * Adds the specified uris to the entity's exactMatch / sameAs
   *
   * @param entity entity to update
   * @param uris uris to add to entity's sameAs / exactMatch
   */
  public void addSameReferenceLinks(Entity entity, List<String> uris) {
    List<String> entitySameReferenceLinks = entity.getSameReferenceLinks();

    if (entitySameReferenceLinks == null) {
      // sameAs is mutable here as we might need to add more values to it later
      entity.setSameReferenceLinks(new ArrayList<>(uris));
      return;
    }

    // combine uris with existing sameReferenceLinks, minus duplicates
    entity.setSameReferenceLinks(
        Stream.concat(entitySameReferenceLinks.stream(), uris.stream())
            .distinct()
            .collect(Collectors.toList()));
  }
}
