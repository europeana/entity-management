package eu.europeana.entitymanagement.web.service.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.web.model.EntityPreview;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

    Logger logger = LogManager.getLogger(getClass());

    @Resource(name = AppConfig.BEAN_ENTITY_RECORD_REPO)
    private EntityRecordRepository entityRecordRepository;

    @Resource(name = AppConfig.BEAN_EM_CONFIGURATION)
    EntityManagementConfiguration emConfiguration;

//    private EMSettings emSettings;

    public Optional<EntityRecord> retrieveEntityRecordByUri(String entityUri) {
	return Optional.ofNullable(entityRecordRepository.findByEntityId(entityUri));
    }

    public EntityRecord saveEntityRecord(EntityRecord er) {
	return entityRecordRepository.save(er);
    }
    
    public EntityRecord disableEntityRecord(EntityRecord er) {
    	er.setDisabled(true);
		return saveEntityRecord(er);
    }

    /**
     * Updates an already existing entity record.
     * @param entityRecord entity record to update
     * @return updated entity
     */
    public EntityRecord update (EntityRecord entityRecord){
    	return this.saveEntityRecord(entityRecord);
    }


    /**
     * Creates an {@link EntityRecord} from an {@link EntityPreview}, which
     * is then persisted.
     * 
     * @param entityCreationRequest         de-referenced XML response instance from Metis
     * @param entityCreationRequest entity request object
     * @param externalEntityType
	 * @return Saved Entity record
     * @throws EntityCreationException if an error occurs
     */
    public EntityRecord createEntityFromRequest(EntityPreview entityCreationRequest, String externalEntityType)
	    throws EntityCreationException {
	Entity entity = EntityObjectFactory.createEntityObject(externalEntityType);

	entity.setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
	entity.setAltLabel(entityCreationRequest.getAltLabel());

	// TODO: add proxies and aggregations

	EntityRecord entityRecord = new EntityRecordImpl();
	entityRecord.setEntity(entity);

	return entityRecordRepository.save(entityRecord);

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

    /**
     * This function merges the data from the entities of the entity record proxies to the consilidated entity.
     * TODO: see how to merge the Aggregation and WebResource objects (currently only the fields that are not of the Class type are merged)
     * @throws EntityCreationException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mergeEntity(EntityRecord entityRecord) throws EntityCreationException {

    	/*
    	 * The primary entity corresponds to the entity in the Europeana proxy.
    	 * The secondary entity corresponds to the entity in the external proxy.
    	 */
    	EntityProxy europeanaProxy = entityRecord.getEuropeanaProxy();
    	EntityProxy externalProxy = entityRecord.getExternalProxy();
    	if (europeanaProxy!=null && externalProxy==null) {
    		Entity consolidatedEntity = EntityObjectFactory.createEntityObjectFromCopy(europeanaProxy.getEntity());
    		entityRecord.setEntity(consolidatedEntity);  
    		return;
    	}
    	else if (europeanaProxy==null && externalProxy!=null) {
    		Entity consolidatedEntity = EntityObjectFactory.createEntityObjectFromCopy(externalProxy.getEntity());
    		entityRecord.setEntity(consolidatedEntity); 
    		return;
    	}
    	else if (europeanaProxy==null && externalProxy==null) {
    		return;
    	}
    	
    	Entity consolidatedEntity = EntityObjectFactory.createEntityObject(europeanaProxy.getEntity().getType());
    	entityRecord.setEntity(consolidatedEntity);
    	
    	Entity primary = europeanaProxy.getEntity();
    	Entity secondary = null;
    	for (EntityProxy entityProxy : entityRecord.getProxies()) {
    		if (entityProxy.getProxyId()==null || !entityProxy.getProxyId().equals(entityRecord.getEuropeanaProxy().getProxyId())) {
    			secondary = entityProxy.getEntity();
    			break;
    		}
    	}
    	
    	
		try {
		    /*
		     * store the preferred label in the secondary entity that is different from the preferred label in the primary entity to the alternative labels of the consolidated entity
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
		
				    if(fieldValuePrimaryObject!=null) {
				    	entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject.toArray((Object[]) Array.newInstance(field.getType().getComponentType(), fieldValuePrimaryObject.size())));
				    }
				    
				} else if (Map.class.isAssignableFrom(fieldType)) {
				    Map<Object, Object> fieldValuePrimaryObjectMap = (Map<Object, Object>) primary.getFieldValue(field);
				    Map<Object, Object> fieldValueSecondaryObjectMap = (Map<Object, Object>) secondary.getFieldValue(field);
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
				    } 
				    else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
						for (Map.Entry elemSecondary : fieldValueSecondaryObject.entrySet()) {
						    Object key = elemSecondary.getKey();
						    /*
						     * if the map value is a list, merge the lists of the primary and the secondary object without duplicates
						     */
						    if (fieldValuePrimaryObject.containsKey(key) && List.class.isAssignableFrom(elemSecondary.getValue().getClass())) {
								List<Object> listSecondaryObject = (List<Object>) elemSecondary.getValue();
								List<Object> listPrimaryObject = new ArrayList<>((List<Object>) fieldValuePrimaryObject.get(key));
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
						    // keep the different preferred labels in the secondary object for the alternative label in the consolidated object
						    else if (fieldValuePrimaryObject.containsKey(key) && fieldName.toLowerCase().contains("pref") && fieldName.toLowerCase().contains("label")) {
								Object primaryObjectPrefLabel = fieldValuePrimaryObject.get(key);
								if (!primaryObjectPrefLabel.equals(elemSecondary.getValue())) {
								    prefLabelsForAltLabels.put(key, elemSecondary.getValue());
								}
						    } 
						    else if (!fieldValuePrimaryObject.containsKey(key)) {
								fieldValuePrimaryObject.put(key, elemSecondary.getValue());
						    }
			
						}
				    }
				    
					if(fieldValuePrimaryObject!=null) {
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
				    } 
				    else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
		
						for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
						    if (!fieldValuePrimaryObject.contains(secondaryObjectListObject)) {
						    	fieldValuePrimaryObject.add(secondaryObjectListObject);
						    }
						}
			
						entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject);
				    }
		
				} else if (fieldType.isPrimitive()) {
				    Object fieldValuePrimaryObject = primary.getFieldValue(field);
				    Object fieldValueSecondaryObject = secondary.getFieldValue(field);
		
				    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
				    	entityRecord.getEntity().setFieldValue(field, fieldValueSecondaryObject);
				    }
				    else if (fieldValuePrimaryObject != null) {
				    	entityRecord.getEntity().setFieldValue(field, fieldValuePrimaryObject);
				    }
				}
	
		    }
	
		    /*
		     * adding the preferred labels from the secondary object to the alternative labels of consolidated object
		     */
		    if (prefLabelsForAltLabels.size() > 0) {
				for (Field field : allEntityFields) {
				    String fieldName = field.getName();
				    if (fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label")) {
					Map<Object, Object> altLabelPrimaryObjectMap = (Map<Object, Object>) primary.getFieldValue(field);
					Map<Object, Object> altLabelPrimaryObject = null;
					if (altLabelPrimaryObjectMap != null)
					    altLabelPrimaryObject = new HashMap<>(altLabelPrimaryObjectMap);
					else
					    altLabelPrimaryObject = new HashMap<>();
		
					boolean altLabelPrimaryValueChanged = false;
					for (Map.Entry prefLabel : prefLabelsForAltLabels.entrySet()) {
					    String keyPrefLabel = (String) prefLabel.getKey();
					    List<Object> altLabelPrimaryObjectList = (List<Object>) altLabelPrimaryObject.get(keyPrefLabel);
					    List<Object> altLabelPrimaryValue = null;
					    if (altLabelPrimaryObjectList != null) {
					    	altLabelPrimaryValue = new ArrayList<>(altLabelPrimaryObjectList);
					    }
					    else {
					    	altLabelPrimaryValue = new ArrayList<>();
					    }
		
					    if (altLabelPrimaryValue.size() == 0 || (altLabelPrimaryValue.size() > 0 && !altLabelPrimaryValue.contains(prefLabel.getValue()))) {
							altLabelPrimaryValue.add(prefLabel.getValue());
							if (altLabelPrimaryValueChanged == false) {
							    altLabelPrimaryValueChanged = true;
							}
							
							altLabelPrimaryObject.put(keyPrefLabel,altLabelPrimaryValue);
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


    public void dropRepository(){
    	this.entityRecordRepository.dropCollection();
	}
}
