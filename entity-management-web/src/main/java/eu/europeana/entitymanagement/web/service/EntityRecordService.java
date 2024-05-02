package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.solr.SolrUtils.createSolrEntity;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaAggregationId;
import static java.time.Instant.now;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.filters.Filter;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.EntityRecordFields;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.definitions.model.ZohoLabelUriMapping;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.exception.EntityAlreadyExistsException;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.exception.HttpUnprocessableException;
import eu.europeana.entitymanagement.exception.MultipleChoicesException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.mongo.repository.VocabularyRepository;
import eu.europeana.entitymanagement.solr.exception.SolrServiceException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityProfile;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;

@Service(AppConfigConstants.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService extends BaseEntityRecordService {

  @Autowired
  public EntityRecordService(EntityRecordRepository entityRecordRepository,
      VocabularyRepository vocabRepository, EntityManagementConfiguration emConfiguration,
      ZohoConfiguration zohoConfiguration, DataSources datasources, SolrService solrService) {

    super(entityRecordRepository, vocabRepository, emConfiguration, zohoConfiguration, datasources,
        solrService);
  }

  public boolean existsByEntityId(String entityId) {
    return entityRecordRepository.existsByEntityId(entityId);
  }

  /**
   * Return the entity record from the database, only for internal use in this class
   * Other classes should use use {@link #retrieveEntityRecord(String, String, boolean)} 
   * @param entityId
   * @param profiles
   * @return
   */
  private Optional<EntityRecord> retrieveByEntityId(String entityId) {
    return Optional.ofNullable(entityRecordRepository.findByEntityId(entityId, null));
  }

  /**
   * Retrieve multiple entities
   * 
   * @param entityIds entities to retrieve
   * @param excludeDisabled if disabled entities should be included
   * @param fetchFullRecord indicating if full record or only ids
   * @return
   */
  public List<EntityRecord> retrieveMultipleByEntityIds(List<String> entityIds,
      boolean excludeDisabled, boolean fetchFullRecord, String profiles) {
    List<EntityRecord> resp =
        entityRecordRepository.findByEntityIds(entityIds, excludeDisabled, fetchFullRecord);
    // for the organizations, populate dinamically generated fields
    if (fetchFullRecord && !resp.isEmpty()) {
      for (EntityRecord record : resp) {
        postProcessOrganizationRetrieval(profiles, record);
      }
    }
    return resp;
  }

  public List<EntityRecord> retrieveMultipleByEntityIdsOrCoreference(List<String> entityIds, String profiles) {
    List<EntityRecord> records = entityRecordRepository.findByEntityIdsOrCoreference(entityIds);
    // sorting the list in order of the input ids, and exclude duplicates
    List<EntityRecord> recordsSorted = new ArrayList<>();
    // for improved performance use a hashset to verify if the record was added to the sorted list
    Set<String> foundIds = new HashSet<>(entityIds.size());
    EntityRecord foundRecord;
    for (String id : entityIds) {
      Optional<EntityRecord> recordIdMatched = records.stream().filter(
          er -> id.equals(er.getEntityId()) || er.getEntity().getSameReferenceLinks().contains(id))
          .findFirst();
      // if the record was found and was not allready added to the sorted list
      if (recordIdMatched.isPresent() && !foundIds.contains(recordIdMatched.get().getEntityId())) {
        foundRecord = recordIdMatched.get();
        recordsSorted.add(foundRecord);
        //populate dynamic fields for organizations
        postProcessOrganizationRetrieval(profiles, foundRecord);
        foundIds.add(foundRecord.getEntityId());
      }
    }
    return recordsSorted;
  }

  public EntityRecord retrieveEntityRecord(EntityTypes type, String identifier, String profiles,
      boolean retrieveDisabled) throws EuropeanaApiException {
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    return retrieveEntityRecord(entityUri, profiles, retrieveDisabled);
  }

  public EntityRecord retrieveEntityRecord(String entityUri, String profiles,
      boolean retrieveDisabled) throws EntityNotFoundException, EntityRemovedException {
    Optional<EntityRecord> entityRecordOpt = this.retrieveByEntityId(entityUri);
    if (entityRecordOpt.isEmpty()) {
      throw new EntityNotFoundException(entityUri);
    }

    EntityRecord entityRecord = entityRecordOpt.get();
    if (!retrieveDisabled && entityRecord.isDisabled()) {
      throw new EntityRemovedException(
          String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, entityUri));
    }

    // the method checks if the record contains an organization
    postProcessOrganizationRetrieval(profiles, entityRecord);

    return entityRecord;
  }

  void postProcessOrganizationRetrieval(String profiles, EntityRecord entityRecord) {
    if (EntityTypes.isOrganization(entityRecord.getEntity().getType())) {
      // for the organizations, populate the aggregatesFrom field
      Organization org = (Organization) entityRecord.getEntity();
      
      //SG: Temporarily disabled, until data is available and performance tested
      //org.setAggregatesFrom(entityRecordRepository.findByAggregator(org.getEntityId()));

      // dereference morphia @Reference fields (e.g. the organization country)
      if (EntityProfile.hasDereferenceProfile(profiles)) {
        dereferenceLinkedEntities(org);
      }
    }
  }
  
  public void dereferenceLinkedEntities(Organization org) {
    // dereference country
    if (org.getCountryId() != null) {
      EntityRecord countryRecord = entityRecordRepository.findByEntityId(org.getCountryId(),
          new String[] {EntityRecordFields.ENTITY});
      setDereferencedCountry(org, countryRecord);
      
      ZohoLabelUriMapping mapping = emConfiguration.getCountryIdMappings().get(org.getCountryId());
      if(mapping != null) {
        //extract ISO code from ZohoCountry
        org.setCountryISO(mapping.getCountryISOCode());  
      }
    }
    // dereference role
    if (org.getEuropeanaRoleIds() != null && !org.getEuropeanaRoleIds().isEmpty()) {
      org.setEuropeanaRole(vocabRepository.findByUri(org.getEuropeanaRoleIds()));
    }
  }

  /**
   * Handle the redirection in the case that the requested entity is not found by searching within
   * the corefs This method allows redirection for old organization ids (which used the zoho
   * identifier before)
   * 
   * @param type of entity
   * @param identifier of entity
   * @param entityNotFoundException original exception to be re-throwned if no redirection is found
   * @param request - the original request
   * @return the id of enabled entity which has the requested id in corefs
   * @throws MultipleChoicesException in case of database inconsistencies indicating multiple
   *         alternatives
   * @throws EntityNotFoundException re-throwned original exception
   */
  public String getRedirectUriWhenNotFound(EntityTypes type, String identifier,
      EntityNotFoundException entityNotFoundException, HttpServletRequest request)
      throws MultipleChoicesException, EntityNotFoundException {
    String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
    // entity not found, search co-references by requested entity id
    List<EntityRecord> corefEntities =
        findEntitiesByCoreference(Collections.singletonList(entityUri), null, false);
    if (corefEntities.size() > 1) {
      throw new MultipleChoicesException(
          String.format(EntityRecordUtils.MULTIPLE_CHOICES_FOR_REDIRECTION_MSG, entityUri,
              EntityRecordUtils.getEntityIds(corefEntities).toString()));
    } else if (corefEntities.size() == 1) {
      // found alternative entity
      String redirectionEntityId = corefEntities.get(0).getEntityId();
      return EntityRecordUtils.buildRedirectionLocation(identifier, redirectionEntityId,
          request.getRequestURI(), request.getQueryString());
    } else {
      throw entityNotFoundException;
    }
  }

  /**
   * Redirection in case of deprecated entities
   * 
   * @param deprecatedEntity for which enabled entites are searched
   * @param identifier - entity identifier extracted from the original request
   * @param request - the web request
   * @return the entity id to redirect to
   * @throws EntityRemovedException if no entity is found to redirect to
   * @throws MultipleChoicesException if multiple candidates are found for redirection, typically
   *         generated by inconsistencies in the database
   */
  public String getRedirectUriWhenDeprecated(EntityRecord deprecatedEntity, String identifier,
      HttpServletRequest request) throws EntityRemovedException, MultipleChoicesException {
    // search by the entity id, using the corefs of the disabled entity
    List<String> allCorefEuropeanaIds = deprecatedEntity.getEntity().getSameReferenceLinks()
        .stream().filter(el -> el.startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI))
        .collect(Collectors.toList());
    List<EntityRecord> entitiesRedirect =
        retrieveMultipleByEntityIds(allCorefEuropeanaIds, true, false, null);
    if (entitiesRedirect.size() > 1) {
      throw new MultipleChoicesException(String.format(
          EntityRecordUtils.MULTIPLE_CHOICES_FOR_REDIRECTION_MSG, deprecatedEntity.getEntityId(),
          EntityRecordUtils.getEntityIds(entitiesRedirect).toString()));
    } else if (entitiesRedirect.size() == 1) {
      String redirectionEntityId = entitiesRedirect.get(0).getEntityId();
      return EntityRecordUtils.buildRedirectionLocation(identifier, redirectionEntityId,
          request.getRequestURI(), request.getQueryString());
    } else {
      throw new EntityRemovedException(
          String.format(EntityRecordUtils.ENTITY_ID_REMOVED_MSG, deprecatedEntity.getEntityId()));
    }
  }

  /**
   * returns the deprecation status (enabled/disabled) for the requested entities
   * 
   * @param entityIds entities to search for
   * @param excludeDisabled if disabled entities should be included or not in the response
   * @return the list of entity statuses
   */
  public List<EntityIdDisabledStatus> retrieveEntityDeprecationStatus(List<String> entityIds,
      boolean excludeDisabled) {
    return entityRecordRepository.getEntityIds(entityIds, excludeDisabled);
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
  void updateEntitiesInScheme(ConceptScheme scheme) {
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
    EntityRecord savedEntityRecord = saveEntityRecord(entityRecord);

    // entity needs to be added back to the solr index
    indexDereferencedEntity(savedEntityRecord);
  }

  /**
   * Method for reindexing the entity record. In case of organizations, the record is read again
   * from databases using dereference profile
   * 
   * @param entityRecord entity record
   * @throws EntityUpdateException thrown if the retrieval or indexing fails
   */
  public void indexDereferencedEntity(EntityRecord entityRecord) throws EntityUpdateException {
    try {
      EntityRecord recordToIndex;
      // for organizations we need dereference
      if (EntityTypes.isOrganization(entityRecord.getEntity().getType())) {
        recordToIndex = retrieveEntityRecord(entityRecord.getEntityId(),
            EntityProfile.dereference.name(), false);
      } else {
        recordToIndex = entityRecord;
      }
      solrService.storeEntity(createSolrEntity(recordToIndex));
    } catch (SolrServiceException | EntityNotFoundException | EntityRemovedException e) {
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

    updateEntityAggregation(entityRecord, entityId, timestamp);
    return entityRecordRepository.save(entityRecord);
  }

  /**
   * Creates an {@link EntityRecord} from an {@link Entity}, which is then persisted.
   *
   * @param europeanaProxyEntity Entity for the europeana proxy
   * @param datasourceResponse Entity obtained from de-referencing
   * @param dataSource the data source identified for the given entity id
   * @param predefinedEntityId previously generated entity ID to be reused for entity registration
   *        (e.g. see zoho EuropeanaID). Currently only supported for zoho organizations
   * @return Saved Entity record
   * @throws EntityCreationException if an error occurs
   * @throws UnsupportedEntityTypeException
   */
  public EntityRecord createEntityFromRequest(Entity europeanaProxyEntity,
      Entity datasourceResponse, DataSource dataSource, String predefinedEntityId)
      throws EntityCreationException, UnsupportedEntityTypeException {

    // Fail quick if no datasource is configured
    String externalEntityId = europeanaProxyEntity.getEntityId();

    // prevent registration of organizations if id generation is not enabled and now EuropeanaID
    // available in zoho
    boolean isZohoOrg = isZohoOrg(externalEntityId, datasourceResponse);
    if (isRegistrationRejected(predefinedEntityId, isZohoOrg)) {
      throw new EntityCreationException(
          "This instance is not allowed to register new Organizations. Registration of external entity refused: "
              + externalEntityId);
    }

    // generate id for the new entity
    String entityId;
    if (predefinedEntityId != null) {
      entityId = verifyPredefinedOrganizationEntityId(predefinedEntityId, isZohoOrg);
    } else {
      entityId = generateEntityId(datasourceResponse);
    }

    // build entity record object
    EntityRecord entityRecord = buildEntityRecordObject(entityId, europeanaProxyEntity,
        datasourceResponse, dataSource, externalEntityId, isZohoOrg);

    // if new generated organization ID, store it in zoho
    if (isZohoOrg && predefinedEntityId == null) {
      // register first the EuropeanaID in Zoho
      updateEuropeanaIDFieldInZoho(externalEntityId, entityId);
    }

    // save entity record object into the database
    return entityRecordRepository.save(entityRecord);
  }

  boolean isRegistrationRejected(String predefinedEntityId, boolean isZohoOrg) {
    return isZohoOrg && !emConfiguration.isGenerateOrganizationEuropeanaId()
        && predefinedEntityId == null;
  }

  EntityRecord buildEntityRecordObject(String entityId, Entity europeanaProxyEntity,
      Entity datasourceResponse, DataSource dataSource, String externalEntityId, boolean isZohoOrg)
      throws EntityCreationException {
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
    setEuropeanaMetadata(europeanaProxyEntity, entityId, dereferencedCorefs, entityRecord,
        timestamp);

    // create external proxy
    setExternalProxy(datasourceResponse, externalEntityId, entityId, dataSource, entityRecord,
        timestamp, 1);

    // create second external proxy for Zoho organizations with Wikidata
    Optional<String> wikidataId;
    if (isZohoOrg
        && (wikidataId = WikidataUtils.getWikidataId(datasourceResponse.getSameReferenceLinks()))
            .isPresent()) {

      String wikidataOrganizationId = wikidataId.get();
      // note appendWikidataProxy is also calling the upsertEntityAggregation method, which is
      // redundant with the next call,
      // for the simplicity of the code, we can leve the dupplicate call as everything is in memory
      appendWikidataProxy(entityRecord, wikidataOrganizationId, datasourceResponse.getType(),
          timestamp);

      // add the wikidata ID to europeana proxy entity
      europeanaProxyEntity.addSameReferenceLink(wikidataOrganizationId);
    }

    // create aggregation object
    updateEntityAggregation(entityRecord, entityId, timestamp);
    return entityRecord;
  }

  boolean isZohoOrg(String externalEntityId, Entity datasourceResponse) {
    return ZohoUtils.isZohoOrganization(externalEntityId, datasourceResponse.getType());
  }

  private String verifyPredefinedOrganizationEntityId(String predefinedEntityId, boolean isZohoOrg)
      throws EntityCreationException {

    // verification only allowed for zoho organizations with predefined entity ID
    if (!isZohoOrg || predefinedEntityId == null) {
      throw new EntityCreationException(
          "Predefined entity ids can be used only for registration of Zoho Organizations. Creation with predefined entity id refused: "
              + predefinedEntityId);
    }

    // only verify if this instance is allowed to generate ids
    if (emConfiguration.isGenerateOrganizationEuropeanaId()) {
      // need to prevent collision of entity ids
      long predefinedIdentifier =
          Long.parseLong(StringUtils.substringAfterLast(predefinedEntityId, "/"));
      long lastGeneratedId = entityRecordRepository
          .getLastGeneratedIdentifier(EntityTypes.Organization.getEntityType());

      if (lastGeneratedId < predefinedIdentifier || predefinedIdentifier < 1) {
        // predefined entity ID out of range
        throw new EntityCreationException(
            "Predefined entity id is out of range: " + predefinedEntityId
                + " identifier must be smaller onr equal than: " + lastGeneratedId);
      }
    }
    // this instance is either not generating organizations ids and has to reuse the provided one
    return predefinedEntityId;
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
      updateEntityAggregation(entityRecord, entityType, timestamp);

      return wikidataProxy;
    } catch (EntityModelCreationException e) {
      throw new EntityCreationException(e.getMessage(), e);
    }
  }

  String generateEntityId(Entity datasourceResponse) throws UnsupportedEntityTypeException {
    // only in case of Zoho Organization use the provided id from de-referencing
    return generateEntityId(EntityTypes.getByEntityType(datasourceResponse.getType()), null);
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
        .filter(f -> !ignoredMergeFields.contains(f.getName())).toList();
    return combineEntities(primary, secondary, fieldsToCombine, true);
  }

  public void updateConsolidatedVersion(EntityRecord entityRecord, Entity consolidatedEntity) {
    entityRecord.setEntity(consolidatedEntity);
    updateEntityAggregation(entityRecord, consolidatedEntity.getEntityId(), new Date());
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



  public void dropRepository() {
    this.entityRecordRepository.dropCollection();
  }

  /**
   * Fetches a record page matching the provided filter(s) and profile
   * Use dereference profile to retrieve dereferenced entities
   *
   * @param start the start index of the records page
   * @param limit the number of records to retrieve
   * @param queryFilters filters to be applied for retrieval
   * @param profiles the profiles to be applied
   * @return the list or retrieved records
   */
  public List<EntityRecord> findEntitiesWithFilter(int start, int limit, Filter[] queryFilters, String profiles) {
    List<EntityRecord> records = entityRecordRepository.find(start, limit, queryFilters);
    //need to post process organizations, when dereference is used 
    for (EntityRecord entityRecord : records) {
      postProcessOrganizationRetrieval(profiles, entityRecord);
    }
    return records;
  }

  private void updateEntityAggregation(EntityRecord entityRecord, String entityId, Date timestamp) {
    Aggregation aggregation = entityRecord.getEntity().getIsAggregatedBy();
    if (aggregation == null) {
      aggregation = createNewAggregation(entityId, timestamp);
      entityRecord.getEntity().setIsAggregatedBy(aggregation);
    } else {
      aggregation.setModified(timestamp);
    }

    updateEntityAggregatesList(aggregation, entityRecord, entityId);
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

  public EntityRecord updateUsedForEnrichment(EntityTypes type, String identifier, String profile,
      String action) throws EuropeanaApiException {

    EntityRecord entityRecord = retrieveEntityRecord(type, identifier, profile, false);

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

  void updateEuropeanaIDFieldInZoho(String zohoOrganizationUrl, String europeanaId)
      throws EntityCreationException {
    try {
      zohoConfiguration.getZohoAccessClient().updateZohoRecordOrganizationStringField(
          zohoOrganizationUrl, ZohoConstants.EUROPEANA_ID_FIELD, europeanaId);
      if (logger.isDebugEnabled()) {
        logger.debug("Updated organization id in Zoho got organization: {} - {}",
            zohoOrganizationUrl, europeanaId);
      }
    } catch (ZohoException e) {
      String message =
          "Updating EuropeanaID field in Zoho faild for Organization: " + zohoOrganizationUrl;
      throw new EntityCreationException(message, e);
    }
  }
 
}
