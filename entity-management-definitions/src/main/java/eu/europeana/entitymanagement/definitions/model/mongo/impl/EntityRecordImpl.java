package eu.europeana.entitymanagement.definitions.model.mongo.impl;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntityRecord;


@Entity(value = "EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
public class EntityRecordImpl extends BaseEntityRecord {

	@Id
    ObjectId dbId;

    @JsonIgnore
    public ObjectId getDbId() {
        return dbId;
    }

    public void setDbId(ObjectId dbId) {
        this.dbId = dbId;
    }
}
