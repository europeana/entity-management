package eu.europeana.entitymanagement.definitions.model.impl;

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

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity(value = "EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
@EntityListeners(EntityRecordWatcher.class)
public class EntityRecordImpl implements EntityRecord {

    @Id
    @JsonIgnore
    private ObjectId dbId;

    private String entityId;

    private Entity entity;

    private List<EntityProxy> proxies = new ArrayList<>();
    
    private boolean disabled;

    private Date modified;

    private Date created;

    @Override
    @JsonGetter
    @JacksonXmlProperty
    public Entity getEntity() {
	return entity;
    }

    @Override
    @JsonSetter
    public void setEntity(Entity entity) {
	this.entity = entity;
    }

    @Override
    @JsonGetter(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute = true, localName = XmlFields.XML_RDF_ABOUT)
    public String getEntityId() {
	return entityId;
    }

    @Override
    @JsonSetter(WebEntityFields.ID)
    public void setEntityId(String entityId) {
	this.entityId = entityId;
    }

    @Override
    @JsonGetter
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<EntityProxy> getProxies() {
	return proxies;
    }

    @Override
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

    @Override
    public boolean getDisabled() {
	return this.disabled;
    }

    @Override
    public void setDisabled(boolean disabledParam) {
	this.disabled = disabledParam;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @JsonIgnore
    @Override
    public EntityProxy getEuropeanaProxy() {
	return proxies.stream()
		.filter(s -> s.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)).findFirst().orElse(null);
    }

    @Override
    @JsonIgnore
    public EntityProxy getExternalProxy() {
	return proxies.stream()
            .filter(s -> !s.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)).findFirst().orElse(null);
    }


    void prePersist(EntityRecordImpl entityRecord){

    }
}