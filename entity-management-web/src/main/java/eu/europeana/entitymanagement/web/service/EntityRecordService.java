package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaProxyId;
import static java.time.Instant.now;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.AppConfig;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.exception.EntityCreationException;
import eu.europeana.entitymanagement.common.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.common.exception.HttpUnprocessableException;
import eu.europeana.entitymanagement.common.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.common.service.BaseEntityRecordService;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.exception.EntityAlreadyExistsException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService extends BaseEntityRecordService {

  protected final EntityRecordRepository entityRecordRepository;

  protected final SolrService solrService;

  private static final Logger logger = LogManager.getLogger(EntityRecordService.class);

  @Autowired
  public EntityRecordService(
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources,
      SolrService solrService) {
    super(emConfiguration, datasources);
    this.entityRecordRepository = entityRecordRepository;
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

  public EntityRecord retrieveEntityRecord(String type, String identifier, boolean retrieveDisabled)
      throws EuropeanaApiException {
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    return retrieveEntityRecord(entityUri, retrieveDisabled);
  }

  public EntityRecord retrieveEntityRecord(String entityUri, boolean retrieveDisabled)
      throws EntityNotFoundException, EntityRemovedException {
    Optional<EntityRecord> entityRecordOptional = this.retrieveByEntityId(entityUri);
    if (entityRecordOptional.isEmpty()) {
      throw new EntityNotFoundException(entityUri);
    }

    EntityRecord entityRecord = entityRecordOptional.get();
    if (!retrieveDisabled && entityRecord.isDisabled()) {
      throw new EntityRemovedException(
          String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, entityUri));
    }
    return entityRecord;
  }

  /**
   * Retieves the Entities based on list of entity Ids
   *
   * @param entityIds
   * @param excludeDisabled
   * @return
   */
  public List<EntityIdDisabledStatus> retrieveMultipleByEntityId(
      List<String> entityIds, boolean excludeDisabled) {
    return entityRecordRepository.getEntityIds(entityIds, excludeDisabled);
  }

  /**
   * Saves the Entity Record to the DB
   *
   * @param er
   * @return
   */
  public EntityRecord saveEntityRecord(EntityRecord er) {
    return entityRecordRepository.save(er);
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

  public void dropRepository() {
    this.entityRecordRepository.dropCollection();
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

  public EntityRecord updateUsedForEnrichment(String type, String identifier, String action)
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
}
