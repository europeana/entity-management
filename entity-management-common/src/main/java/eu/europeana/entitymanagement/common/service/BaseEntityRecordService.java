package eu.europeana.entitymanagement.common.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.*;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.exception.EntityCreationException;
import eu.europeana.entitymanagement.common.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.*;
import eu.europeana.entitymanagement.utils.EMCollectionUtils;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

// @Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class BaseEntityRecordService {

  final EntityManagementConfiguration emConfiguration;

  protected final DataSources datasources;

  private static final Logger logger = LogManager.getLogger(BaseEntityRecordService.class);

  // Fields to be ignored during consolidation ("type" is final, so it cannot be updated)
  private static final List<String> ignoredMergeFields = List.of("type");

  @Autowired
  public BaseEntityRecordService(
      EntityManagementConfiguration emConfiguration, DataSources datasources) {
    this.emConfiguration = emConfiguration;
    this.datasources = datasources;
  }
  /**
   * Method to update the Entity consolidated version
   *
   * @param entityRecord
   * @param consolidatedEntity
   */
  public void updateConsolidatedVersion(EntityRecord entityRecord, Entity consolidatedEntity) {
    entityRecord.setEntity(consolidatedEntity);
    upsertEntityAggregation(entityRecord, consolidatedEntity.getEntityId(), new Date());
  }

  /**
   * Fetches the datsource from a external prody ID
   *
   * @param externalProxyId
   * @return
   * @throws EntityCreationException
   */
  private Optional<DataSource> getDataSource(String externalProxyId)
      throws EntityCreationException {
    Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(externalProxyId);
    if (externalDatasourceOptional.isEmpty()) {
      throw new EntityCreationException("No configured datasource for id " + externalProxyId);
    }
    return externalDatasourceOptional;
  }

  protected void upsertEntityAggregation(
      EntityRecord entityRecord, String entityId, Date timestamp) {
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

  protected EntityProxy setExternalProxy(
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
    for (Object second : secondaryArray) {
      if (!EMCollectionUtils.ifValueAlreadyExistsInList(
          Arrays.asList(primaryArray), second, doSloppyMatch(field.getName()))) {
        mergedAndOrdered.add(second);
      }
    }
    return mergedAndOrdered.toArray(Arrays.copyOf(primaryArray, 0));
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
        // check if value already exists in the primary list.
        if (!EMCollectionUtils.ifValueAlreadyExistsInList(
            listPrimaryObject, elemSecondaryList, doSloppyMatch(fieldName))) {
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
            && !EMCollectionUtils.ifValueAlreadyExistsInList(
                altLabelPrimaryValue, prefLabel.getValue(), true));
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

  private Map<Object, Object> initialiseObjectMap(Map<Object, Object> fieldValueObjectMap) {
    if (fieldValueObjectMap != null) {
      return new HashMap<>(fieldValueObjectMap);
    }
    return new HashMap<>();
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
          addToPrimaryList(field, fieldValuePrimaryObject, secondaryObjectListObject);
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
  /**
   * Add the secondary value in the primary list (if not already present)
   *
   * @param field
   * @param fieldValuePrimaryObject
   * @param secondaryObjectListObject
   */
  private void addToPrimaryList(
      Field field, List<Object> fieldValuePrimaryObject, Object secondaryObjectListObject) {
    // check if the secondary value already exists in primary List
    if (!EMCollectionUtils.ifValueAlreadyExistsInList(
        fieldValuePrimaryObject, secondaryObjectListObject, doSloppyMatch(field.getName()))) {
      fieldValuePrimaryObject.add(secondaryObjectListObject);
    }
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

  boolean isStringOrPrimitive(Class<?> fieldType) {
    return String.class.isAssignableFrom(fieldType)
        || fieldType.isPrimitive()
        || Float.class.isAssignableFrom(fieldType)
        || Integer.class.isAssignableFrom(fieldType);
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
}
