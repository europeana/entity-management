package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.Sort.ascending;
import static dev.morphia.query.experimental.filters.Filters.*;
import static eu.europeana.entitymanagement.definitions.EntityRecordFields.*;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MAJORITY_WRITE_MODIFY_OPTS;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_UPDATE_OPTS;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.EntityRecordFields;
import eu.europeana.entitymanagement.definitions.model.EntityIdGenerator;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.web.EntityIdDisabledStatus;
import eu.europeana.entitymanagement.mongo.utils.MorphiaUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.ValidatorFactory;
import org.springframework.stereotype.Repository;

/** Repository for retrieving the EntityRecord objects. */
@Repository(AppConfigConstants.BEAN_ENTITY_RECORD_REPO)
public class EntityRecordRepository {

  @Resource(name = AppConfigConstants.BEAN_EM_DATA_STORE)
  private Datastore datastore;

  @Resource(name = "emValidatorFactory")
  private ValidatorFactory emValidatorFactory;

  /** @return the total number of resources in the database */
  public long count() {
    return datastore.find(EntityRecord.class).count();
  }

  /**
   * Check if an EntityRecord exists that matches the given parameters using DBCollection.count().
   * In ticket EA-1464 this method was tested as the best performing.
   *
   * @param entityId ID of the dataset
   * @return true if yes, otherwise false
   */
  public boolean existsByEntityId(String entityId) {
    return datastore
            .find(EntityRecord.class)
            .filter(eq(EntityRecordFields.ENTITY_ID, entityId))
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
    return datastore.find(EntityRecord.class).filter(eq(ENTITY_ID, entityId)).first();
  }

  public List<EntityIdDisabledStatus> getEntityIds(
      List<String> entityIds, boolean excludeDisabled) {
    // Get all EntityRecords that match the given entityIds
    List<Filter> filters = new ArrayList<>();
    filters.add(in(ENTITY_ID, entityIds));

    // Only fetch active records. Disabled records have a date value
    if (excludeDisabled) {
      filters.add(eq(DISABLED, null));
    }

    List<EntityRecord> entityRecords =
        datastore
            .find(EntityRecord.class)
            .filter(filters.toArray(Filter[]::new))
            .iterator(
                new FindOptions()
                    // we only care about the entityId and disabled flag for this query
                    .projection()
                    .include(ENTITY_ID)
                    .projection()
                    .include(DISABLED))
            .toList();
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
    return datastore
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
    return datastore.save(entityRecord);
  }

  /**
   * Generates an autoincrement value for entities, based on the Entity type
   *
   * @param internalType internal type for Entity
   * @return autoincrement value
   */
  public long generateAutoIncrement(String internalType) {
    /*
     * Get the given key from the auto increment entity and try to increment it.
     * Synchronization occurs on the DB-level, so we don't need to synchronize this code block.
     */

    EntityIdGenerator autoIncrement =
        datastore
            .find(EntityIdGenerator.class)
            .filter(eq("_id", internalType))
            .modify(UpdateOperators.inc("value"))
            .execute(MAJORITY_WRITE_MODIFY_OPTS);

    /*
     * If none is found, we need to create one for the given key. This shouldn't happen in
     * production as the db is pre-populated with entities
     */
    if (autoIncrement == null) {
      autoIncrement = new EntityIdGenerator(internalType, 1L);
      datastore.save(autoIncrement);
    }
    return autoIncrement.getValue();
  }

  /** Drops the EntityRecord and Entity ID generator collections. */
  public void dropCollection() {
    datastore.getMapper().getCollection(EntityRecord.class).drop();
    datastore.getMapper().getCollection(EntityIdGenerator.class).drop();
  }

  /**
   * Gets EntityRecord containing the given uris in its sameAs or exactMatch fields.
   *
   * @param uris uris to query
   * @return Optional containing result, or empty Optional if no match
   */
  public Optional<EntityRecord> findMatchingEntitiesByCoreference(List<String> uris) {

    EntityRecord value =
        datastore
            .find(EntityRecord.class)
            .disableValidation()
            .filter(or(in(ENTITY_SAME_AS, uris), in(ENTITY_EXACT_MATCH, uris)))
            .first();

    return Optional.ofNullable(value);
  }

  public List<EntityRecord> saveBulk(List<EntityRecord> entityRecords) {
    return datastore.save(entityRecords);
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
    return datastore
        .find(EntityRecord.class)
        .filter(filters)
        .iterator(new FindOptions().skip(start).sort(ascending(ENTITY_MODIFIED)).limit(count))
        .toList();
  }

  public long deleteBulk(List<String> entityIds) {
    return datastore
        .find(EntityRecord.class)
        .filter(in(ENTITY_ID, entityIds))
        .delete(MorphiaUtils.MULTI_DELETE_OPTS)
        .getDeletedCount();
  }

  public UpdateResult disableBulk(List<String> entityIds) {
    Date disablingDate = new Date();
    return datastore
        .find(EntityRecord.class)
        .filter(in(ENTITY_ID, entityIds))
        .update(UpdateOperators.set(DISABLED, disablingDate))
        .execute(MULTI_UPDATE_OPTS);
  }
}
