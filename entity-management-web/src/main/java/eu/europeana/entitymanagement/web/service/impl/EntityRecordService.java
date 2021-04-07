package eu.europeana.entitymanagement.web.service.impl;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.EUROPEANA_URL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.RIGHTS_CREATIVE_COMMONS;
import static eu.europeana.entitymanagement.web.service.impl.EntityRecordUtils.getDatasourceAggregationId;
import static eu.europeana.entitymanagement.web.service.impl.EntityRecordUtils.getEuropeanaAggregationId;
import static eu.europeana.entitymanagement.web.service.impl.EntityRecordUtils.getEuropeanaProxyId;
import static eu.europeana.entitymanagement.web.service.impl.EntityRecordUtils.getIsAggregatedById;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.morphia.query.experimental.filters.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
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

	EntityRecord entityRecord = new EntityRecordImpl();
	String entityId = generateEntityId(entity.getType());
        entityRecord.setEntityId(entityId);
        entity.setEntityId(entityId);
        entityRecord.setEntity(entity);


        Entity europeanaProxyMetadata = EntityObjectFactory.createEntityObject(metisResponse.getType());
				// copy metadata from request into entity
				copyPreviewMetadata(europeanaProxyMetadata, entityCreationRequest);
        setEuropeanaMetadata(europeanaProxyMetadata, entityId, entityRecord, timestamp);

       
	DataSource externalDatasource = externalDatasourceOptional.get();
	setDatasourceMetadata(metisResponse, entityCreationRequest, entityId, externalDatasource, entityRecord, timestamp);

	setEntityAggregation(entityRecord, entityId, timestamp);
	return entityRecordRepository.save(entityRecord);

    }

	/**
	 * Copies metadata provided during Entity creation, into the created Entity
	 * @param entity entity
	 * @param entityCreationRequest entity creation request
	 */
	private void copyPreviewMetadata(Entity entity, EntityPreview entityCreationRequest) {
			entity.setSameAs(new String[]{entityCreationRequest.getId()});
			entity.setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
			entity.setAltLabel(entityCreationRequest.getAltLabel());
			entity.setDepiction(entityCreationRequest.getDepiction());
	}

	private String generateEntityId(String entityType) {
	long dbId = entityRecordRepository.generateAutoIncrement(entityType);
	return EntityRecordUtils.buildEntityIdUri(entityType, String.valueOf(dbId));
    }

    /**
     * Checks if any of the resources in the SameAs field from Metis is alredy
     * known.
     * 
     * @param rdfResources list of SameAs resources
     * @return Optional containing EntityRecord, or empty Optional if none found
     */
    public Optional<EntityRecord> retrieveMetisCoreferenceSameAs(String[] rdfResources) {
	for (String resource : rdfResources) {
	    Optional<EntityRecord> entityRecordOptional = retrieveEntityRecordByUri(resource);
	    if (entityRecordOptional.isPresent()) {
		return entityRecordOptional;
	    }
	}

	return Optional.empty();
    }

    public void performReferentialIntegrity(Entity entity) throws JsonMappingException, JsonProcessingException {

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
    String[] updatedField = null;
	// for the field hasPart
	String[] hasPartField = entity.getHasPart();
	if(hasPartField!=null) {
		updatedField=replaceWithInternalReferences(hasPartField);
		entity.setHasPart(updatedField);
	}

	// for the field isPartOf
	String[] isPartOfField = entity.getIsPartOfArray();
	if(isPartOfField!=null)
	{
		updatedField=replaceWithInternalReferences(isPartOfField);
		entity.setIsPartOfArray(updatedField);
	}

	// for the field isRelatedTo
	String[] isRelatedToField = entity.getIsRelatedTo();
	if(isRelatedToField!=null) {
		updatedField=replaceWithInternalReferences(isRelatedToField);
		entity.setIsRelatedTo(updatedField);
	}
	

    }

    private void performReferentialIntegrityConcept(Concept entity) {
    String[] updatedField = null;
	// for the field broader
	String[] broaderField = entity.getBroader();
	if(broaderField!=null) {
		updatedField=replaceWithInternalReferences(broaderField);
		entity.setBroader(updatedField);
	}

	// for the field narrower
	String[] narrowerField = entity.getNarrower();
	if(narrowerField!=null) {
		updatedField=replaceWithInternalReferences(narrowerField);
		entity.setNarrower(updatedField);
	}	

	// for the field related
	String[] relatedField = entity.getRelated();
	if(relatedField!=null)
	{
		updatedField=replaceWithInternalReferences(relatedField);
		entity.setRelated(updatedField);
	}
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
	Map<String, List<String>> professionOrOccupationField = entity.getProfessionOrOccupation();
	if(professionOrOccupationField!=null) {
		updatedField=replaceWithInternalReferences(professionOrOccupationField);
		entity.setProfessionOrOccupation(updatedField);
	}
	
	String[] updatedField2 = null;
	// for the field hasMet
	String[] hasMetField = entity.getHasMet();
	if(hasMetField!=null) {
		updatedField2=replaceWithInternalReferences(hasMetField);
		entity.setHasMet(updatedField2);
	}	
	
	// for the field date
	String[] dateField = entity.getDate();
	if(dateField!=null) {
		updatedField2=replaceWithInternalReferences(dateField);
		entity.setDate(updatedField2);
	}

	// for the field wasPresentAt
	String[] wasPresentAtField = entity.getWasPresentAt();
	if(wasPresentAtField!=null) {
		updatedField2=replaceWithInternalReferences(wasPresentAtField);
		entity.setWasPresentAt(updatedField2);
	}

    }

    private void performReferentialIntegrityPlace(Place entity) {
    String[] updatedField = null;
	// for the field isNextInSequence
	String[] isNextInSequenceField = entity.getIsNextInSequence();
	if(isNextInSequenceField!=null) {
		updatedField=replaceWithInternalReferences(isNextInSequenceField);
		entity.setIsNextInSequence(updatedField);
	}
    }

    private void performReferentialIntegrityTimespan(Timespan entity) {
    String[] updatedField = null;
	// for the field isNextInSequence
	String[] isNextInSequenceField = entity.getIsNextInSequence();
	if(isNextInSequenceField!=null) {
		updatedField=replaceWithInternalReferences(isNextInSequenceField);
		entity.setIsNextInSequence(updatedField);
	}

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

    private String[] replaceWithInternalReferences(String[] originalReferences) {
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
	return updatedReferences.toArray(new String[updatedReferences.size()]);
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
    public void mergeEntity(EntityRecord entityRecord) throws EntityCreationException {

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
	Entity secondary = null;
	for (EntityProxy entityProxy : entityRecord.getProxies()) {
	    if (entityProxy.getProxyId() == null
		    || !entityProxy.getProxyId().equals(entityRecord.getEuropeanaProxy().getProxyId())) {
		secondary = entityProxy.getEntity();
		break;
	    }
	}

	try {
	    /*
	     * store the preferred label in the secondary entity that is different from the
	     * preferred label in the primary entity to the alternative labels of the
	     * consolidated entity
	     */
	    Map<Object, Object> prefLabelsForAltLabels = new HashMap<>();

	    List<Field> allEntityFields = new ArrayList<>();
	    EntityUtils.getAllFields(allEntityFields, primary.getClass());

	    for (Field field : allEntityFields) {

		Class<?> fieldType = field.getType();
		String fieldName = field.getName();

		if (fieldType.isArray()) {
		    Object[] fieldValuePrimaryObjectArray = (Object[]) primary.getFieldValue(field);
		    Object[] fieldValueSecondaryObjectArray = (Object[]) secondary.getFieldValue(field);
		    List<Object> fieldValuePrimaryObject = null;
		    List<Object> fieldValueSecondaryObject = null;
		    if (fieldValuePrimaryObjectArray != null) {
			fieldValuePrimaryObject = new ArrayList<>(Arrays.asList(fieldValuePrimaryObjectArray));
		    }
		    if (fieldValueSecondaryObjectArray != null) {
			fieldValueSecondaryObject = new ArrayList<>(Arrays.asList(fieldValueSecondaryObjectArray));
		    }

		    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
			fieldValuePrimaryObject = new ArrayList<>();
			fieldValuePrimaryObject.addAll(fieldValueSecondaryObject);
		    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
			// check the secondary object for new values that are not in the primary object
			for (Object secondaryElem : fieldValueSecondaryObject) {
			    if (!fieldValuePrimaryObject.contains(secondaryElem)) {
				fieldValuePrimaryObject.add(secondaryElem);
			    }
			}
		    }

		    if (fieldValuePrimaryObject != null) {
			entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject.toArray((Object[]) Array
				.newInstance(field.getType().getComponentType(), fieldValuePrimaryObject.size())));
		    }

		} else if (Map.class.isAssignableFrom(fieldType)) {
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
		    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
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
			entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject);
		    }

		} else if (List.class.isAssignableFrom(fieldType)) {

		    List<Object> fieldValuePrimaryObjectList = (List<Object>) primary.getFieldValue(field);
		    List<Object> fieldValueSecondaryObjectList = (List<Object>) secondary.getFieldValue(field);
		    List<Object> fieldValuePrimaryObject = null;
		    List<Object> fieldValueSecondaryObject = null;
		    if (fieldValuePrimaryObjectList != null) {
			fieldValuePrimaryObject = new ArrayList<Object>(fieldValuePrimaryObjectList);
		    }
		    if (fieldValueSecondaryObjectList != null) {
			fieldValueSecondaryObject = new ArrayList<Object>(fieldValueSecondaryObjectList);
		    }

		    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
			entityRecord.getEntity().setFieldValue(field, fieldValueSecondaryObject);
		    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {

			for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
			    if (!fieldValuePrimaryObject.contains(secondaryObjectListObject)) {
				fieldValuePrimaryObject.add(secondaryObjectListObject);
			    }
			}

			entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject);
		    }

		} else if (fieldType.isPrimitive() || String.class.isAssignableFrom(fieldType)) {
		    Object fieldValuePrimaryObjectPrimitiveOrString = primary.getFieldValue(field);
		    Object fieldValueSecondaryObjectPrimitiveOrString = secondary.getFieldValue(field);

		    if (fieldValuePrimaryObjectPrimitiveOrString == null && fieldValueSecondaryObjectPrimitiveOrString != null) {
			entityRecord.getEntity().setFieldValue(field, fieldValueSecondaryObjectPrimitiveOrString);
		    } else if (fieldValuePrimaryObjectPrimitiveOrString != null) {
			entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObjectPrimitiveOrString);
		    }
		    
		} else if (Date.class.isAssignableFrom(fieldType)) {
		    Object fieldValuePrimaryObjectDate = primary.getFieldValue(field);
		    Object fieldValueSecondaryObjectDate = secondary.getFieldValue(field);

		    if (fieldValuePrimaryObjectDate == null && fieldValueSecondaryObjectDate != null) {
			entityRecord.getEntity().setFieldValue(field, new Date (((Date)fieldValueSecondaryObjectDate).getTime()));
		    } else if (fieldValuePrimaryObjectDate != null) {
			entityRecord.getEntity().setFieldValue(field, new Date (((Date)fieldValuePrimaryObjectDate).getTime()));
		    }

		}

	    }

	    /*
	     * adding the preferred labels from the secondary object to the alternative
	     * labels of consolidated object
	     */
	    if (prefLabelsForAltLabels.size() > 0) {
		for (Field field : allEntityFields) {
		    String fieldName = field.getName();
		    if (fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label")) {
			Map<Object, Object> altLabelConsolidatedMap = (Map<Object, Object>) entityRecord.getEntity().getFieldValue(field);
			Map<Object, Object> altLabelPrimaryObject = null;
			if (altLabelConsolidatedMap != null)
			    altLabelPrimaryObject = new HashMap<>(altLabelConsolidatedMap);
			else
			    altLabelPrimaryObject = new HashMap<>();

			boolean altLabelPrimaryValueChanged = false;
			for (Map.Entry prefLabel : prefLabelsForAltLabels.entrySet()) {
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
			    entityRecord.getEntity().setFieldValue(field, altLabelPrimaryObject);
			}
			break;
		    }
		}
	    }
	} catch (IllegalArgumentException e) {
	    logger.error(
		    "During the reconceliation of the entity data from different sources a method has been passed an illegal or inappropriate argument.",
		    e);
	} catch (IllegalAccessException e) {
	    logger.error(
		    "During the reconceliation of the entity data from different sources an illegal access to some method or field has happened.",
		    e);
	}
    }

    public void dropRepository() {
        this.entityRecordRepository.dropCollection();
    }


    public List<? extends EntityRecord> findEntitiesWithFilter(int start, int count, Filter[] queryFilters) {
        return this.entityRecordRepository.findWithFilters(start, count, queryFilters);
    }

    private void setEntityAggregation(EntityRecord entityRecord, String entityId, Date timestamp) {
        Aggregation isAggregatedBy = new AggregationImpl();
        isAggregatedBy.setId(getIsAggregatedById(entityId));
	isAggregatedBy.setCreated(timestamp);
	isAggregatedBy.setModified(timestamp);
	isAggregatedBy.setRecordCount(1);
	isAggregatedBy.setAggregates(
		Arrays.asList(getEuropeanaAggregationId(entityId), getDatasourceAggregationId(entityId)));

	entityRecord.getEntity().setIsAggregatedBy(isAggregatedBy);
    }


    private void setEuropeanaMetadata(
				Entity europeanaProxyMetadata,
				String entityId, EntityRecord entityRecord, Date timestamp) {
	Aggregation europeanaAggr = new AggregationImpl();
	europeanaAggr.setId(getEuropeanaAggregationId(entityId));
	europeanaAggr.setRights(RIGHTS_CREATIVE_COMMONS);
	europeanaAggr.setCreated(timestamp);
	europeanaAggr.setModified(timestamp);
	europeanaAggr.setSource(EUROPEANA_URL);

	EntityProxy europeanaProxy = new EntityProxyImpl();
	europeanaProxy.setProxyId(getEuropeanaProxyId(entityId));
	europeanaProxy.setProxyFor(entityId);
	europeanaProxy.setProxyIn(europeanaAggr);

	europeanaProxy.setEntity(europeanaProxyMetadata);

	entityRecord.addProxy(europeanaProxy);
    }

    private void setDatasourceMetadata(
				Entity metisResponse,
				EntityPreview entityCreationRequest, String entityId,
				DataSource externalDatasource, EntityRecord entityRecord, Date timestamp) {
	Aggregation datasourceAggr = new AggregationImpl();
	datasourceAggr.setId(getDatasourceAggregationId(entityId));
	datasourceAggr.setCreated(timestamp);
	datasourceAggr.setModified(timestamp);
	datasourceAggr.setRights(externalDatasource.getRights());
	datasourceAggr.setSource(externalDatasource.getUrl());

	EntityProxy datasourceProxy = new EntityProxyImpl();
	datasourceProxy.setProxyId(entityCreationRequest.getId());
	datasourceProxy.setProxyFor(entityId);
	datasourceProxy.setProxyIn(datasourceAggr);
	datasourceProxy.setEntity(metisResponse);

	entityRecord.addProxy(datasourceProxy);
    }
}
