package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEuropeanaProxyId;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getIsAggregatedById;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.mongo.repository.VocabularyRepository;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.utils.EMCollectionUtils;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.utils.EntityRecordUtils;
import eu.europeana.entitymanagement.utils.UriValidator;
import eu.europeana.entitymanagement.vocabulary.EntityFieldsTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;

public class BaseEntityRecordService {

  final EntityRecordRepository entityRecordRepository;

  final VocabularyRepository vocabRepository;

  final EntityManagementConfiguration emConfiguration;

  final DataSources datasources;

  final SolrService solrService;

  final ZohoConfiguration zohoConfiguration;

  protected final Logger logger = LogManager.getLogger(getClass());

  // Fields to be ignored during consolidation ("type" is final)
  static final Set<String> ignoredMergeFields = Set.of(WebEntityFields.TYPE, WebEntityFields.IS_AGGREGATED_BY);


  protected BaseEntityRecordService(EntityRecordRepository entityRecordRepository,
      VocabularyRepository vocabRepository, EntityManagementConfiguration emConfiguration,
      ZohoConfiguration zohoConfiguration, DataSources datasources, SolrService solrService) {
    this.entityRecordRepository = entityRecordRepository;
    this.vocabRepository = vocabRepository;
    this.emConfiguration = emConfiguration;
    this.zohoConfiguration = zohoConfiguration;
    this.datasources = datasources;
    this.solrService = solrService;
  }

  /**
   * Gets that have at list one of the provided URIs present in co-references (sameAs or exactMatch
   * value in the Consolidated version). The current entity identified by the entityId, if not null
   * or empty is excluded. Exclude disabled indicates if the disabled entities should be included in
   * the response or not
   *
   * @param uris co-reference uris
   * @param entityId URI indicating the the record for the given entityId should not be retrieved as
   *        matchingCoreference
   * @param excludeDisabled indicated if the disabled entities should be filtered out or not
   * @return List of dupplicated entities or empty.
   */
  public List<EntityRecord> findEntitiesByCoreference(List<String> uris, String entityId,
      boolean excludeDisabled) {
    return entityRecordRepository.findEntitiesByCoreference(uris, entityId, excludeDisabled);
  }

  protected void addValueOrInternalReference(List<String> updatedReferences, String value) {
    if (value.startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI) || !UriValidator.isUri(value)) {
      // value is internal reference or string literal
      updatedReferences.add(value);
    } else {
      // value is external URI, replace it with internal reference if they are accessible
      // do not use disabled entities as they are not accessible anymore
      List<EntityRecord> records =
          findEntitiesByCoreference(Collections.singletonList(value), null, true);
      if (!records.isEmpty()) {
        // if the prevention of dupplication worked propertly, that we should find only one active
        // entry in the database
        updatedReferences.add(records.get(0).getEntityId());
      }
    }
  }


  /**
   * Merges metadata between two entities. This method performs a deep copy of the objects, for the
   * mutable (custom) field types.
   * 
   * @param primary Primary entity. Metadata from this entity takes precedence
   * @param secondary Secondary entity. Metadata from this entity is only used if no matching field
   *        is contained within the primary entity.
   * @param fieldsToCombine metadata fields to reconcile
   * @param accumulate if true, metadata from the secondary entity are added to the matching
   *        collection (eg. maps, lists and arrays) within the primary . If accumulate is false, the
   *        "primary" content overwrites the "secondary"
   */
  protected Entity combineEntities(Entity primary, Entity secondary, List<Field> fieldsToCombine,
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
          mergePrimitiveField(field, primary, secondary, consolidatedEntity);
        } else if (Date.class.isAssignableFrom(fieldType)) {
          mergeDateField(field, primary, secondary, consolidatedEntity);
        } else if (fieldType.isArray()) {
          Object[] mergedArray = mergeArrays(primary, secondary, field, accumulate);
          consolidatedEntity.setFieldValue(field, mergedArray);
        } else if (List.class.isAssignableFrom(fieldType)) {
          mergeListField(field, primary, secondary, consolidatedEntity, accumulate);
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

  @SuppressWarnings("unchecked")
  void mergeListField(Field field, Entity primary, Entity secondary, Entity consolidatedEntity,
      boolean accumulate) throws IllegalAccessException, EntityUpdateException {
    List<Object> fieldValuePrimaryObjectList = (List<Object>) primary.getFieldValue(field);
    List<Object> fieldValueSecondaryObjectList = (List<Object>) secondary.getFieldValue(field);
    mergeList(consolidatedEntity, fieldValuePrimaryObjectList, fieldValueSecondaryObjectList, field,
        accumulate);
  }

  void mergeDateField(Field field, Entity primary, Entity secondary, Entity consolidatedEntity)
      throws IllegalAccessException {
    Object fieldValuePrimaryObjectDate = primary.getFieldValue(field);
    Object fieldValueSecondaryObjectDate = secondary.getFieldValue(field);

    if (fieldValuePrimaryObjectDate != null) {
      consolidatedEntity.setFieldValue(field,
          new Date(((Date) fieldValuePrimaryObjectDate).getTime()));
    } else if (fieldValueSecondaryObjectDate != null) {
      consolidatedEntity.setFieldValue(field,
          new Date(((Date) fieldValueSecondaryObjectDate).getTime()));
    }
  }

  void mergePrimitiveField(Field field, Entity primary, Entity secondary, Entity consolidatedEntity)
      throws IllegalAccessException {
    Object fieldValuePrimaryObjectPrimitiveOrString = primary.getFieldValue(field);
    Object fieldValueSecondaryObjectPrimitiveOrString = secondary.getFieldValue(field);

    if (fieldValuePrimaryObjectPrimitiveOrString != null) {
      consolidatedEntity.setFieldValue(field, fieldValuePrimaryObjectPrimitiveOrString);
    } else if (fieldValueSecondaryObjectPrimitiveOrString != null) {
      consolidatedEntity.setFieldValue(field, fieldValueSecondaryObjectPrimitiveOrString);
    }
  }


  private void mergeCustomObjects(Entity primary, Entity secondary, Field field,
      Entity consolidatedEntity) throws IllegalAccessException, EntityUpdateException {
    Object primaryObj = primary.getFieldValue(field);
    Object secondaryObj = secondary.getFieldValue(field);
    if (primaryObj != null) {
      consolidatedEntity.setFieldValue(field, deepCopyOfObject(primaryObj));
    } else if (secondaryObj != null) {
      consolidatedEntity.setFieldValue(field, deepCopyOfObject(secondaryObj));
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
  protected void mergePrimarySecondaryListWitoutDuplicates(
      Map<Object, Object> fieldValuePrimaryObject, Object key, Map.Entry elemSecondary,
      String fieldName, Map<Object, Object> prefLabelsForAltLabels) throws EntityUpdateException {
    if (fieldValuePrimaryObject.containsKey(key)
        && List.class.isAssignableFrom(elemSecondary.getValue().getClass())) {
      List<Object> listSecondaryObject = (List<Object>) elemSecondary.getValue();
      List<Object> listPrimaryObject =
          deepCopyOfList((List<Object>) fieldValuePrimaryObject.get(key));
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
  protected boolean addValuesToAltLabel(Map<Object, Object> prefLabelsForAltLabels,
      Map<Object, Object> altLabelPrimaryObject, boolean altLabelPrimaryValueChanged)
      throws EntityUpdateException {
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


  protected boolean isFieldAltLabel(String fieldName) {
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

    if (!CollectionUtils.isEmpty(fieldValuePrimaryObject)
        && !CollectionUtils.isEmpty(fieldValueSecondaryObject) && accumulate) {
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

    if (deepCopyPrimaryArray.length == 0 && deepCopySecondaryArray.length == 0) {
      return deepCopyPrimaryArray;
    } else if (deepCopyPrimaryArray.length == 0) {
      return deepCopySecondaryArray;
    } else if (secondaryArray.length == 0 || !append) {
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


  /**
   * Deep copy of an object.
   * 
   * @param obj
   * @param isReference if the object is a reference to another object (in which case we keep the
   *        reference without deep copying)
   * @return
   * @throws EntityUpdateException
   */
  private Object deepCopyOfObject(Object obj) throws EntityUpdateException {
    if (obj == null || isStringOrPrimitive(obj.getClass())) {
      return obj;
    }

    try {
      return obj.getClass().getConstructor(obj.getClass()).newInstance(obj);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new EntityUpdateException(
          "Metadata consolidation failed due to illegal creation of the object copy by calling newInstance.",
          e);
    }

  }


  private Object[] deepCopyOfArray(Object[] input) throws EntityUpdateException {
    if (input == null || input.length == 0) {
      return new Object[0];
    }
    Object[] copy;
    if (isStringOrPrimitive(input[0].getClass())) {
      copy = input.clone();
    } else {
      copy = new Object[input.length];
      for (int i = 0; i < input.length; i++) {
        try {
          copy[i] =
              input[i].getClass().getDeclaredConstructor(input[i].getClass()).newInstance(input[i]);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new EntityUpdateException(
              "Metadata consolidation failed due to illegal creation of the object "
                  + "copy within an array by calling newInstance.",
              e);
        }
      }
    }
    return copy;
  }


  private List<Object> deepCopyOfList(List<Object> input) throws EntityUpdateException {
    if (input == null || input.isEmpty()) {
      return new ArrayList<>();
    }

    List<Object> copy;
    if (isStringOrPrimitive(input.get(0).getClass())) {
      copy = new ArrayList<Object>(input);
    } else {
      copy = new ArrayList<>(input.size());
      for (Object obj : input) {
        try {
          copy.add(obj.getClass().getDeclaredConstructor(obj.getClass()).newInstance(obj));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
          throw new EntityUpdateException(
              "Metadata consolidation failed due to illegal creation of the object "
                  + "copy within a list by calling newInstance.",
              e);
        }
      }
    }
    return copy;
  }


  @SuppressWarnings("unchecked")
  protected Map<Object, Object> deepCopyOfMap(Map<Object, Object> input)
      throws EntityUpdateException {
    if (input == null || input.isEmpty()) {
      return new HashMap<>();
    }

    Map<Object, Object> copy;
    Object mapFirstKey = input.entrySet().iterator().next().getKey();
    Object mapFirstValue = input.entrySet().iterator().next().getValue();
    // if both keys and values are of primitive type, no need for deep copy
    if (isStringOrPrimitive(mapFirstKey.getClass())
        && isStringOrPrimitive(mapFirstValue.getClass())) {
      copy = new HashMap<>(input);
    } else {
      copy = new HashMap<>(input.size());
      for (Map.Entry<Object, Object> entry : input.entrySet()) {
        Object keyDeepCopy = null;
        Object valueDeepCopy = null;
        if (List.class.isAssignableFrom(mapFirstKey.getClass())) {
          keyDeepCopy = deepCopyOfList((List<Object>) entry.getKey());
        } else {
          keyDeepCopy = deepCopyOfObject(entry.getKey());
        }

        if (List.class.isAssignableFrom(mapFirstValue.getClass())) {
          valueDeepCopy = deepCopyOfList((List<Object>) entry.getValue());
        } else {
          valueDeepCopy = deepCopyOfObject(entry.getValue());
        }
        copy.put(keyDeepCopy, valueDeepCopy);
      }
    }

    return copy;

  }


  protected Aggregation createNewAggregation(String entityId, Date timestamp) {
    Aggregation isAggregatedBy = new Aggregation();
    isAggregatedBy.setId(getIsAggregatedById(entityId));
    isAggregatedBy.setCreated(timestamp);
    isAggregatedBy.setModified(timestamp);
    return isAggregatedBy;
  }


  protected void setEuropeanaMetadata(Entity europeanaProxyMetadata, String entityId,
      List<String> corefs, EntityRecord entityRecord, Date timestamp) {
    Aggregation europeanaAggr = new Aggregation();
    Optional<DataSource> europeanaDataSource = datasources.getEuropeanaDatasource();


    europeanaAggr.setId(getEuropeanaAggregationId(entityId));
    // europeana datasource is checked on startup, so it cannot be empty here
    if (europeanaDataSource.isPresent()) {
      europeanaAggr.setRights(europeanaDataSource.get().getRights());
      europeanaAggr.setSource(europeanaDataSource.get().getUrl());
    }
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

  protected EntityProxy setExternalProxy(Entity metisResponse, String proxyId, String entityId,
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


  protected void processRoleReference(Organization org) {
    if (org.getEuropeanaRoleIds() != null && !org.getEuropeanaRoleIds().isEmpty()) {
      List<Vocabulary> vocabs = vocabRepository.findByUri(org.getEuropeanaRoleIds());
      if (vocabs.isEmpty()) {
        if (logger.isWarnEnabled()) {
          logger.warn(
              "No vocabularies with the uris: {} were found in the database. Cannot assign role reference to organization with id {}",
              org.getEuropeanaRoleIds(), org.getEntityId());
        }
      } else {
        org.setEuropeanaRoleRefs(vocabs);
      }
    }
  }


  protected void processCountryReference(Organization org) {
    // country reference
    if (StringUtils.isEmpty(org.getCountryId())) {
      return;
    }
    String europeanaCountryId = getEuropeanaCountryId(org);
    if (europeanaCountryId == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Dropping unsupported country id in consolidated entity version: {} -- {} ",
            org.getEntityId(), org.getCountryId());
      }
      org.setCountryId(null);
    } else {
      // replace wikidata country ids
      org.setCountryId(europeanaCountryId);
      // search reference
      EntityRecord orgCountry = entityRecordRepository.findByEntityId(europeanaCountryId, null);
      if (orgCountry != null) {
        org.setCountryRef(orgCountry);
      } else if (logger.isWarnEnabled()) {
        logger.warn(
            "No country found in database for the entity id: {}. Cannot assign country reference to organization with id {}",
            europeanaCountryId, org.getEntityId());
      }
    }
  }


  String getEuropeanaCountryId(Organization org) {
    if (EntityRecordUtils.isEuropeanaEntity(org.getCountryId())) {
      // country id is already europeana entity
      return org.getCountryId();
    }
    // drop all country ids except for the Europeana entities
    return null;
  }

  boolean isStringOrPrimitive(Class<?> fieldType) {
    return String.class.isAssignableFrom(fieldType) || ClassUtils.isPrimitiveOrWrapper(fieldType);
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

  @SuppressWarnings("unchecked")
  void mergeSkippedPrefLabels(Entity consolidatedEntity, Map<Object, Object> prefLabelsForAltLabels,
      List<Field> allEntityFields) throws IllegalAccessException, EntityUpdateException {
    /*
     * adding the preferred labels from the secondary object to the alternative labels of
     * consolidated object
     */
    if (prefLabelsForAltLabels.isEmpty()) {
      // nothing to merge
      return;
    }

    // get the alt label field
    Optional<Field> altLabelField =
        allEntityFields.stream().filter(field -> isFieldAltLabel(field.getName())).findFirst();

    if (altLabelField.isEmpty()) {
      // altLabel field not found
      if (logger.isWarnEnabled()) {
        logger.warn("altLabel field not found in list: {}", allEntityFields);
      }

      // skip
      return;
    }

    Map<Object, Object> altLabelConsolidatedMap =
        (Map<Object, Object>) consolidatedEntity.getFieldValue(altLabelField.get());
    Map<Object, Object> altLabelPrimaryObject = deepCopyOfMap(altLabelConsolidatedMap);
    boolean altLabelPrimaryValueChanged = false;
    altLabelPrimaryValueChanged = addValuesToAltLabel(prefLabelsForAltLabels, altLabelPrimaryObject,
        altLabelPrimaryValueChanged);
    if (altLabelPrimaryValueChanged) {
      consolidatedEntity.setFieldValue(altLabelField.get(), altLabelPrimaryObject);
    }
  }

  static boolean doSloppyMatch(String fieldName) {
    if (EntityFieldsTypes.hasTypeDefinition(fieldName)) {
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
    }
    // for all other cases
    return false;
  }
}
