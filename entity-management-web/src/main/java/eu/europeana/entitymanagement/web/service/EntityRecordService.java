package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaProxyId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getIsAggregatedById;
import static java.time.Instant.now;
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
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.definitions.model.Country;
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
import eu.europeana.entitymanagement.utils.EMCollectionUtils;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

  private final EntityRecordRepository entityRecordRepository;

  final EntityManagementConfiguration emConfiguration;

  private final DataSources datasources;

  private final SolrService solrService;
  
  private final ZohoConfiguration zohoConfiguration; 
  
  private static final Logger logger = LogManager.getLogger(EntityRecordService.class);

  // Fields to be ignored during consolidation ("type" is final, so it cannot be updated)
  private static final List<String> ignoredMergeFields = List.of("type");

  @Autowired
  public EntityRecordService(EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      ZohoConfiguration zohoConfiguration,
      DataSources datasources,
      SolrService solrService) {
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.zohoConfiguration = zohoConfiguration; 
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

  public EntityRecord retrieveEntityRecord(EntityTypes type, String identifier,
      boolean retrieveDisabled) throws EuropeanaApiException {
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    return retrieveEntityRecord(entityUri, retrieveDisabled);
  }

  public EntityRecord retrieveEntityRecord(String entityUri, boolean retrieveDisabled)
      throws EntityNotFoundException, EntityRemovedException {
    Optional<EntityRecord> entityRecordOpt = this.retrieveByEntityId(entityUri);
    if (entityRecordOpt.isEmpty()) {
      throw new EntityNotFoundException(entityUri);
    }

    EntityRecord entityRecord = entityRecordOpt.get();
    if (!retrieveDisabled && entityRecord.isDisabled()) {
      throw new EntityRemovedException(
          String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, entityUri));
    }
    return entityRecord;
  }

  public List<EntityIdDisabledStatus> retrieveMultipleByEntityId(List<String> entityIds,
      boolean excludeDisabled) {
    return entityRecordRepository.getEntityIds(entityIds, excludeDisabled);
  }

  /**
   * Gets coreferenced entity with the given id (sameAs or exactMatch value in the Consolidated
   * version)
   *
   * @param uris co-reference uris
   * @param entityId indicating the the record for the given entityId should not be retrieved as
   *        matchingCoreference
   * @return Optional containing matching record, or empty optional if none found.
   */
  public Optional<EntityRecord> findEntityDupplicationByCoreference(List<String> uris,
      String entityId) {
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
    logger.debug("Deprecated {} entities: entityIds={}", updateResult.getModifiedCount(),
        entityIds);
  }

  /**
   * Mark entity record as disabled and remove from solr
   *
   * @param er the entity record
   * @param forceSolrCommit indicating if a solr commit should be explicitly (synchronuously)
   *        executed
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
    entityRecordRepository.save(er);
  }

  // update the inScheme field of the entities that refer to this scheme
  @Deprecated
  /**
   * @deprecated "need to call the update methods to keep data consistent"
   * @param scheme
   */
  private void updateEntitiesInScheme(ConceptScheme scheme) {
    if (scheme.getItems() == null) {
      return;
    }
    for (String entityUrl : scheme.getItems()) {
      Optional<EntityRecord> erOpt = retrieveByEntityId(entityUrl);
      if (erOpt.isEmpty()) {
        continue;
      }

      EntityRecord er = erOpt.get();
      List<String> inScheme = er.getEntity().getInScheme();
      if (inScheme != null) {
        inScheme.remove(scheme.getConceptSchemeId());
      }
      entityRecordRepository.save(er);
    }
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
   * @throws UnsupportedEntityTypeException
   */
  public EntityRecord createEntityFromMigrationRequest(Entity europeanaProxyEntity, String type,
      String identifier) throws EntityAlreadyExistsException, HttpBadRequestException,
      HttpUnprocessableException, EntityModelCreationException, UnsupportedEntityTypeException {

    String externalProxyId = europeanaProxyEntity.getEntityId();

    // Fail quick if no datasource is configured
    DataSource externalDatasource = datasources.verifyDataSource(externalProxyId, true);

    Date timestamp = new Date();

    Entity entity = EntityObjectFactory.createConsolidatedEntityObject(type);
    String entityId = generateEntityId(EntityTypes.getByEntityType(entity.getType()), identifier);
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
    setEuropeanaMetadata(europeanaProxyEntity, entityId, new ArrayList<>(List.of(externalProxyId)),
        entityRecord, timestamp);
    // external proxy second
    setExternalProxy(metisEntity, externalProxyId, entityId, externalDatasource, entityRecord,
        timestamp, 1);

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
   * @throws UnsupportedEntityTypeException
   */
  public EntityRecord createEntityFromRequest(
      Entity europeanaProxyEntity, Entity datasourceResponse, DataSource dataSource)
      throws EntityCreationException, UnsupportedEntityTypeException {

    // Fail quick if no datasource is configured
    String externalEntityId = europeanaProxyEntity.getEntityId();

    //prevent registration of organizations if id generation is not enabled
    boolean isZohoOrg = ZohoUtils.isZohoOrganization(externalEntityId, datasourceResponse.getType());
    if(isZohoOrg && !emConfiguration.isGenerateOrganizationEuropeanaId()) {
      throw new EntityCreationException("This instance is not allowed to register new Organizations. Registration of external entity refused: " + externalEntityId);
    }

    //genrerate id for the new entity
    String entityId = generateEntityId(datasourceResponse);

    Date timestamp = new Date();
    Entity entity;
    try {
      entity = EntityObjectFactory.createConsolidatedEntityObject(europeanaProxyEntity);
    } catch (EntityModelCreationException e) {
      throw new EntityCreationException(e.getMessage(), e);
    }
    
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
    
    if(isZohoOrg) {
      //register first the EuropeanaID in Zoho
      updateEuropeanaIDFieldInZoho(externalEntityId, entityId);  
    }
    
    //save object into the database
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
  public EntityProxy appendWikidataProxy(EntityRecord entityRecord, String wikidataProxyId,
      String entityType, Date timestamp) throws EntityCreationException {
    try {
      // entity metadata will be populated during update task
      Entity wikidataProxyEntity = EntityObjectFactory.createProxyEntityObject(entityType);

      Optional<DataSource> wikidataDatasource = getDataSource(wikidataProxyId);
      // exception is thrown in factory method if wikidataDatasource is empty
      int proxyNr = entityRecord.getProxies().size();
      EntityProxy wikidataProxy = setExternalProxy(wikidataProxyEntity, wikidataProxyId,
          entityRecord.getEntityId(), wikidataDatasource.get(), entityRecord, timestamp, proxyNr);

      // add wikidata uri to entity sameAs
      entityRecord.getEntity().addSameReferenceLink(wikidataProxyId);
      // add to entityIsAggregatedBy, use upsertMethod
      upsertEntityAggregation(entityRecord, entityType, timestamp);

      return wikidataProxy;
    } catch (EntityModelCreationException e) {
      throw new EntityCreationException(e.getMessage(), e);
    }
  }

  String generateEntityId(Entity datasourceResponse) throws UnsupportedEntityTypeException {
    // only in case of Zoho Organization use the provided id from de-referencing
    String entityId = null;
    EntityTypes type = EntityTypes.getByEntityType(datasourceResponse.getType());
    // now we generate the sequenced entityIds also for the organizations
    // if (isZohoOrg) {
    // // zoho id is mandatory and unique identifier for zoho Organizations
    // String zohoId = EntityRecordUtils.getIdFromUrl(datasourceResponse.getEntityId());
    // entityId = EntityRecordUtils.buildEntityIdUri(type, zohoId);
    // } else {
    // entityId = generateEntityId(type, null);
    // }
    return generateEntityId(type, null);
  }

  List<String> buildSameAsReferenceLinks(String externalProxyId, Entity datasourceResponse,
      Entity europeanaProxyEntity) {
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
    Optional<EntityRecord> entityRecordOpt = retrieveByEntityId(entityId);
    if (entityRecordOpt.isPresent()) {
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
  private String generateEntityId(EntityTypes entityType, String entityId) {
    if (entityId != null) {
      return EntityRecordUtils.buildEntityIdUri(entityType, entityId);
    } else {
      long dbId = entityRecordRepository.generateAutoIncrement(entityType.getEntityType());
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
  @Deprecated(since = "to remove deprecation when first used", forRemoval = false)
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
    List<Field> fieldsToCombine = EntityUtils.getAllFields(primary.getClass()).stream()
        .filter(f -> !ignoredMergeFields.contains(f.getName())).collect(Collectors.toList());
    return combineEntities(primary, secondary, fieldsToCombine, true);
  }

  public void updateConsolidatedVersion(EntityRecord entityRecord, Entity consolidatedEntity) {

    entityRecord.setEntity(consolidatedEntity);
    upsertEntityAggregation(entityRecord, consolidatedEntity.getEntityId(), new Date());
  }

  /**
   * Replaces Europeana proxy metadata with the provided entity metadata.
   *
   * <p>
   * EntityId and SameAs values are not affected
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
   * Merges metadata between two entities. This method performs a deep copy of the objects, 
   * for the mutable (custom) filed types.
   * 
   * @param primary Primary entity. Metadata from this entity takes precedence
   * @param secondary Secondary entity. Metadata from this entity is only used if no matching field
   *        is contained within the primary entity.
   * @param fieldsToCombine metadata fields to reconcile
   * @param accumulate if true, metadata from the secondary entity are added to the matching
   *        collection (eg. maps, lists and arrays) within the primary . If accumulate is false, the
   *        "primary" content overwrites the "secondary"
   */
  @SuppressWarnings("unchecked")
  private Entity combineEntities(Entity primary, Entity secondary, List<Field> fieldsToCombine,
      boolean accumulate) throws EuropeanaApiException, EntityModelCreationException {
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

        if (isStringOrPrimitive(fieldType)) {
          Object fieldValuePrimaryObjectPrimitiveOrString = primary.getFieldValue(field);
          Object fieldValueSecondaryObjectPrimitiveOrString = secondary.getFieldValue(field);

          if (fieldValuePrimaryObjectPrimitiveOrString != null) {
            consolidatedEntity.setFieldValue(field, fieldValuePrimaryObjectPrimitiveOrString);
          } else if (fieldValueSecondaryObjectPrimitiveOrString != null) {
            consolidatedEntity.setFieldValue(field, fieldValueSecondaryObjectPrimitiveOrString);
          }

        } else if (Date.class.isAssignableFrom(fieldType)) {
          Object fieldValuePrimaryObjectDate = primary.getFieldValue(field);
          Object fieldValueSecondaryObjectDate = secondary.getFieldValue(field);

          if (fieldValuePrimaryObjectDate != null) {
            consolidatedEntity.setFieldValue(field,
                new Date(((Date) fieldValuePrimaryObjectDate).getTime()));
          } else if (fieldValueSecondaryObjectDate != null) {
            consolidatedEntity.setFieldValue(field,
                new Date(((Date) fieldValueSecondaryObjectDate).getTime()));
          }
        } else if (fieldType.isArray()) {
          Object[] mergedArray = mergeArrays(primary, secondary, field, accumulate);
          consolidatedEntity.setFieldValue(field, mergedArray);
        } else if (List.class.isAssignableFrom(fieldType)) {
          List<Object> fieldValuePrimaryObjectList = (List<Object>) primary.getFieldValue(field);
          List<Object> fieldValueSecondaryObjectList =
              (List<Object>) secondary.getFieldValue(field);
          mergeList(consolidatedEntity, fieldValuePrimaryObjectList, fieldValueSecondaryObjectList,
              field, accumulate);
        } else if (Map.class.isAssignableFrom(fieldType)) {
          combineEntities(consolidatedEntity, primary, secondary, prefLabelsForAltLabels, field,
              fieldName, accumulate);
        } else {
          mergeCustomObjects(primary, secondary, field, consolidatedEntity);
        }
      }

      mergeSkippedPrefLabels(consolidatedEntity, prefLabelsForAltLabels, fieldsToCombine);

    } catch (IllegalAccessException e) {
      throw new EntityUpdateException(
          "Metadata consolidation failed to access required properties!", e);
    }

    return consolidatedEntity;
  }

  private void mergeCustomObjects(Entity primary, Entity secondary, Field field,
      Entity consolidatedEntity) throws IllegalAccessException, IllegalArgumentException, EntityUpdateException {
    Object primaryObj = primary.getFieldValue(field);
    Object secondaryObj = secondary.getFieldValue(field);
    if (primaryObj != null) {
      consolidatedEntity.setFieldValue(field, deepCopyOfObject(primaryObj));
    } else if (secondaryObj != null) {
      consolidatedEntity.setFieldValue(field, deepCopyOfObject(secondaryObj));
    } 
  }

  boolean isStringOrPrimitive(Class<?> fieldType) {
    return 
        String.class.isAssignableFrom(fieldType) 
        || fieldType.isPrimitive()
        || Float.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType)
        || Double.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType)
        || Byte.class.isAssignableFrom(fieldType) || Boolean.class.isAssignableFrom(fieldType)
        || Long.class.isAssignableFrom(fieldType);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  void combineEntities(Entity consolidatedEntity, Entity primary, Entity secondary,
      Map<Object, Object> prefLabelsForAltLabels, Field field, String fieldName, boolean accumulate)
      throws IllegalAccessException, EntityUpdateException {
    // TODO: refactor implemetation

    Map<Object, Object> fieldValuePrimaryObjectMap =
        (Map<Object, Object>) primary.getFieldValue(field);
    Map<Object, Object> fieldValueSecondaryObjectMap =
        (Map<Object, Object>) secondary.getFieldValue(field);
    Map<Object, Object> fieldValuePrimaryObject = deepCopyOfMap(fieldValuePrimaryObjectMap);
    Map<Object, Object> fieldValueSecondaryObject = deepCopyOfMap(fieldValueSecondaryObjectMap);

    if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)
        && !CollectionUtils.isEmpty(fieldValueSecondaryObject) && accumulate) {
      for (Map.Entry elemSecondary : fieldValueSecondaryObject.entrySet()) {
        Object key = elemSecondary.getKey();
        /*
         * if the map value is a list, merge the lists of the primary and the secondary object
         * without duplicates
         */
        mergePrimarySecondaryListWitoutDuplicates(fieldValuePrimaryObject, key, elemSecondary,
            fieldName, prefLabelsForAltLabels);
      }
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    } else if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    } else if (!CollectionUtils.isEmpty(fieldValueSecondaryObject)) {
      consolidatedEntity.setFieldValue(field, fieldValueSecondaryObject);
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
   * @throws EntityUpdateException 
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mergePrimarySecondaryListWitoutDuplicates(
      Map<Object, Object> fieldValuePrimaryObject, Object key, Map.Entry elemSecondary,
      String fieldName, Map<Object, Object> prefLabelsForAltLabels) throws EntityUpdateException {
    if (fieldValuePrimaryObject.containsKey(key)
        && List.class.isAssignableFrom(elemSecondary.getValue().getClass())) {      
      List<Object> listSecondaryObject = (List<Object>) elemSecondary.getValue();
      List<Object> listPrimaryObject = deepCopyOfList((List<Object>) fieldValuePrimaryObject.get(key));
      boolean listPrimaryObjectChanged = false;
      for (Object elemSecondaryList : listSecondaryObject) {
        // check if value already exists in the primary list.
        if (!EMCollectionUtils.ifValueAlreadyExistsInList(listPrimaryObject, elemSecondaryList,
            doSloppyMatch(fieldName))) {
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
    else if (fieldValuePrimaryObject.containsKey(key) && fieldName.toLowerCase().contains("pref")
        && fieldName.toLowerCase().contains("label")) {
      Object primaryObjectPrefLabel = fieldValuePrimaryObject.get(key);
      if (!primaryObjectPrefLabel.equals(elemSecondary.getValue())) {
        prefLabelsForAltLabels.put(key, elemSecondary.getValue());
      }
    } else if (!fieldValuePrimaryObject.containsKey(key)) {
      fieldValuePrimaryObject.put(key, elemSecondary.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  void mergeSkippedPrefLabels(Entity consilidatedEntity, Map<Object, Object> prefLabelsForAltLabels,
      List<Field> allEntityFields) throws IllegalAccessException, EntityUpdateException {
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
          Map<Object, Object> altLabelPrimaryObject = deepCopyOfMap(altLabelConsolidatedMap);
          boolean altLabelPrimaryValueChanged = false;
          altLabelPrimaryValueChanged = addValuesToAltLabel(prefLabelsForAltLabels,
              altLabelPrimaryObject, altLabelPrimaryValueChanged);
          if (altLabelPrimaryValueChanged) {
            consilidatedEntity.setFieldValue(field, altLabelPrimaryObject);
          }
          break;
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private boolean addValuesToAltLabel(Map<Object, Object> prefLabelsForAltLabels,
      Map<Object, Object> altLabelPrimaryObject, boolean altLabelPrimaryValueChanged) throws EntityUpdateException {
    for (Map.Entry<Object, Object> prefLabel : prefLabelsForAltLabels.entrySet()) {
      String keyPrefLabel = (String) prefLabel.getKey();
      List<Object> altLabelPrimaryObjectList =
          (List<Object>) altLabelPrimaryObject.get(keyPrefLabel);
      List<Object> altLabelPrimaryValue = deepCopyOfList(altLabelPrimaryObjectList);
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

  private boolean shouldValuesBeAddedToAltLabel(List<Object> altLabelPrimaryValue,
      Map.Entry<Object, Object> prefLabel) {
    return altLabelPrimaryValue.isEmpty() || (!altLabelPrimaryValue.isEmpty() && !EMCollectionUtils
        .ifValueAlreadyExistsInList(altLabelPrimaryValue, prefLabel.getValue(), true));
  }

  void mergeList(Entity consolidatedEntity, List<Object> fieldValuePrimaryObjectList,
      List<Object> fieldValueSecondaryObjectList, Field field, boolean accumulate)
      throws IllegalAccessException, EntityUpdateException {
    List<Object> fieldValuePrimaryObject = deepCopyOfList(fieldValuePrimaryObjectList);
    List<Object> fieldValueSecondaryObject = deepCopyOfList(fieldValueSecondaryObjectList);

    if (!CollectionUtils.isEmpty(fieldValuePrimaryObject) && !CollectionUtils.isEmpty(fieldValueSecondaryObject)
        && accumulate) {
        for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
          addToPrimaryList(field, fieldValuePrimaryObject, secondaryObjectListObject);
        }
        consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    } else if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    } else if (!CollectionUtils.isEmpty(fieldValueSecondaryObject)) {
      consolidatedEntity.setFieldValue(field, fieldValueSecondaryObject);
    }
  }

  /**
   * Add the secondary value in the primary list (if not already present)
   *
   * @param field
   * @param fieldValuePrimaryObject
   * @param secondaryObjectListObject
   */
  private void addToPrimaryList(Field field, List<Object> fieldValuePrimaryObject,
      Object secondaryObjectListObject) {
    // check if the secondary value already exists in primary List
    if (!EMCollectionUtils.ifValueAlreadyExistsInList(fieldValuePrimaryObject,
        secondaryObjectListObject, doSloppyMatch(field.getName()))) {
      fieldValuePrimaryObject.add(secondaryObjectListObject);
    }
  }

  Object[] mergeArrays(Entity primary, Entity secondary, Field field, boolean append)
      throws IllegalAccessException, EntityUpdateException {
    Object[] primaryArray = (Object[]) primary.getFieldValue(field);
    Object[] secondaryArray = (Object[]) secondary.getFieldValue(field);
    
    Object[] deepCopyPrimaryArray = deepCopyOfArray(primaryArray);
    Object[] deepCopySecondaryArray = deepCopyOfArray(secondaryArray);

    if (deepCopyPrimaryArray.length==0 && deepCopySecondaryArray.length==0) {
      return deepCopyPrimaryArray;
    } else if (deepCopyPrimaryArray.length==0) {
      return deepCopySecondaryArray;
    } else if (secondaryArray.length==0 || !append) {
      return deepCopyPrimaryArray;
    }
    // merge arrays
    Set<Object> mergedAndOrdered = new TreeSet<>(Arrays.asList(deepCopyPrimaryArray));
    for (Object second : deepCopySecondaryArray) {
      if (!EMCollectionUtils.ifValueAlreadyExistsInList(Arrays.asList(deepCopyPrimaryArray), second,
          doSloppyMatch(field.getName()))) {
        mergedAndOrdered.add(second);
      }
    }
    return mergedAndOrdered.toArray(Arrays.copyOf(deepCopyPrimaryArray, 0));
  }
  
  private Object deepCopyOfObject(Object obj) throws EntityUpdateException {
    if(obj==null) {
      return obj;
    }
    
    if(isStringOrPrimitive(obj.getClass())) {
      return obj;
    } else if(obj instanceof WebResource) {
      return new WebResource((WebResource) obj);
    } else if (obj instanceof Address) {
      return new Address((Address) obj);
    } else if (obj instanceof Country) {
      return new Country((Country) obj);
    } else {
      throw new EntityUpdateException("Metadata consolidation failed due to unknown object type!");
    }
  }

  private Object[] deepCopyOfArray(Object[] input) throws EntityUpdateException {    
    if(input==null || input.length==0) {
      return new Object[0];
    }
    
    Object[] copy;
    if(isStringOrPrimitive(input[0].getClass())) {
      copy=input.clone();
    }
    else {
      copy = new Object [input.length];
      if(input[0] instanceof WebResource) {        
        for(int i=0;i<input.length;i++) {
          copy[i]=new WebResource((WebResource) input[i]);
        }
      }
      else if(input[0] instanceof Address) {
        for(int i=0;i<input.length;i++) {
          copy[i]=new Address((Address) input[i]);
        }
      }
      else if(input[0] instanceof Country) {
        for(int i=0;i<input.length;i++) {
          copy[i]=new Country((Country) input[i]);
        }
      }
      else {
        throw new EntityUpdateException("Metadata consolidation failed due to unknown object type in array!");
      }
    }
    return copy;
  }
  
  private List<Object> deepCopyOfList(List<Object> input) throws EntityUpdateException {    
    if(input==null || input.size()==0) {
      return new ArrayList<>();
    }
    
    List<Object> copy;
    if(isStringOrPrimitive(input.get(0).getClass())) {
      copy=new ArrayList<Object>(input);
    }
    else {
      copy = new ArrayList<>(input.size());
      if(input.get(0) instanceof WebResource) {        
        for(int i=0;i<input.size();i++) {
          copy.add(new WebResource((WebResource) input.get(i)));
        }
      }
      else if(input.get(0) instanceof Address) {
        for(int i=0;i<input.size();i++) {
          copy.add(new Address((Address) input.get(i)));
        }
      }
      else if(input.get(0) instanceof Country) {
        for(int i=0;i<input.size();i++) {
          copy.add(new Country((Country) input.get(i)));
        }
      }
      else {
        throw new EntityUpdateException("Metadata consolidation failed due to unknown object type in list!");
      }
    }
    return copy;
  }
  
  private Map<Object, Object> deepCopyOfMap(Map<Object, Object> input) throws EntityUpdateException {
    if(input==null || input.size()==0) {
      return new HashMap<>();
    }
    
    Map<Object, Object> copy;
    Object mapFirstKey = input.keySet().stream().findFirst().get();
    Object mapFirstValue = input.values().stream().findFirst().get();
    //if both keys and values are of primitive type, no need for deep copy
    if(isStringOrPrimitive(mapFirstKey.getClass()) && isStringOrPrimitive(mapFirstValue.getClass())) {
      copy=new HashMap<>(input);
    }
    else {
      copy=new HashMap<>(input.size());
      for(Map.Entry<Object, Object> entry : input.entrySet()) {
        Object keyDeepCopy = null;
        Object valueDeepCopy = null;
        if(List.class.isAssignableFrom(mapFirstKey.getClass())) {
          keyDeepCopy = deepCopyOfList((List<Object>) entry.getKey());
        } else {
          keyDeepCopy = deepCopyOfObject(entry.getKey());
        }
        
        if(List.class.isAssignableFrom(mapFirstValue.getClass())) {
          valueDeepCopy = deepCopyOfList((List<Object>) entry.getValue());
        } else {
          valueDeepCopy = deepCopyOfObject(entry.getValue());
        }
        copy.put(keyDeepCopy, valueDeepCopy);
      }
    }

    return copy;
    
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

  private void updateEntityAggregatesList(Aggregation aggregation, EntityRecord entityRecord,
      String entityId) {
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

  private void setEuropeanaMetadata(Entity europeanaProxyMetadata, String entityId,
      List<String> corefs, EntityRecord entityRecord, Date timestamp) {
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

  private EntityProxy setExternalProxy(Entity metisResponse, String proxyId, String entityId,
      DataSource externalDatasource, EntityRecord entityRecord, Date timestamp, int aggregationId) {
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

    setExternalProxy(EntityObjectFactory.createProxyEntityObject(entityType), newProxyId,
        entityRecord.getEntityId(), dataSource, entityRecord, new Date(), 1);
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
    entity.setSameReferenceLinks(Stream.concat(entitySameReferenceLinks.stream(), uris.stream())
        .distinct().collect(Collectors.toList()));
  }

  public EntityRecord updateUsedForEnrichment(EntityTypes type, String identifier, String action)
      throws EuropeanaApiException {

    EntityRecord entityRecord = retrieveEntityRecord(type, identifier, false);

    // Set the “enrich” field on the Aggregation of the Consolidated Version
    // according to the value indicated in action parameter
    if (StringUtils.equals(action, WebEntityConstants.ACTION_ENABLE)) {
      entityRecord.getEntity().getIsAggregatedBy().setEnrich(Boolean.TRUE);
    }
    if (StringUtils.equals(action, WebEntityConstants.ACTION_DISABLE)) {
      entityRecord.getEntity().getIsAggregatedBy().setEnrich(Boolean.FALSE);
    }

    return update(entityRecord);
  }

  public static boolean doSloppyMatch(String fieldName) {
    String type = EntityFieldsTypes.getFieldType(fieldName);
    // for text do a sloppy match
    if (StringUtils.equals(type, EntityFieldsTypes.FIELD_TYPE_TEXT)) {
      return true;
    }
    // for uri or keywords do an exact match
    else if (StringUtils.equals(type, EntityFieldsTypes.FIELD_TYPE_URI)
        || StringUtils.equals(type, EntityFieldsTypes.FIELD_TYPE_KEYWORD)) {
      return false;
    }
    // for all other cases
    return false;
  }

  void updateEuropeanaIDFieldInZoho(String zohoOrganizationUrl,  String europeanaId) throws EntityCreationException {
    try {
        zohoConfiguration.getZohoAccessClient().updateZohoRecordOrganizationStringField(zohoOrganizationUrl, ZohoConstants.EUROPEANA_ID_FIELD, europeanaId);
    } catch (ZohoException e) {
      String message =
          "Updating EuropeanaID field in Zoho faild for Organization: "
              + zohoOrganizationUrl;
      throw new EntityCreationException(message, e);
    }
  }
}
