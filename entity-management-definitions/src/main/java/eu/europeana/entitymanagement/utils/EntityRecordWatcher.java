package eu.europeana.entitymanagement.utils;

import dev.morphia.annotations.PrePersist;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;

import java.util.Date;

/**
 * Watches for Database operations on the EntityRecord collection
 */
public class EntityRecordWatcher {

    /**
     * Invoked by Morphia when creating / updating an EntityRecord.
     * Sets the modified time for the record
     *
     * @param record EntityRecord
     */
    @PrePersist
    void prePersist(EntityRecord record) {
        record.setModified(new Date());
    }

}