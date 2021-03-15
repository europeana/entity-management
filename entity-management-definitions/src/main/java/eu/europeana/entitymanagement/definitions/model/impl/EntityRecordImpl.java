package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
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
    private long dbId;

    private String entityId;

    private Entity entity;

    private List<EntityProxy> proxies;

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
    public void setProxies(List<EntityProxy> proxies) {
	this.proxies = proxies;
    }

    @Override
    public void setDbId(long dbId_param) {
	this.dbId = dbId_param;
    }

    @Override
    public long getDbId() {
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
    public EntityProxy getEuropeanaProxy() {
	return (EntityProxy) proxies.stream()
		.filter(s -> s.getProxyId().startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI));
    }
}
