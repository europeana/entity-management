package eu.europeana.entitymanagement.definitions.model.mongo.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import eu.europeana.entitymanagement.definitions.model.impl.BaseEntityRecord;


@Entity(value = "EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
public class EntityRecordImpl extends BaseEntityRecord {

    @Id
    @JsonIgnore
    private Long dbId;

    @Override
    public Long getDbId() {
        return dbId;
    }

    @Override
    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }
}
