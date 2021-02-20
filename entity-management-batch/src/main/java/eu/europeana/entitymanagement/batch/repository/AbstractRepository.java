package eu.europeana.entitymanagement.batch.repository;

import com.mongodb.client.model.ReturnDocument;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.entitymanagement.batch.entity.SequenceGenerator;

import static dev.morphia.query.experimental.filters.Filters.eq;

public abstract class AbstractRepository {

    abstract Datastore getDataStore();

    /**
     * Generates an autoincrement value for entities, based on the Entity type
     * @param internalType internal type for Entity
     * @return autoincrement value
     */
    protected long generateSequence(String internalType) {
        // Get the given key from the auto increment entity and try to increment it.
        SequenceGenerator nextId = getDataStore().find(SequenceGenerator.class).filter(
                eq("_id", internalType))
                .modify(UpdateOperators.inc("value"))
                .execute(new ModifyOptions()
                        .returnDocument(ReturnDocument.AFTER));


        // If none is found, we need to create one for the given key.
        if (nextId == null) {
            nextId = new SequenceGenerator(internalType, 1L);
            getDataStore().save(nextId);
        }
        return nextId.getValue();
    }
}
