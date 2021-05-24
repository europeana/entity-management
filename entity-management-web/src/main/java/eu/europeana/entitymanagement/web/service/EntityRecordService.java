package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.EUROPEANA_URL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.RIGHTS_CREATIVE_COMMONS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ENTITY_ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ENTITY_IDENTIFIER;
import static eu.europeana.entitymanagement.web.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.web.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.web.EntityRecordUtils.getEuropeanaProxyId;
import static eu.europeana.entitymanagement.web.EntityRecordUtils.getIsAggregatedById;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entitymanagement.exception.*;
import eu.europeana.entitymanagement.web.EntityRecordUtils;
import java.lang.reflect.Field;
import java.util.*;

import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dev.morphia.query.experimental.filters.Filter;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.model.EntityPreview;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

    Logger logger = LogManager.getLogger(getClass());

    private final EntityRecordRepository entityRecordRepository;

    final EntityManagementConfiguration emConfiguration;

    private final DataSources datasources;

	private static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has been removed";

	/**
	 * Fields to ignore when updating entities from user request
	 */
	private final List<String> UPDATE_FIELDS_TO_IGNORE = List
			.of(WebEntityFields.ID, WebEntityFields.IDENTIFIER, WebEntityFields.TYPE, ENTITY_ID, ENTITY_IDENTIFIER,
					WebEntityFields.IS_AGGREGATED_BY);

	@Autowired
    public EntityRecordService(EntityRecordRepository entityRecordRepository,
	    EntityManagementConfiguration emConfiguration, DataSources datasources) {
	this.entityRecordRepository = entityRecordRepository;
	this.emConfiguration = emConfiguration;
	this.datasources = datasources;
    }

    public Optional<EntityRecord> retrieveEntityRecordByUri(String entityUri) {
	return Optional.ofNullable(entityRecordRepository.findByEntityId(entityUri));
    }

	public EntityRecord retrieveEntityRecord(String type, String identifier)
			throws EuropeanaApiException {
		String entityUri = EntityRecordUtils.buildEntityIdUri(type, identifier);
		Optional<EntityRecord> entityRecordOptional = this.
				retrieveEntityRecordByUri(entityUri);
		if (entityRecordOptional.isEmpty()) {
			throw new EntityNotFoundException(entityUri);
		}

		EntityRecord entityRecord = entityRecordOptional.get();
		if (entityRecord.isDisabled()) {
			throw new EntityRemovedException(String.format(ENTITY_ID_REMOVED_MSG, entityUri));
		}
		return entityRecord;
	}

    /**
     * Gets coreferenced entity with the given id (sameAs or exactMatch value in the
     * Consolidated version)
     * 
     * @param id co-reference id
     * @return Optional containing matching record, or empty optional if none found.
     */
    public Optional<EntityRecord> findMatchingCoreference(String id) {
	return entityRecordRepository.findMatchingEntitiesByCoreference(id);
    }

    public EntityRecord saveEntityRecord(EntityRecord er) {
	return entityRecordRepository.save(er);
    }

	@SuppressWarnings("unchecked")
	public List<? extends EntityRecord> saveBulkEntityRecords(List<? extends EntityRecord> records) {
		// TODO: this is a code smell. Simplify EntityRecord vs EntityRecordImpl structure
		return entityRecordRepository.saveBulk((List<EntityRecord>) records);
	}

    public EntityRecord disableEntityRecord(EntityRecord er) {
	er.setDisabled(true);
	return saveEntityRecord(er);
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
	 * Creates an {@link EntityRecord} from an {@link EntityPreview}, which is then
	 * persisted.
	 * Note : This method is used for creating Entity for Migration requests
	 *
	 * @param entityCreationRequest
	 * @param type type of entity
	 * @param identifier id of entity
	 * @return Saved Entity record
	 * @throws EntityCreationException if an error occurs
	 */
	public EntityRecord createEntityFromMigrationRequest(EntityPreview entityCreationRequest, String type, String identifier)
			throws EntityCreationException, EntityAlreadyExistsException {
		// Fail quick if no datasource is configured
		Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(entityCreationRequest.getId());
		if (externalDatasourceOptional.isEmpty()) {
			throw new EntityCreationException("No configured datasource for entity " + entityCreationRequest.getId());
		}

		Date timestamp = new Date();
		Entity entity = EntityObjectFactory.createEntityObject(type);
		EntityRecord entityRecord = new EntityRecord();
		String entityId = generateEntityId(entity.getType(), identifier);
		// check if entity already exists
		// this is avoid MongoDb exception for duplicate key
		checkIfEntityAlreadyExists(entityId);

		entityRecord.setEntityId(entityId);
		entity.setEntityId(entityId);
		entityRecord.setEntity(entity);

		Entity europeanaProxyMetadata = EntityObjectFactory.createEntityObject(type);
		// copy metadata from request into entity
		europeanaProxyMetadata.setEntityId(entityId);
		europeanaProxyMetadata.setType(type);
		copyPreviewMetadata(europeanaProxyMetadata, entityCreationRequest);
		setEuropeanaMetadata(europeanaProxyMetadata, entityId, entityRecord, timestamp);

		// create metis Entity
		Entity metisEntity = EntityObjectFactory.createEntityObject(type);

		DataSource externalDatasource = externalDatasourceOptional.get();
		setExternalProxyMetadata(metisEntity, entityCreationRequest, entityId, externalDatasource, entityRecord, timestamp);

		setEntityAggregation(entityRecord, entityId, timestamp);
		return entityRecordRepository.save(entityRecord);
	}

	/**
     * Creates an {@link EntityRecord} from an {@link EntityPreview}, which is then
     * persisted.
     *
     * @param entityCreationRequest de-referenced XML response instance from Metis
     * @param metisResponse    Metis de-referencing response
     * @return Saved Entity record
     * @throws EntityCreationException if an error occurs
     */
    public EntityRecord createEntityFromRequest(EntityPreview entityCreationRequest, Entity metisResponse)
	    throws EntityCreationException {
	// Fail quick if no datasource is configured
	Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(entityCreationRequest.getId());
	if (externalDatasourceOptional.isEmpty()) {
		throw new EntityCreationException("No configured datasource for entity " + entityCreationRequest.getId());
	}

	Date timestamp = new Date();
	Entity entity = EntityObjectFactory.createEntityObject(metisResponse.getType());

	EntityRecord entityRecord = new EntityRecord();
	String entityId = generateEntityId(entity.getType(), null);
        entityRecord.setEntityId(entityId);
        entity.setEntityId(entityId);
        entityRecord.setEntity(entity);


        Entity europeanaProxyMetadata = EntityObjectFactory.createEntityObject(metisResponse.getType());
				// copy metadata from request into entity
				europeanaProxyMetadata.setEntityId(entityId);
				europeanaProxyMetadata.setType(metisResponse.getType());
				copyPreviewMetadata(europeanaProxyMetadata, entityCreationRequest);
        setEuropeanaMetadata(europeanaProxyMetadata, entityId, entityRecord, timestamp);

       
	DataSource externalDatasource = externalDatasourceOptional.get();
	setExternalProxyMetadata(metisResponse, entityCreationRequest, entityId, externalDatasource, entityRecord, timestamp);

	setEntityAggregation(entityRecord, entityId, timestamp);
	return entityRecordRepository.save(entityRecord);

    }

	/**
	 * Checks if Entity already exists
	 * @param entityId
	 * @throws EntityAlreadyExistsException
	 */
	private void checkIfEntityAlreadyExists(String entityId) throws EntityAlreadyExistsException {
	Optional<EntityRecord> entityRecordOptional = retrieveEntityRecordByUri(entityId);
	if (!entityRecordOptional.isEmpty()) {
		throw new EntityAlreadyExistsException(entityId);
	}
	}

	/**
	 * Copies metadata provided during Entity creation, into the created Entity
	 * @param entity entity
	 * @param entityCreationRequest entity creation request
	 */
	private void copyPreviewMetadata(Entity entity, EntityPreview entityCreationRequest) {
			entity.setSameAs(List.of(entityCreationRequest.getId()));
			entity.setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
			entity.setAltLabel(entityCreationRequest.getAltLabel());
			entity.setDepiction(entityCreationRequest.getDepiction());
	}

	/**
	 * generates the EntityId
	 * If entityId is present, generate entity id uri with entityId
	 * else generates a auto increment id
	 * ex: http://data.europeana.eu/<entitytype>/<entityId>
	 * OR  http://data.europeana.eu/<entitytype>/<dbId>
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

    /**
     * Checks if any of the resources in the SameAs field from Metis is alredy
     * known.
     * 
     * @param rdfResources list of SameAs resources
     * @return Optional containing EntityRecord, or empty Optional if none found
     */
    public Optional<EntityRecord> retrieveMetisCoreferenceSameAs(List<String> rdfResources) {
	for (String resource : rdfResources) {
	    Optional<EntityRecord> entityRecordOptional = retrieveEntityRecordByUri(resource);
	    if (entityRecordOptional.isPresent()) {
		return entityRecordOptional;
	    }
	}

	return Optional.empty();
    }

    public void performReferentialIntegrity(Entity entity) throws JsonMappingException, JsonProcessingException {

	//TODO: consider refactoring the implementation of this method by creating a new class (e.g. ReferentialIntegrityProcessor) 
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
	case Timespan:
	    performReferentialIntegrityTimespan((Timespan) entity);
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
	List<String> narrowerField = entity.getBroader();
	entity.setNarrower(replaceWithInternalReferences(narrowerField));

	// for the field related
	List<String> relatedField = entity.getRelated();
	entity.setRelated(replaceWithInternalReferences(relatedField));

    }

    private void performReferentialIntegrityAgent(Agent entity) {
	Map<String, List<String>> updatedField = null;
	// for the field placeOfBirth
	Map<String, List<String>> placeOfBirthField = entity.getPlaceOfBirth();
	if (placeOfBirthField != null) {
	    updatedField = replaceWithInternalReferences(placeOfBirthField);
	    entity.setPlaceOfBirth(updatedField);
	}
	// for the field placeOfDeath
	Map<String, List<String>> placeOfDeathField = entity.getPlaceOfDeath();
	if (placeOfDeathField != null) {
	    updatedField = replaceWithInternalReferences(placeOfDeathField);
	    entity.setPlaceOfDeath(updatedField);
	}
	// for the field professionOrOccupation
	List<String> professionOrOccupationField = entity.getProfessionOrOccupation();
	entity.setProfessionOrOccupation(replaceWithInternalReferences(professionOrOccupationField));
	
	// for the field hasMet
	List<String> hasMetField = entity.getHasMet();
	entity.setHasMet(replaceWithInternalReferences(hasMetField));
	
	// for the field hasMet
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

    private void performReferentialIntegrityTimespan(Timespan entity) {
	// for the field isNextInSequence
	List<String> isNextInSequenceField = entity.getIsNextInSequence();
	entity.setIsNextInSequence(replaceWithInternalReferences(isNextInSequenceField));

    }

    private Map<String, List<String>> replaceWithInternalReferences(Map<String, List<String>> originalReferences) {
	if(originalReferences == null) {
	    return null;
	}
	
	Map<String, List<String>> updatedReferenceMap = new HashMap<String, List<String>>();
	for (Map.Entry<String, List<String>> entry : originalReferences.entrySet()) {
	    List<String> updatedReferences = new ArrayList<String>();
	    
	    for (String value : entry.getValue()) {
		addValueOrInternalReference(updatedReferences, value);
	    }
	    
	    if(!updatedReferences.isEmpty()) {
		updatedReferenceMap.put(entry.getKey(), updatedReferences);
	    }
	}
	
	if(updatedReferenceMap.isEmpty()) {
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
	    //value is not a reference or it is an internal referece
	    updatedReferences.add(value);
	} else {
	    //value is external URI, replace it with internal reference if they are accessible
	    Optional<EntityRecord> record = findMatchingCoreference(value);
	    if (record.isPresent()) {
		updatedReferences.add(record.get().getEntityId());
	    }
	}
    }

    /**
     * This function merges the data from the entities of the entity record proxies
     * to the consilidated entity. TODO: see how to merge the Aggregation and
     * WebResource objects (currently only the fields that are not of the Class type
     * are merged)
     * 
     * @throws EntityCreationException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mergeEntity(EntityRecord entityRecord) throws EuropeanaApiException, IllegalAccessException {

	//TODO: consider refactoring of this implemeentation by creating a new class EntityReconciliator
	/*
	 * The primary entity corresponds to the entity in the Europeana proxy. The
	 * secondary entity corresponds to the entity in the external proxy.
	 */
	EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();
	EntityProxy externalProxy = entityRecord.getExternalProxy();
	if (europeanaProxy == null || externalProxy == null) {
	    return;
	}

	Entity primary = europeanaProxy.getEntity();
	Entity secondary = externalProxy.getEntity();

			List<Field> fieldsToCombine = new ArrayList<>();
			EntityUtils.getAllFields(fieldsToCombine, primary.getClass());


				Entity consolidatedEntity = combineEntities(primary, secondary, fieldsToCombine, true);

				/*
				 * isAggregatedBy isn't set on Europeana Proxy, so it won't be copied to the consolidatedEntity
				 * We add it separately here
 				 */
				    Aggregation aggregation = entityRecord.getEntity().getIsAggregatedBy();
				    aggregation.setModified(new Date());
				    consolidatedEntity.setIsAggregatedBy(aggregation);
						entityRecord.setEntity(consolidatedEntity);
		}

	/**
	 * Replaces Europeana proxy metadata with the provided entity metadata.
	 *
	 * EntityId and SameAs values are not affected
	 * @param updateRequestEntity entity to replace with
	 * @param entityRecord entity record
	 */
	public void replaceEuropeanaProxy(final Entity updateRequestEntity, EntityRecord entityRecord) {
		EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();

		List<String> sameAs = europeanaProxy.getEntity().getSameAs();
		String entityId = europeanaProxy.getEntity().getEntityId();

		// copy SameAs and EntityId from existing Europeana proxy metadata
		europeanaProxy.setEntity(updateRequestEntity);
		europeanaProxy.getEntity().setSameAs(sameAs);
		europeanaProxy.getEntity().setEntityId(entityId);
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

		List<Field> allFields = new ArrayList<>();
		EntityUtils.getAllFields(allFields, updateEntity.getClass());

		List<Field> filteredList = allFields.stream()
				.filter(field -> !UPDATE_FIELDS_TO_IGNORE.contains(field.getName()))
				.collect(Collectors.toUnmodifiableList());

		Entity europeanaProxyEntity = europeanaProxy.getEntity();
		/**
		 * updateEntity considered as "primary", since its values take precedence over existing metadata.
		 * We also overwrite collection fields, instead of concatenating them
 		 */

		Entity updatedEntity = combineEntities(updateEntity, europeanaProxyEntity,
				filteredList, false);

		// finally copy over ignored fields from the existing metadata
		List<Field> ignoredFields = allFields.stream()
				.filter(field -> UPDATE_FIELDS_TO_IGNORE.contains(field.getName()))
				.collect(Collectors.toUnmodifiableList());

		for (Field field : ignoredFields) {
			updatedEntity.setFieldValue(field, europeanaProxyEntity.getFieldValue(field));
		}

    	europeanaProxy.setEntity(updatedEntity);

	if (europeanaProxy.getProxyIn()!=null) {
		europeanaProxy.getProxyIn().setModified(new Date());
	}
}

	/**
	 * Reconciles metadata between two entities.
	 * @param primary Primary entity. Metadata from this entity takes precedence
	 * @param secondary Secondary entity. Metadata from this entity is only used if no matching field
	 *                  is contained within the primary entity.
	 * @param fieldsToCombine metadata fields to reconcile
	 * @param accumulate if true, metadata from the secondary entity are added to the matching collection (eg. maps, lists and arrays)
	 *               within the primary . If accumulate is false, the "primary"
	 *               content overwrites the "secondary"
	 * @return
	 * @throws EntityCreationException
	 * @throws IllegalAccessException
	 */
	private Entity combineEntities(Entity primary, Entity secondary, List<Field> fieldsToCombine, boolean accumulate)
			throws EntityCreationException, IllegalAccessException {
		Entity consolidatedEntity = EntityObjectFactory.createEntityObject(primary.getType());

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
					List<Object> fieldValueSecondaryObjectList = (List<Object>) secondary.getFieldValue(field);
					mergeList(consolidatedEntity, fieldValuePrimaryObjectList, fieldValueSecondaryObjectList, field, accumulate);

			} else if (isStringOrPrimitive(fieldType)) {
					Object fieldValuePrimaryObjectPrimitiveOrString = primary.getFieldValue(field);
					Object fieldValueSecondaryObjectPrimitiveOrString = secondary.getFieldValue(field);

					if (fieldValuePrimaryObjectPrimitiveOrString == null && fieldValueSecondaryObjectPrimitiveOrString != null) {
						consolidatedEntity.setFieldValue(field, fieldValueSecondaryObjectPrimitiveOrString);
					} else if (fieldValuePrimaryObjectPrimitiveOrString != null) {
						consolidatedEntity.setFieldValue(field, fieldValuePrimaryObjectPrimitiveOrString);
					}

			} else if (Date.class.isAssignableFrom(fieldType)) {
					Object fieldValuePrimaryObjectDate = primary.getFieldValue(field);
					Object fieldValueSecondaryObjectDate = secondary.getFieldValue(field);

					if (fieldValuePrimaryObjectDate == null && fieldValueSecondaryObjectDate != null) {
						consolidatedEntity.setFieldValue(field, new Date (((Date)fieldValueSecondaryObjectDate).getTime()));
					} else if (fieldValuePrimaryObjectDate != null) {
						consolidatedEntity.setFieldValue(field, new Date (((Date)fieldValuePrimaryObjectDate).getTime()));
					}

			} else if (Map.class.isAssignableFrom(fieldType)) {
					combineEntities(consolidatedEntity, primary, secondary, prefLabelsForAltLabels, field, fieldName, accumulate);

			}

				}

				mergeSkippedPrefLabels(consolidatedEntity, prefLabelsForAltLabels, fieldsToCombine);


		return consolidatedEntity;
	}

    boolean isStringOrPrimitive(Class<?> fieldType) {
//        System.out.println(fieldType + " is primitive: " + fieldType.isPrimitive());
        return String.class.isAssignableFrom(fieldType) || fieldType.isPrimitive() || Float.class.isAssignableFrom(fieldType) 
                || Integer.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType);
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
    void combineEntities(Entity consolidatedEntity, Entity primary, Entity secondary,
			Map<Object, Object> prefLabelsForAltLabels, Field field, String fieldName,
			boolean accumulate) throws IllegalAccessException {
	//TODO: refactor implemetation
	
	Map<Object, Object> fieldValuePrimaryObjectMap = (Map<Object, Object>) primary.getFieldValue(field);
	Map<Object, Object> fieldValueSecondaryObjectMap = (Map<Object, Object>) secondary
	    .getFieldValue(field);
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
		List<Object> listPrimaryObject = new ArrayList<>(
			(List<Object>) fieldValuePrimaryObject.get(key));
		boolean listPrimaryObjectChanged = false;
		for (Object elemSecondaryList : listSecondaryObject) {
		    if (!listPrimaryObject.contains(elemSecondaryList)) {
			listPrimaryObject.add(elemSecondaryList);
			if (listPrimaryObjectChanged == false)
			    listPrimaryObjectChanged = true;
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
    void mergeSkippedPrefLabels(Entity consilidatedEntity, Map<Object, Object> prefLabelsForAltLabels,
	    List<Field> allEntityFields) throws IllegalAccessException {
	/*
	 * adding the preferred labels from the secondary object to the alternative
	 * labels of consolidated object
	 */
	if (prefLabelsForAltLabels.size() > 0) {
	for (Field field : allEntityFields) {
	    String fieldName = field.getName();
	    if (fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label")) {
		Map<Object, Object> altLabelConsolidatedMap = (Map<Object, Object>) consilidatedEntity.getFieldValue(field);
		Map<Object, Object> altLabelPrimaryObject = null;
		if (altLabelConsolidatedMap != null)
		    altLabelPrimaryObject = new HashMap<>(altLabelConsolidatedMap);
		else
		    altLabelPrimaryObject = new HashMap<>();

		boolean altLabelPrimaryValueChanged = false;
		for (Map.Entry<Object, Object> prefLabel : prefLabelsForAltLabels.entrySet()) {
		    String keyPrefLabel = (String) prefLabel.getKey();
		    List<Object> altLabelPrimaryObjectList = (List<Object>) altLabelPrimaryObject
			    .get(keyPrefLabel);
		    List<Object> altLabelPrimaryValue = null;
		    if (altLabelPrimaryObjectList != null) {
			altLabelPrimaryValue = new ArrayList<>(altLabelPrimaryObjectList);
		    } else {
			altLabelPrimaryValue = new ArrayList<>();
		    }

		    if (altLabelPrimaryValue.size() == 0 || (altLabelPrimaryValue.size() > 0
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

    void mergeList(Entity consolidatedEntity, List<Object> fieldValuePrimaryObjectList,
				List<Object> fieldValueSecondaryObjectList, Field field, boolean accumulate) throws IllegalAccessException {
	List<Object> fieldValuePrimaryObject = null;
	List<Object> fieldValueSecondaryObject = null;
	if (fieldValuePrimaryObjectList != null) {
	fieldValuePrimaryObject = new ArrayList<Object>(fieldValuePrimaryObjectList);
	}
	if (fieldValueSecondaryObjectList != null) {
	fieldValueSecondaryObject = new ArrayList<Object>(fieldValueSecondaryObjectList);
	}

	if(fieldValuePrimaryObject != null && !accumulate){
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

    Object[] mergeArrays(Entity primary, Entity secondary, Field field, boolean append) throws IllegalAccessException {
	Object[] primaryArray = (Object[]) primary.getFieldValue(field);
	Object[] secondaryArray = (Object[]) secondary.getFieldValue(field);
	
	if(primaryArray == null && secondaryArray == null) {
	    return null;
	}else if(primaryArray == null) {
	    //return a clone of the secondary
	    return secondaryArray.clone();
	}else if(secondaryArray == null || !append) {
	    //return a clone of the primary if we're not appending
	    return primaryArray.clone();
	}
	
	//merge arrays
	Set<Object> mergedAndOrdered= new TreeSet<>(Arrays.asList(primaryArray));
	mergedAndOrdered.addAll(Arrays.asList(secondaryArray));
	
	return mergedAndOrdered.toArray(
		Arrays.copyOf(primaryArray, 0));
	
    }

    public void dropRepository() {
        this.entityRecordRepository.dropCollection();
    }


    public List<? extends EntityRecord> findEntitiesWithFilter(int start, int count, Filter[] queryFilters) {
        return this.entityRecordRepository.findWithFilters(start, count, queryFilters);
    }

    private void setEntityAggregation(EntityRecord entityRecord, String entityId, Date timestamp) {
        Aggregation isAggregatedBy = new Aggregation();
        isAggregatedBy.setId(getIsAggregatedById(entityId));
	isAggregatedBy.setCreated(timestamp);
	isAggregatedBy.setModified(timestamp);
	isAggregatedBy.setAggregates(
		Arrays.asList(getEuropeanaAggregationId(entityId), getDatasourceAggregationId(entityId)));

	entityRecord.getEntity().setIsAggregatedBy(isAggregatedBy);
    }


    private void setEuropeanaMetadata(
				Entity europeanaProxyMetadata,
				String entityId, EntityRecord entityRecord, Date timestamp) {
	Aggregation europeanaAggr = new Aggregation();
	europeanaAggr.setId(getEuropeanaAggregationId(entityId));
	europeanaAggr.setRights(RIGHTS_CREATIVE_COMMONS);
	europeanaAggr.setCreated(timestamp);
	europeanaAggr.setModified(timestamp);
	europeanaAggr.setSource(EUROPEANA_URL);

	EntityProxy europeanaProxy = new EntityProxy();
	europeanaProxy.setProxyId(getEuropeanaProxyId(entityId));
	europeanaProxy.setProxyFor(entityId);
	europeanaProxy.setProxyIn(europeanaAggr);

	europeanaProxy.setEntity(europeanaProxyMetadata);

	entityRecord.addProxy(europeanaProxy);
    }

    private void setExternalProxyMetadata(
				Entity metisResponse,
				EntityPreview entityCreationRequest, String entityId,
				DataSource externalDatasource, EntityRecord entityRecord, Date timestamp) {
	Aggregation datasourceAggr = new Aggregation();
	datasourceAggr.setId(getDatasourceAggregationId(entityId));
	datasourceAggr.setCreated(timestamp);
	datasourceAggr.setModified(timestamp);
	datasourceAggr.setRights(externalDatasource.getRights());
	datasourceAggr.setSource(externalDatasource.getUrl());

	EntityProxy datasourceProxy = new EntityProxy();
	datasourceProxy.setProxyId(entityCreationRequest.getId());
	datasourceProxy.setProxyFor(entityId);
	datasourceProxy.setProxyIn(datasourceAggr);
	datasourceProxy.setEntity(metisResponse);

	entityRecord.addProxy(datasourceProxy);
    }


}
