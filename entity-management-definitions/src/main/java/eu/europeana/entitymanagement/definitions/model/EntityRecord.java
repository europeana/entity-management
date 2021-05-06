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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity
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
    @JacksonXmlProperty
    public Entity getEntity() {
        return entity;
    }

    
    @JsonSetter
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    
    @JsonGetter(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute = true, localName = XmlFields.XML_RDF_ABOUT)
    public String getEntityId() {
        return entityId;
    }

    
    @JsonSetter(WebEntityFields.ID)
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    
    @JsonGetter
    @JacksonXmlElementWrapper(useWrapping = false)
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