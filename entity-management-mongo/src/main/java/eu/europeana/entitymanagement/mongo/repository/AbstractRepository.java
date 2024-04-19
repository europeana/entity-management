package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.filters.Filters.eq;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MAJORITY_WRITE_MODIFY_OPTS;

import dev.morphia.Datastore;
import dev.morphia.query.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.EntityIdGenerator;
import javax.annotation.Resource;

public abstract class AbstractRepository {

  @Resource(name = AppConfigConstants.BEAN_EM_DATA_STORE)
  Datastore datastore;

  /**
   * Generates an autoincrement value for entities, based on the Entity type
   *
   * @param internalType internal type for Entity
   * @return autoincrement value
   */
  public long generateAutoIncrement(String type) {
    /*
     * Get the given key from the auto increment entity and try to increment it.
     * Synchronization occurs on the DB-level, so we don't need to synchronize this code block.
     */

    EntityIdGenerator autoIncrement =
        getDataStore()
            .find(EntityIdGenerator.class)
            .filter(eq("_id", type))
            .modify(UpdateOperators.inc("value"))
            .execute(MAJORITY_WRITE_MODIFY_OPTS);

    /*
     * If none is found, we need to create one for the given key. This shouldn't happen in
     * production as the db is pre-populated with entities
     */
    if (autoIncrement == null) {
      autoIncrement = new EntityIdGenerator(type, 1L);
      getDataStore().save(autoIncrement);
    }
    return autoIncrement.getValue();
  }

  /**
   * Generates an autoincrement value for entities, based on the Entity type
   *
   * @param internalType internal type for Entity
   * @return autoincrement value
   */
  public long getLastGeneratedIdentifier(String type) {
    /*
     * Get the given key from the auto increment entity and try to increment it.
     * Synchronization occurs on the DB-level, so we don't need to synchronize this code block.
     */

    EntityIdGenerator sequenceValue =
        getDataStore().find(EntityIdGenerator.class).first();

    return sequenceValue != null ? sequenceValue.getValue() : 0;
  }
  
  protected Datastore getDataStore() {
    return datastore;
  }
}
