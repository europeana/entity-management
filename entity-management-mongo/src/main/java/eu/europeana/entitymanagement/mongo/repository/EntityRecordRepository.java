package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.filters.Filters.or;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.*;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.mongodb.client.model.ReturnDocument;

import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;

/**
 * Repository for retrieving the EntityRecord objects.
 */
@Repository(AppConfigConstants.BEAN_ENTITY_RECORD_REPO)
public class EntityRecordRepository {

    private static final Logger logger = LogManager.getLogger(EntityRecordRepository.class);

    @Resource(name=AppConfigConstants.BEAN_EM_DATA_STORE)
    private Datastore datastore;

    @Resource(name = "emValidatorFactory")
    private ValidatorFactory emValidatorFactory;


    /**
     * @return the total number of resources in the database
     */
    public long count() {
        return datastore.find(EntityRecordImpl.class).count();
    }

    /**
     * Check if an EntityRecord exists that matches the given parameters 
     * using DBCollection.count(). In ticket EA-1464 this method was tested as the best performing.
     * @param entityId	ID of the dataset
     * @return true if yes, otherwise false
     */
    public boolean existsByEntityId(String entityId) {
        return datastore.find(EntityRecordImpl.class).filter(
                eq(EntityRecordFields.ENTITY_ID, entityId)
        ).count() > 0 ;
    }

    /**
     * Find and return EntityRecord that matches the given parameters
     * @param entityId	ID of the dataset
     * @return EntityRecord
     */
    public EntityRecord findByEntityId(String entityId) {
        return datastore.find(EntityRecordImpl.class).filter(
                eq(ENTITY_ID, entityId))
                .first();
    }


    /**
     * Deletes all EntityRecord objects that contain the given entityId
     * @param entityId ID of the dataset to be deleted
     * @return the number of deleted objects
     */
    // TODO move this to the loader?
    public long deleteForGood(String entityId) {
        return datastore.find(EntityRecordImpl.class).filter(
                eq(ENTITY_ID,entityId))
                .delete(MULTI_DELETE_OPTS).getDeletedCount();
    }

    /**
     * Saves the given entity record to the database.
     * First generates a numerical ID for the record, before insertion.
     * @param entityRecord entity record to save
     * @return saved entity
     */
    public EntityRecord save(EntityRecord entityRecord){
        //check the validation of the entity fields
    	//Temporarily disabled until the implementation is complete and correct
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord.getEntity());
        for (ConstraintViolation<Entity> violation : violations) {
            logger.error(violation.getMessage());
            //TODO: do something besides logging warning!
        }

        return datastore.save(entityRecord);
    }


    /**
     * Generates an autoincrement value for entities, based on the Entity type
     *
     * @param internalType internal type for Entity
     * @return autoincrement value
     */
    public synchronized long generateAutoIncrement(String internalType) {
        // Get the given key from the auto increment entity and try to increment it.
        EntityIdGenerator autoIncrement = datastore.find(EntityIdGenerator.class).filter(
                eq("_id", internalType))
                .modify(UpdateOperators.inc("value"))
                .execute(new ModifyOptions()
                        .returnDocument(ReturnDocument.AFTER));


        // If none is found, we need to create one for the given key.
        if (autoIncrement == null) {
            autoIncrement = new EntityIdGenerator(internalType, 1L);
            datastore.save(autoIncrement);
        }
        return autoIncrement.getValue();
    }


    /**
     * Drops the EntityRecord and Entity ID generator collections.
     */
    public void dropCollection(){
        datastore.getMapper().getCollection(EntityRecordImpl.class).drop();
        datastore.getMapper().getCollection(EntityIdGenerator.class).drop();
    }


    /**
     * Gets EntityRecord containing the given id in its sameAs or exactMatch fields.
     * @param id id to query
     * @return Optional containing result, or empty Optional if no match
     */
    public Optional<EntityRecord> findMatchingEntitiesByCoreference(String id) {

        EntityRecordImpl value = datastore.find(EntityRecordImpl.class).disableValidation()
                .filter(or(
                        eq(ENTITY_SAME_AS, id),
                        eq(ENTITY_EXACT_MATCH, id)
                        )
                ).first();

        return Optional.ofNullable(value);

    }

    public List<EntityRecord> saveBulk(List<EntityRecord> entityRecords){
        return datastore.save(entityRecords);
    }

    /**
     * Queries the EntityRecord for records that match the given filter(s).
     *
     * Results are sorted in descending order of modified time.
     *
     * @param filters Query filters
     * @return List with results
     */
    public List<? extends EntityRecord> findWithFilters(int start, int count, Filter[] filters) {
        return datastore.find(EntityRecordImpl.class)
                .filter(filters)
                .iterator(new FindOptions()
                        .skip(start)
                        .sort(descending(ENTITY_MODIFIED))
                        .limit(count)).toList();

    }
}
