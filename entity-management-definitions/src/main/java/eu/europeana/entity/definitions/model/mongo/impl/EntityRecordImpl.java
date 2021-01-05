package eu.europeana.entity.definitions.model.mongo.impl;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.europeana.entity.definitions.model.impl.BaseEntityRecord;

public class EntityRecordImpl extends BaseEntityRecord {

    ObjectId dbId;

    @JsonIgnore
    public ObjectId getDbId() {
        return dbId;
    }

    public void setDbId(ObjectId dbId) {
        this.dbId = dbId;
    }
}
