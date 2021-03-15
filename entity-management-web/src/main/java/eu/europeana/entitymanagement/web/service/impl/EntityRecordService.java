package eu.europeana.entitymanagement.web.service.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.DataSources;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.impl.AggregationImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.web.model.EntityPreview;

@Service(AppConfig.BEAN_ENTITY_RECORD_SERVICE)
public class EntityRecordService {

    Logger logger = LogManager.getLogger(getClass());

    private final EntityRecordRepository entityRecordRepository;

    final EntityManagementConfiguration emConfiguration;

    private final DataSources datasources;

    @Autowired
    public EntityRecordService(EntityRecordRepository entityRecordRepository, EntityManagementConfiguration emConfiguration, DataSources datasources) {
        this.entityRecordRepository = entityRecordRepository;
        this.emConfiguration = emConfiguration;
        this.datasources = datasources;
    }


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
     * Creates an {@link EntityRecord} from an {@link EntityPreview}, which
     * is then persisted.
     *
     * @param entityCreationRequest de-referenced XML response instance from Metis
     * @param entityCreationRequest entity request object
     * @param externalEntityType    entity type based on de-referencing response
     * @return Saved Entity record
     * @throws EntityCreationException if an error occurs
     */
    public EntityRecord createEntityFromRequest(EntityPreview entityCreationRequest, String externalEntityType)
            throws EntityCreationException {
        // Fail quick if no datasource is configured
        Optional<DataSource> externalDatasourceOptional = datasources.getDatasource(entityCreationRequest.getId());
        if (externalDatasourceOptional.isEmpty()) {
            throw new EntityCreationException("No configured datasource for entity " + entityCreationRequest.getId());
        }

        Entity entity = EntityObjectFactory.createEntityObject(externalEntityType);
        long dbId = entityRecordRepository.generateAutoIncrement(entity.getType());


        entity.setPrefLabelStringMap(entityCreationRequest.getPrefLabel());
        entity.setAltLabel(entityCreationRequest.getAltLabel());

        EntityRecord entityRecord = new EntityRecordImpl();
        entityRecord.setEntity(entity);

        entityRecord.setDbId(dbId);
        String entityId = AppConfigConstants.BASE_URI_DATA + entityRecord.getEntity().getType().toLowerCase() + "/" + dbId;
        entityRecord.setEntityId(entityId);
        entityRecord.getEntity().setEntityId(entityId);

        setEuropeanaMetadata(entityId, entityRecord);

        DataSource externalDatasource = externalDatasourceOptional.get();
        setDatasourceMetadata(entityCreationRequest, entityId, externalDatasource, entityRecord);


        setEntityAggregation(entityRecord, entityId);
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
     * This function merges the data from the secondary entity to the primary entity
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void mergeEntity(Entity primary, Entity secondary) {

//		String altLabelFieldNamePrefix = emConfiguration.getAltLabelFieldNamePrefix();
//		String altLabelCharacterSeparator = emConfiguration.getLanguageSeparator();

        String altLabelFieldNamePrefix = "skos";
        String altLabelCharacterSeparator = ".";

        try {
            // store the preferred label in the secondary entity that is different from the
            // preferred label in the primary entity to the alternative labels of the
            // primary entity
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
                    if (fieldValuePrimaryObjectArray != null)
                        fieldValuePrimaryObject = new ArrayList<>(Arrays.asList(fieldValuePrimaryObjectArray));
                    if (fieldValueSecondaryObjectArray != null)
                        fieldValueSecondaryObject = new ArrayList<>(Arrays.asList(fieldValueSecondaryObjectArray));
                    boolean fieldValuePrimaryObjectChanged = false;

                    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
                        fieldValuePrimaryObject = new ArrayList<>();
                        fieldValuePrimaryObject.addAll(fieldValueSecondaryObject);
                        fieldValuePrimaryObjectChanged = true;
                    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
                        // check the secondary object for new values that are not in the primary object
                        for (Object secondaryElem : fieldValueSecondaryObject) {
                            if (!fieldValuePrimaryObject.contains(secondaryElem)) {
                                fieldValuePrimaryObject.add(secondaryElem);
                                if (fieldValuePrimaryObjectChanged == false)
                                    fieldValuePrimaryObjectChanged = true;
                            }
                        }
                    }

                    if (fieldValuePrimaryObjectChanged)
                        primary.setFieldValue(field, fieldValuePrimaryObject.toArray((Object[]) Array
                                .newInstance(field.getType().getComponentType(), fieldValuePrimaryObject.size())));
                } else if (Map.class.isAssignableFrom(fieldType)) {
                    Map<Object, Object> fieldValuePrimaryObjectMap = (Map<Object, Object>) primary.getFieldValue(field);
                    Map<Object, Object> fieldValueSecondaryObjectMap = (Map<Object, Object>) secondary
                            .getFieldValue(field);
                    Map<Object, Object> fieldValuePrimaryObject = null;
                    Map<Object, Object> fieldValueSecondaryObject = null;
                    if (fieldValuePrimaryObjectMap != null)
                        fieldValuePrimaryObject = new HashMap<>(fieldValuePrimaryObjectMap);
                    if (fieldValueSecondaryObjectMap != null)
                        fieldValueSecondaryObject = new HashMap<>(fieldValueSecondaryObjectMap);
                    boolean fieldValuePrimaryObjectChanged = false;

                    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
                        fieldValuePrimaryObject = new HashMap<>();
                        fieldValuePrimaryObject.putAll(fieldValueSecondaryObject);
                        fieldValuePrimaryObjectChanged = true;
                    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {
                        for (Map.Entry elemSecondary : fieldValueSecondaryObject.entrySet()) {
                            Object key = elemSecondary.getKey();
                            // if the map value is a list, merge the lists of the primary and the secondary
                            // object without duplicates
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
                                    fieldValuePrimaryObjectChanged = true;
                                    fieldValuePrimaryObject.put(key, listPrimaryObject);
                                }
                            }
                            // keep the different preferred labels in the secondary object for the
                            // alternative label in the primary object
                            else if (fieldValuePrimaryObject.containsKey(key)
                                    && fieldName.toLowerCase().contains("pref")
                                    && fieldName.toLowerCase().contains("label")) {
                                Object primaryObjectPrefLabel = fieldValuePrimaryObject.get(key);
                                if (!primaryObjectPrefLabel.equals(elemSecondary.getValue())) {
                                    prefLabelsForAltLabels.put(key, elemSecondary.getValue());
                                }
                            } else if (!fieldValuePrimaryObject.containsKey(key)) {
                                fieldValuePrimaryObject.put(key, elemSecondary.getValue());
                                fieldValuePrimaryObjectChanged = true;
                            }

                        }
                    }
                    if (fieldValuePrimaryObjectChanged)
                        primary.setFieldValue(field, fieldValuePrimaryObject);

                } else if (List.class.isAssignableFrom(fieldType)) {

                    List<Object> fieldValuePrimaryObjectList = (List<Object>) primary.getFieldValue(field);
                    List<Object> fieldValueSecondaryObjectList = (List<Object>) secondary.getFieldValue(field);
                    List<Object> fieldValuePrimaryObject = null;
                    List<Object> fieldValueSecondaryObject = null;
                    if (fieldValuePrimaryObjectList != null)
                        fieldValuePrimaryObject = new ArrayList<Object>(fieldValuePrimaryObjectList);
                    if (fieldValueSecondaryObjectList != null)
                        fieldValueSecondaryObject = new ArrayList<Object>(fieldValueSecondaryObjectList);
                    boolean fieldValuePrimaryObjectChanged = false;

                    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null) {
                        primary.setFieldValue(field, fieldValueSecondaryObject);
                    } else if (fieldValuePrimaryObject != null && fieldValueSecondaryObject != null) {

                        for (Object secondaryObjectListObject : fieldValueSecondaryObject) {
                            if (!fieldValuePrimaryObject.contains(secondaryObjectListObject)) {
                                fieldValuePrimaryObject.add(secondaryObjectListObject);
                                if (fieldValuePrimaryObjectChanged == false)
                                    fieldValuePrimaryObjectChanged = true;
                            }
                        }

                        if (fieldValuePrimaryObjectChanged)
                            primary.setFieldValue(field, fieldValuePrimaryObject);
                    }

                } else {
                    Object fieldValuePrimaryObject = primary.getFieldValue(field);
                    Object fieldValueSecondaryObject = secondary.getFieldValue(field);

                    if (fieldValuePrimaryObject == null && fieldValueSecondaryObject != null)
                        primary.setFieldValue(field, fieldValueSecondaryObject);
                }

            }

            // adding the preferred labels from the secondary object to the alternative
            // labels of primary object
            if (prefLabelsForAltLabels.size() > 0) {
                for (Field field : allEntityFields) {
                    String fieldName = field.getName();
                    if (fieldName.toLowerCase().contains("alt") && fieldName.toLowerCase().contains("label")) {
                        Map<Object, Object> altLabelPrimaryObjectMap = (Map<Object, Object>) primary
                                .getFieldValue(field);
                        Map<Object, Object> altLabelPrimaryObject = null;
                        if (altLabelPrimaryObjectMap != null)
                            altLabelPrimaryObject = new HashMap<>(altLabelPrimaryObjectMap);
                        else
                            altLabelPrimaryObject = new HashMap<>();

                        boolean altLabelPrimaryValueChanged = false;
                        for (Map.Entry prefLabel : prefLabelsForAltLabels.entrySet()) {
                            String keyPrefLabel = (String) prefLabel.getKey();
                            // extracting only the language part after the "_" character
                            String keyPrefLabelEnding = keyPrefLabel.substring(keyPrefLabel.lastIndexOf("_") + 1);
                            List<Object> altLabelPrimaryObjectList = (List<Object>) altLabelPrimaryObject
                                    .get(altLabelFieldNamePrefix + altLabelCharacterSeparator + keyPrefLabelEnding);
                            List<Object> altLabelPrimaryValue = null;
                            if (altLabelPrimaryObjectList != null)
                                altLabelPrimaryValue = new ArrayList<>(altLabelPrimaryObjectList);
                            else
                                altLabelPrimaryValue = new ArrayList<>();

                            if (altLabelPrimaryValue.size() == 0 || (altLabelPrimaryValue.size() > 0
                                    && !altLabelPrimaryValue.contains(prefLabel.getValue()))) {
                                altLabelPrimaryValue.add(prefLabel.getValue());
                                if (altLabelPrimaryValueChanged == false)
                                    altLabelPrimaryValueChanged = true;
                                altLabelPrimaryObject.put(
                                        altLabelFieldNamePrefix + altLabelCharacterSeparator + keyPrefLabelEnding,
                                        altLabelPrimaryValue);
                            }
                        }
                        if (altLabelPrimaryValueChanged)
                            primary.setFieldValue(field, altLabelPrimaryObject);
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

    private void setEntityAggregation(EntityRecord entityRecord, String entityId) {
        // set isAggregatedBy on Entity
        Aggregation isAggregatedBy = new AggregationImpl();
        isAggregatedBy.setId(entityId + "#aggregation");
        isAggregatedBy.setCreated(new Date());
        isAggregatedBy.setModified(new Date());
        isAggregatedBy.setRecordCount(1);
        isAggregatedBy.setAggregates(Arrays.asList(getEuropeanaAggregationId(entityId), getDatasourceAggregationId(entityId)));

        entityRecord.getEntity().setIsAggregatedBy(isAggregatedBy);
    }

    private void setEuropeanaMetadata(String entityId, EntityRecord entityRecord) {
        String europeanaProxyId = entityId + "#proxy_europeana";

        Aggregation europeanaAggr = new AggregationImpl();
        europeanaAggr.setId(getEuropeanaAggregationId(entityId));
        //TODO: move to constants file
        europeanaAggr.setRights("https://creativecommons.org/publicdomain/zero/1.0/");
        europeanaAggr.setCreated(new Date());
        europeanaAggr.setModified(new Date());
        europeanaAggr.setSource("www.europeana.eu");


        EntityProxy europeanaProxy = new EntityProxyImpl();
        europeanaProxy.setProxyId(europeanaProxyId);
        europeanaProxy.setProxyFor(entityId);
        europeanaProxy.setProxyIn(europeanaAggr);

        entityRecord.addProxy(europeanaProxy);
    }

    private void setDatasourceMetadata(EntityPreview entityCreationRequest, String entityId, DataSource externalDatasource, EntityRecord entityRecord) {
        Aggregation datasourceAggr = new AggregationImpl();
        datasourceAggr.setId(getDatasourceAggregationId(entityId));
        datasourceAggr.setRights(externalDatasource.getRights());
        datasourceAggr.setCreated(new Date());
        datasourceAggr.setModified(new Date());
        datasourceAggr.setRights(externalDatasource.getUrl());


        EntityProxy datasourceProxy = new EntityProxyImpl();
        datasourceProxy.setProxyId(entityCreationRequest.getId());
        datasourceProxy.setProxyFor(entityId);
        datasourceProxy.setProxyIn(datasourceAggr);

        entityRecord.addProxy(datasourceProxy);
    }


    private String getEuropeanaAggregationId(String entityId) {
        return entityId + "#aggr_europeana";
    }

    private String getDatasourceAggregationId(String entityId) {
        return entityId + "#aggr_source_1";
    }
}
