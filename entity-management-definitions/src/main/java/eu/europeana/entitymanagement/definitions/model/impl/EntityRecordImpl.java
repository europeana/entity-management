package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity(value = "EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
public class EntityRecordImpl implements EntityRecord {

    @Id
    @JsonIgnore
    private ObjectId dbId;

    private String entityId;

    private Entity entity;

    private List<EntityProxy> proxies = new ArrayList<>();
    
    private boolean disabled;

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

    @JsonIgnore
    @Override
    public EntityProxy getEuropeanaProxy() {
    	for (EntityProxy proxy : proxies) {
    		if (proxy.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)) {
    			return proxy;
    		}
    	}
    	return null;	
    }

    @Override
    @JsonIgnore
    public EntityProxy getExternalProxy() {
    	for (EntityProxy proxy : proxies) {
    		if (!proxy.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI)) {
    			return proxy;
    		}
    	}
    	return null;
    }

}