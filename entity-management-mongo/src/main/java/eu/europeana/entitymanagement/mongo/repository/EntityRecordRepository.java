package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_ID;

import com.mongodb.client.model.ReturnDocument;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.mongo.impl.EntityRecordImpl;


/**
 * Repository for retrieving the EntityRecord objects.
 */
@Repository
public class EntityRecordRepository {


    @Autowired
    private Datastore datastore;

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
    public long deleteDataset(String entityId) {
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
        // first get ID from database before saving
        long dbId = generateAutoIncrement(entityRecord.getEntity().getInternalType());
        String entityId = "http://data.europeana.eu/" + entityRecord.getEntity().getInternalType() + "/" + dbId;
        entityRecord.setDbId(dbId);
        entityRecord.getEntity().setEntityId(entityId);
        entityRecord.setEntityId(entityId);

        return datastore.save(entityRecord);
    }


    /**
     * Generates an autoincrement value for entities, based on the Entity type
     * @param internalType internal type for Entity
     * @return autoincrement value
     */
    private long generateAutoIncrement(String internalType) {
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

}
