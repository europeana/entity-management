package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.in;
import static dev.morphia.query.experimental.filters.Filters.ne;
import static dev.morphia.query.experimental.filters.Filters.or;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.DISABLED;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_AGGREGATED_VIA;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_EXACT_MATCH;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_MODIFIED;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.ENTITY_SAME_AS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_UPDATE_OPTS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.EntityIdGenerator;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.mongo.utils.MorphiaUtils;

/** Repository for retrieving the EntityRecord objects. */
@Repository(AppConfigConstants.BEAN_ENTITY_RECORD_REPO)
public class EntityRecordRepository extends AbstractRepository {

  /** @return the total number of resources in the database */
  public long count() {
    return getDataStore().find(EntityRecord.class).count();
  }

  /**
   * Check if an EntityRecord exists that matches the given parameters using DBCollection.count().
   * In ticket EA-1464 this method was tested as the best performing.
   *
   * @param entityId ID of the dataset
   * @return true if yes, otherwise false
   */
  public boolean existsByEntityId(String entityId) {
    return getDataStore()
            .find(EntityRecord.class)
            .filter(eq(ENTITY_ID, entityId))
            .count()
        > 0;
  }

  /**
   * Find and return EntityRecord that matches the given parameters
   *
   * @param entityId ID of the dataset
   * @return EntityRecord
   */
  public EntityRecord findByEntityId(String entityId) {
    return getDataStore().find(EntityRecord.class).filter(eq(ENTITY_ID, entityId)).first();
  }

  /**
   * Find List of EntityRecord that matches the given entity ids
   *
   * @param entityIds : list of entity id's to be fetched
   * @param excludeDisabled : fetch only active records, if set to true
   * @param fetchFullRecord : fetch the full entity record, if set to true
   * @return list of Entity records matching entity Ids
   */
  public List<EntityRecord> findByEntityIds(
      List<String> entityIds, boolean excludeDisabled, boolean fetchFullRecord) {
    // Get all EntityRecords that match the given entityIds
    List<Filter> filters = new ArrayList<>();
    filters.add(in(ENTITY_ID, entityIds));

    // Only fetch active records. Disabled records have a date value
    if (excludeDisabled) {
      filters.add(eq(DISABLED, null));
    }

    FindOptions findOptions = new FindOptions();
    // if fetchFullRecord is set to false, we only care about the entityId and disabled flag for
    // this query
    if (!fetchFullRecord) {
      findOptions.projection().include(ENTITY_ID).projection().include(DISABLED);
    }
    return getDataStore()
        .find(EntityRecord.class)
        .filter(filters.toArray(Filter[]::new))
        .iterator(findOptions)
        .toList();
  }

  public List<EntityIdDisabledStatus> getEntityIds(
      List<String> entityIds, boolean excludeDisabled) {
    List<EntityRecord> entityRecords = findByEntityIds(entityIds, excludeDisabled, false);
    return entityRecords.stream()
        .map(
            entityRecord ->
                new EntityIdDisabledStatus(entityRecord.getEntityId(), entityRecord.isDisabled()))
        .collect(Collectors.toList());
  }

  /**
   * Deletes all EntityRecord objects that contain the given entityId
   *
   * @param entityId ID of the dataset to be deleted
   * @return the number of deleted objects
   */
  public long deleteForGood(String entityId) {
    return getDataStore()
        .find(EntityRecord.class)
        .filter(eq(ENTITY_ID, entityId))
        .delete()
        .getDeletedCount();
  }

  /**
   * Saves the given entity record to the database. First generates a numerical ID for the record,
   * before insertion.
   *
   * @param entityRecord entity record to save
   * @return saved entity
   */
  public EntityRecord save(EntityRecord entityRecord) {
    return getDataStore().save(entityRecord);
  }

  /** Drops the EntityRecord and Entity ID generator collections. */
  public void dropCollection() {
    getDataStore().getMapper().getCollection(EntityRecord.class).drop();
    getDataStore().getMapper().getCollection(EntityIdGenerator.class).drop();
  }

  /**
   * Retrieves EntityRecords containing at least one of the provided uris in its sameAs or exactMatch fields.
   *
   * @param uris uris to query co-references
   * @param entityId indicated the record for which dupplicates are searched. Use null or empty to return any
   * @param excludeDisabled if disabled entities should be returned or not
   * @return the list of entity records found
   */
  @SuppressWarnings("java:S2301")
  public List<EntityRecord> findEntitiesByCoreference(
      List<String> uris, String entityId, boolean excludeDisabled) {

    Query<EntityRecord> query =
        getDataStore()
            .find(EntityRecord.class)
            .disableValidation()
            .filter(or(in(ENTITY_SAME_AS, uris), in(ENTITY_EXACT_MATCH, uris)));

    if (StringUtils.isNotBlank(entityId)) {
      query.filter(ne(ENTITY_ID, entityId));
    }
    
    if(excludeDisabled) {
      query.filter(eq(DISABLED, null));
    }

    //query the database
    return query.iterator().toList();
  }
  
  public List<String> findAggregatesFrom(String entityId) {
    List<EntityRecord> entityRecords =
        getDataStore()
            .find(EntityRecord.class)
            .disableValidation()
            .filter(in(ENTITY_AGGREGATED_VIA, Arrays.asList(entityId)))
            .iterator(new FindOptions().projection().include(ENTITY_ID))
            .toList();
    
    return entityRecords.stream().map(entityRecord -> entityRecord.getEntityId()).toList();
  }
  
  public List<EntityRecord> saveBulk(List<EntityRecord> entityRecords) {
    return getDataStore().save(entityRecords);
  }

  /**
   * Queries the EntityRecord for records that match the given filter(s).
   *
   * <p>Results are sorted in ascending order of modified time.
   *
   * @param filters Query filters
   * @return List with results
   */
  public List<EntityRecord> findWithFilters(int start, int count, Filter[] filters) {
    return getDataStore()
        .find(EntityRecord.class)
        .filter(filters)
        .iterator(new FindOptions().skip(start).sort(ascending(ENTITY_MODIFIED)).limit(count))
        .toList();
  }

  public long deleteBulk(List<String> entityIds) {
    return getDataStore()
        .find(EntityRecord.class)
        .filter(in(ENTITY_ID, entityIds))
        .delete(MorphiaUtils.MULTI_DELETE_OPTS)
        .getDeletedCount();
  }

  public UpdateResult disableBulk(List<String> entityIds) {
    Date disablingDate = new Date();
    return getDataStore()
        .find(EntityRecord.class)
        .filter(in(ENTITY_ID, entityIds))
        .update(UpdateOperators.set(DISABLED, disablingDate))
        .execute(MULTI_UPDATE_OPTS);
  }
}
