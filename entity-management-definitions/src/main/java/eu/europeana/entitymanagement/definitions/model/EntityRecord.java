package eu.europeana.entitymanagement.definitions.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dev.morphia.annotations.*;
import eu.europeana.entitymanagement.utils.EntityRecordWatcher;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity("EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
@EntityListeners(EntityRecordWatcher.class)
public class EntityRecord {

    @Id
    @JsonIgnore
    private ObjectId dbId;

    private String entityId;

    private Entity entity;

    private final List<EntityProxy> proxies = new ArrayList<>();

    @JsonIgnore
    private boolean disabled;

    @JsonIgnore
    private Date created;

    @JsonIgnore
    private Date modified;


    
    @JsonGetter
    public Entity getEntity() {
        return entity;
    }

    
    @JsonSetter
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    
    @JsonGetter(WebEntityFields.ID)
    public String getEntityId() {
        return entityId;
    }

    
    @JsonSetter(WebEntityFields.ID)
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    
    @JsonGetter
    public List<EntityProxy> getProxies() {
        return proxies;
    }

    
    @JsonSetter
    public void addProxy(EntityProxy proxy) {
        this.proxies.add(proxy);
    }

    public void setDbId(ObjectId dbId_param) {
        this.dbId = dbId_param;
    }

    public ObjectId getDbId() {
        return dbId;
    }

    
    public boolean isDisabled() {
        return this.disabled;
    }

    
    public void setDisabled(boolean disabledParam) {
        this.disabled = disabledParam;
    }

    
    public void setCreated(Date created) {
        this.created = created;
    }

    
    public Date getCreated() {
        return this.created;
    }

    
    public void setModified(Date modified) {
        this.modified = modified;
    }

    
    public Date getModified() {
        return this.modified;
    }

    @JsonIgnore
    
    public EntityProxy getEuropeanaProxy() {
        return proxies.stream()
            .filter(s -> s.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)).findFirst().orElse(null);
    }

    
    @JsonIgnore
    public EntityProxy getExternalProxy() {
        return proxies.stream()
            .filter(s -> !s.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)).findFirst().orElse(null);
    }
}