package eu.europeana.entitymanagement.mongo.repository;

import static dev.morphia.query.experimental.filters.Filters.eq;
import static eu.europeana.entitymanagement.mongo.repository.EntityRecordFields.ENTITY_ID;
import static eu.europeana.entitymanagement.mongo.utils.MorphiaUtils.MULTI_DELETE_OPTS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import dev.morphia.Datastore;
import eu.europeana.entitymanagement.definitions.model.EntityRoot;


/**
 * Repository for retrieving the EntityRecord objects.
 */
@Repository
public class EntityRepository {


    @Autowired
    private Datastore datastore;

    /**
     * @return the total number of resources in the database
     */
    public long count() {
        return datastore.find(EntityRoot.class).count();
    }

    /**
     * Check if an Entity exists that matches the given parameters 
     * using DBCollection.count(). In ticket EA-1464 this method was tested as the best performing.
     * @param entityId	ID of the dataset
     * @return true if yes, otherwise false
     */
    public boolean existsByEntityId(String entityId) {
        return datastore.find(EntityRoot.class).filter(
                eq(ENTITY_ID, entityId)
        ).count() > 0 ;
    }

    /**
     * Find and return Entity that matches the given parameters
     * @param entityId	ID of the dataset
     * @return Entity
     */
    public EntityRoot findByEntityId(String entityId) {
        return datastore.find(EntityRoot.class).filter(
                eq(ENTITY_ID, entityId))
                .first();
    }


    /**
     * Deletes all Entity objects that contain the given entityId
     * @param entityId ID of the dataset to be deleted
     * @return the number of deleted objects
     */
    // TODO move this to the loader?
    public long deleteDataset(String entityId) {
        return datastore.find(EntityRoot.class).filter(
                eq(ENTITY_ID,entityId))
                .delete(MULTI_DELETE_OPTS).getDeletedCount();
    }

    public void save(EntityRoot entityRecord){
        datastore.save(entityRecord);
    }

}
