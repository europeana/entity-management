package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.*;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;
import static java.time.Instant.now;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.batch.utils.BatchUtils;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.exception.DatasourceNotKnownException;
import eu.europeana.entitymanagement.exception.EntityAlreadyExistsException;
import eu.europeana.entitymanagement.exception.EntityNotFoundException;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.zoho.utils.WikidataUtils;
import eu.europeana.entitymanagement.zoho.utils.ZohoUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

  private final EntityRecordRepository entityRecordRepository;

  final EntityManagementConfiguration emConfiguration;

  private final DataSources datasources;

  private static final Logger logger = LogManager.getLogger(EntityRecordService.class);

  private static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has been removed";

  /** Fields to ignore when updating entities from user request */
  private final List<String> UPDATE_FIELDS_TO_IGNORE =
      List.of(ID, TYPE, ENTITY_ID, IS_AGGREGATED_BY);

  @Autowired
  public EntityRecordService(
      EntityRecordRepository entityRecordRepository,
      EntityManagementConfiguration emConfiguration,
      DataSources datasources) {
    this.entityRecordRepository = entityRecordRepository;
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
  }

  public Optional<EntityRecord> retrieveByEntityId(String entityId) {
    return Optional.ofNullable(entityRecordRepository.findByEntityId(entityId));
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
   * @return Optional containing matching record, or empty optional if none found.
   */
  public Optional<EntityRecord> findMatchingCoreference(List<String> uris) {
    return entityRecordRepository.findMatchingEntitiesByCoreference(uris);
  }

  public EntityRecord saveEntityRecord(EntityRecord er) {
    return entityRecordRepository.save(er);
  }

  @SuppressWarnings("unchecked")
  public List<? extends EntityRecord> saveBulkEntityRecords(List<? extends EntityRecord> records) {
    return entityRecordRepository.saveBulk((List<EntityRecord>) records);
  }

  public void deleteBulk(List<? extends EntityRecord> entityRecords) {
    String[] entityIds = BatchUtils.getEntityIds(entityRecords);
    long deleteCount = entityRecordRepository.deleteBulk(List.of(entityIds));
    if (deleteCount > 0) {
      logger.info("Deleted {} entityRecords from database: entityIds={}", deleteCount, entityIds);
    }
  }

  public void disableBulk(List<? extends EntityRecord> entityRecords) {
    String[] entityIds = BatchUtils.getEntityIds(entityRecords);
    UpdateResult updateResult = entityRecordRepository.disableBulk(List.of(entityIds));
    logger.info("Deprecated {} entities: entityIds={}", updateResult.getModifiedCount(), entityIds);
  }

  public void disableEntityRecord(EntityRecord er) {
    er.setDisabled(true);
    saveEntityRecord(er);
  }

  /**
   * Re-Enable an already existing entity record.
   *
   * @param entityRecord entity record to update
   * @return Re-Enabled entity
   */
  public EntityRecord enableEntityRecord(EntityRecord entityRecord) {
    entityRecord.setDisabled(false);
    return saveEntityRecord(entityRecord);
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
   */
  public long delete(String entityId) {
    return entityRecordRepository.deleteForGood(entityId);
  }

  /**
   * Creates an {@link EntityRecord} from an {@link EntityPreview}, which is then persisted. Note :
   * This method is used for creating Entity for Migration requests
   *
   * @param entityCreationRequest
   * @param type type of entity
   * @param identifier id of entity
   * @return Saved Entity record
   * @throws EntityCreationException if an error occurs
   */
  public EntityRecord createEntityFromMigrationRequest(
      EntityPreview entityCreationRequest, String type, String identifier)
      throws EntityCreationException, EntityAlreadyExistsException {
    // Fail quick if no datasource is configured
    Optional<DataSource> externalDatasourceOptional = getDataSource(entityCreationRequest.getId());

    Date timestamp = new Date();
    Entity entity = EntityObjectFactory.createProxyEntityObject(type);
    EntityRecord entityRecord = new EntityRecord();
    String entityId = generateEntityId(entity.getType(), identifier);
    // check if entity already exists
    // this is avoid MongoDb exception for duplicate key
    checkIfEntityAlreadyExists(entityId);

    entityRecord.setEntityId(entityId);
    entity.setEntityId(entityId);
    /*
     * sameAs will be replaced during consolidation; however we set this here to prevent duplicate
     * registrations if consolidation fails
     */
    entity.setSameReferenceLinks(new ArrayList<>(List.of(entityCreationRequest.getId())));
    entityRecord.setEntity(entity);

    Entity europeanaProxyMetadata = EntityObjectFactory.createProxyEntityObject(type);
    // copy metadata from request into entity
    europeanaProxyMetadata.setEntityId(entityId);
    copyPreviewMetadata(europeanaProxyMetadata, entityCreationRequest);
    setEuropeanaMetadata(europeanaProxyMetadata, entityId, entityRecord, timestamp);

    // create metis Entity
    Entity metisEntity = EntityObjectFactory.createProxyEntityObject(type);

    DataSource externalDatasource = externalDatasourceOptional.get();
    setExternalProxyMetadata(
        metisEntity,
        entityCreationRequest.getId(),
        entityId,
        externalDatasource,
        entityRecord,
        timestamp);
    //		createExternalProxy(metisEntity, entityCreationRequest.getId(), entityId,
    // externalDatasource, entityRecord, timestamp);

    setEntityAggregation(entityRecord, entityId, timestamp);
    return entityRecordRepository.save(entityRecord);
  }

  /**
   * Creates an {@link EntityRecord} from an {@link EntityPreview}, which is then persisted.
   *
   * @param entityCreationRequest de-referenced XML response instance from Metis
   * @param datasourceResponse Entity obtained from de-referencing
   * @return Saved Entity record
   * @throws EntityCreationException if an error occurs
   */
  public EntityRecord createEntityFromRequest(
      EntityPreview entityCreationRequest, Entity datasourceResponse)
      throws EntityCreationException {
    // Fail quick if no datasource is configured
    String externalProxyId = entityCreationRequest.getId();
    Optional<DataSource> externalDatasourceOptional = getDataSource(externalProxyId);

    Date timestamp = new Date();
    Entity entity =
        EntityObjectFactory.createConsolidatedEntityObject(datasourceResponse.getType());

    boolean isZohoOrg = ZohoUtils.isZohoOrganization(externalProxyId, datasourceResponse.getType());

    EntityRecord entityRecord = new EntityRecord();
    // only in case of Zoho Organization use the provided id from de-referencing
    String entityId =
        isZohoOrg ? datasourceResponse.getEntityId() : generateEntityId(entity.getType(), null);

    entityRecord.setEntityId(entityId);
    entity.setEntityId(entityId);
    /*
     * sameAs will be replaced during consolidation; however we set this here to prevent duplicate
     * registrations if consolidation fails
     */

    entity.setSameReferenceLinks(new ArrayList<>(List.of(externalProxyId)));
    entityRecord.setEntity(entity);

    Entity europeanaProxyMetadata =
        EntityObjectFactory.createProxyEntityObject(datasourceResponse.getType());
    // copy metadata from request into entity
    europeanaProxyMetadata.setEntityId(entityId);
    copyPreviewMetadata(europeanaProxyMetadata, entityCreationRequest);
    setEuropeanaMetadata(europeanaProxyMetadata, entityId, entityRecord, timestamp);

    DataSource externalDatasource = externalDatasourceOptional.get();
    // create default external proxy
    setExternalProxyMetadata(
        datasourceResponse,
        entityCreationRequest.getId(),
        entityId,
        externalDatasource,
        entityRecord,
        timestamp);
    //	createExternalProxy(datasourceResponse, externalProxyId, entityId, externalDatasource,
    // entityRecord, timestamp);

    // for Zoho organizations, create second proxy for Wikidata metadata
    Optional<String> wikidataId;
    if (isZohoOrg
        && (wikidataId = WikidataUtils.getWikidataId(datasourceResponse.getSameReferenceLinks()))
            .isPresent()) {

      // entity metadata will be populated during update task
      Entity wikidataProxyEntity =
          EntityObjectFactory.createProxyEntityObject(datasourceResponse.getType());

      Optional<DataSource> wikidataDatasource = getDataSource(wikidataId.get());
      // exception is thrown in factory method if wikidataDatasource is empty
      setExternalProxyMetadata(
          wikidataProxyEntity,
          wikidataId.get(),
          entityId,
          wikidataDatasource.get(),
          entityRecord,
          timestamp);

      // add wikidata uri to entity sameAs
      entity.getSameReferenceLinks().add(wikidataId.get());
    }

    setEntityAggregation(entityRecord, entityId, timestamp);
    return entityRecordRepository.save(entityRecord);
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
   * Copies metadata provided during Entity creation, into the created Entity
   *
   * @param entity entity
   * @param entityCreationRequest entity creation request
   */
  private void copyPreviewMetadata(Entity entity, EntityPreview entityCreationRequest) {
    entity.setPrefLabel(entityCreationRequest.getPrefLabel());
    entity.setAltLabel(entityCreationRequest.getAltLabel());
    entity.setDepiction(entityCreationRequest.getDepiction());
  }

  /**
   * generates the EntityId If entityId is present, generate entity id uri with entityId else
   * generates a auto increment id ex: http://data.europeana.eu/<entitytype>/<entityId> OR
   * http://data.europeana.eu/<entitytype>/<dbId>
   *
   * @param entityType
   * @param entityId
   * @return
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
    if (!EntityUtils.isUri(value) || value.startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)) {
      // value is not a reference or it is an internal referece
      updatedReferences.add(value);
    } else {
      // value is external URI, replace it with internal reference if they are accessible
      Optional<EntityRecord> record = findMatchingCoreference(Collections.singletonList(value));
      record.ifPresent(entityRecord -> updatedReferences.add(entityRecord.getEntityId()));
    }
  }

  /**
   * This function merges the metadata data from the provided entities and returns the consolidated
   * version
   *
   * @throws EntityCreationException
   */
  public Entity mergeEntities(Entity primary, Entity secondary) throws EuropeanaApiException {

    // TODO: consider refactoring of this implemeentation by creating a new class
    // EntityReconciliator
    /*
     * The primary entity corresponds to the entity in the Europeana proxy. The
     * secondary entity corresponds to the entity in the external proxy.
     */
    List<Field> fieldsToCombine = EntityUtils.getAllFields(primary.getClass());
    return combineEntities(primary, secondary, fieldsToCombine, true);
  }

  public void updateConsolidatedVersion(EntityRecord entityRecord, Entity consolidatedEntity) {

    /*
     * isAggregatedBy isn't set on Europeana Proxy, so it won't be copied to the
     * consolidatedEntity We add it separately here
     */
    Aggregation aggregation = entityRecord.getEntity().getIsAggregatedBy();
    aggregation.setModified(new Date());
    consolidatedEntity.setIsAggregatedBy(aggregation);
    entityRecord.setEntity(consolidatedEntity);
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
   * Updates Europeana proxy metadata with the provided entity metadata.
   *
   * @param updateEntity entity to copy metadata from
   * @param entityRecord entity record
   * @throws Exception if error occurs
   */
  public void updateEuropeanaProxy(Entity updateEntity, EntityRecord entityRecord)
      throws Exception {
    EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();

    List<Field> allFields = EntityUtils.getAllFields(updateEntity.getClass());

    List<Field> filteredList =
        allFields.stream()
            .filter(field -> !UPDATE_FIELDS_TO_IGNORE.contains(field.getName()))
            .collect(Collectors.toUnmodifiableList());

    Entity europeanaProxyEntity = europeanaProxy.getEntity();
    /*
     * updateEntity considered as "primary", since its values take precedence over existing metadata.
     * We also overwrite collection fields, instead of concatenating them
     */

    Entity updatedEntity = combineEntities(updateEntity, europeanaProxyEntity, filteredList, false);

    // finally copy over ignored fields from the existing metadata
    List<Field> ignoredFields =
        allFields.stream()
            .filter(field -> UPDATE_FIELDS_TO_IGNORE.contains(field.getName()))
            .collect(Collectors.toUnmodifiableList());

    for (Field field : ignoredFields) {
      updatedEntity.setFieldValue(field, europeanaProxyEntity.getFieldValue(field));
    }

    europeanaProxy.setEntity(updatedEntity);

    if (europeanaProxy.getProxyIn() != null) {
      europeanaProxy.getProxyIn().setModified(new Date());
    }
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
  @SuppressWarnings("unchecked")
  private Entity combineEntities(
      Entity primary, Entity secondary, List<Field> fieldsToCombine, boolean accumulate)
      throws EuropeanaApiException {
    Entity consolidatedEntity =
        EntityObjectFactory.createConsolidatedEntityObject(primary.getType());

    try {

      /*
       * store the preferred label in the secondary entity that is different from the
       * preferred label in the primary entity to the alternative labels of the
       * consolidated entity
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
          WebResource primaryWebResource = (WebResource) primary.getFieldValue(field);
          WebResource secondaryWebResource = (WebResource) secondary.getFieldValue(field);
          if (primaryWebResource == null && secondaryWebResource != null) {
            consolidatedEntity.setFieldValue(field, new WebResource(secondaryWebResource));
          } else if (primaryWebResource != null) {
            consolidatedEntity.setFieldValue(field, new WebResource(primaryWebResource));
          }
        } else if (Address.class.isAssignableFrom(fieldType)) {
          Address primaryAddress = (Address) primary.getFieldValue(field);
          Address secondaryAddress = (Address) secondary.getFieldValue(field);
          if (primaryAddress == null && secondaryAddress != null) {
            consolidatedEntity.setFieldValue(field, new Address(secondaryAddress));
          } else if (primaryAddress != null) {
            consolidatedEntity.setFieldValue(field, new Address(primaryAddress));
          }
        }
      }

      mergeSkippedPrefLabels(consolidatedEntity, prefLabelsForAltLabels, fieldsToCombine);

    } catch (IllegalAccessException e) {
      throw new EntityUpdateException(
          "Metadata consolidation failed to access required properties!", e);
    }

    // Add external proxy id to consolidated entity sameAs / exactMatch
    String externalProxyId = secondary.getEntityId();
    List<String> consolidatedEntitySameRefs = consolidatedEntity.getSameReferenceLinks();

    if (consolidatedEntitySameRefs == null) {
      // sameAs is mutable here as we might need to add more values to it later
      consolidatedEntity.setSameReferenceLinks(new ArrayList<>(List.of(externalProxyId)));
    } else if (!consolidatedEntitySameRefs.contains(externalProxyId)) {
      consolidatedEntitySameRefs.add(externalProxyId);
    }

    return consolidatedEntity;
  }

  boolean isStringOrPrimitive(Class<?> fieldType) {
    //        System.out.println(fieldType + " is primitive: " + fieldType.isPrimitive());
    return String.class.isAssignableFrom(fieldType)
        || fieldType.isPrimitive()
        || Float.class.isAssignableFrom(fieldType)
        || Integer.class.isAssignableFrom(fieldType)
        || Integer.class.isAssignableFrom(fieldType);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
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
    Map<Object, Object> fieldValuePrimaryObject = null;
    Map<Object, Object> fieldValueSecondaryObject = null;
    if (fieldValuePrimaryObjectMap != null) {
      fieldValuePrimaryObject = new HashMap<>(fieldValuePrimaryObjectMap);
    }
    if (fieldValueSecondaryObjectMap != null) {
      fieldValueSecondaryObject = new HashMap<>(fieldValueSecondaryObjectMap);
    }

    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
      fieldValuePrimaryObject = new HashMap<>();
      fieldValuePrimaryObject.putAll(fieldValueSecondaryObject);
    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null && accumulate) {
      for (Map.Entry elemSecondary : fieldValueSecondaryObject.entrySet()) {
        Object key = elemSecondary.getKey();
        /*
         * if the map value is a list, merge the lists of the primary and the secondary
         * object without duplicates
         */
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
    }

    if (fieldValuePrimaryObject != null) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
    }
  }

  @SuppressWarnings("unchecked")
  void mergeSkippedPrefLabels(
      Entity consilidatedEntity,
      Map<Object, Object> prefLabelsForAltLabels,
      List<Field> allEntityFields)
      throws IllegalAccessException {
    /*
     * adding the preferred labels from the secondary object to the alternative
     * labels of consolidated object
     */
    if (prefLabelsForAltLabels.size() > 0) {
      for (Field field : allEntityFields) {
        String fieldName = field.getName();
        if (fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label")) {
          Map<Object, Object> altLabelConsolidatedMap =
              (Map<Object, Object>) consilidatedEntity.getFieldValue(field);
          Map<Object, Object> altLabelPrimaryObject = null;
          if (altLabelConsolidatedMap != null) {
            altLabelPrimaryObject = new HashMap<>(altLabelConsolidatedMap);
          } else {
            altLabelPrimaryObject = new HashMap<>();
          }

          boolean altLabelPrimaryValueChanged = false;
          for (Map.Entry<Object, Object> prefLabel : prefLabelsForAltLabels.entrySet()) {
            String keyPrefLabel = (String) prefLabel.getKey();
            List<Object> altLabelPrimaryObjectList =
                (List<Object>) altLabelPrimaryObject.get(keyPrefLabel);
            List<Object> altLabelPrimaryValue = null;
            if (altLabelPrimaryObjectList != null) {
              altLabelPrimaryValue = new ArrayList<>(altLabelPrimaryObjectList);
            } else {
              altLabelPrimaryValue = new ArrayList<>();
            }

            if (altLabelPrimaryValue.size() == 0
                || (altLabelPrimaryValue.size() > 0
                    && !altLabelPrimaryValue.contains(prefLabel.getValue()))) {
              altLabelPrimaryValue.add(prefLabel.getValue());
              if (altLabelPrimaryValueChanged == false) {
                altLabelPrimaryValueChanged = true;
              }

              altLabelPrimaryObject.put(keyPrefLabel, altLabelPrimaryValue);
            }
          }
          if (altLabelPrimaryValueChanged) {
            consilidatedEntity.setFieldValue(field, altLabelPrimaryObject);
          }
          break;
        }
      }
    }
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

    if (fieldValuePrimaryObject != null && !accumulate) {
      // we're not appending items, so just return the primary field value
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
      return;
    }

    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
      consolidatedEntity.setFieldValue(field, fieldValueSecondaryObject);
    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {

      for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
        if (!fieldValuePrimaryObject.contains(secondaryObjectListObject)) {
          fieldValuePrimaryObject.add(secondaryObjectListObject);
        }
      }

      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObject);
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

  public List<? extends EntityRecord> findEntitiesWithFilter(
      int start, int count, Filter[] queryFilters) {
    return this.entityRecordRepository.findWithFilters(start, count, queryFilters);
  }

  private void setEntityAggregation(EntityRecord entityRecord, String entityId, Date timestamp) {
    Aggregation isAggregatedBy = new Aggregation(true);
    isAggregatedBy.setId(getIsAggregatedById(entityId));
    isAggregatedBy.setCreated(timestamp);
    isAggregatedBy.setModified(timestamp);
    isAggregatedBy.setAggregates(
        Arrays.asList(getEuropeanaAggregationId(entityId), getDatasourceAggregationId(entityId)));

    entityRecord.getEntity().setIsAggregatedBy(isAggregatedBy);
  }

  private void setEuropeanaMetadata(
      Entity europeanaProxyMetadata, String entityId, EntityRecord entityRecord, Date timestamp) {
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

    europeanaProxy.setEntity(europeanaProxyMetadata);

    entityRecord.addProxy(europeanaProxy);
  }

  private void setExternalProxyMetadata(
      Entity metisResponse,
      String proxyId,
      String entityId,
      DataSource externalDatasource,
      EntityRecord entityRecord,
      Date timestamp) {
    Aggregation datasourceAggr = new Aggregation();
    datasourceAggr.setId(getDatasourceAggregationId(entityId));
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
  }

  /**
   * Recreates the external proxy on an Entity, using the newProxyId value as its proxyId
   *
   * @param entityRecord entity record
   * @param newProxyId new proxyId value
   * @throws EuropeanaApiException on exception
   */
  public void changeExternalProxy(EntityRecord entityRecord, String newProxyId)
      throws EuropeanaApiException {
    Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(newProxyId);
    if (externalDatasourceOptional.isEmpty()) {
      throw new DatasourceNotKnownException("No configured datasource for url " + newProxyId);
    }

    List<EntityProxy> externalProxies = entityRecord.getExternalProxies();

    if (externalProxies.size() > 1) {
      // Changing provenance isn't supported if entity has multiple external proxies (eg. for Zoho
      // orgs)
      throw new HttpBadRequestException("Changing provenance not supported for entity");
    }

    EntityProxy externalProxy = externalProxies.get(0);
    String entityType = externalProxy.getEntity().getType();

    entityRecord.getProxies().remove(externalProxy);

    setExternalProxyMetadata(
        EntityObjectFactory.createProxyEntityObject(entityType),
        newProxyId,
        entityRecord.getEntityId(),
        externalDatasourceOptional.get(),
        entityRecord,
        new Date());
  }
}
