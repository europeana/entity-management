package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;


@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@dev.morphia.annotations.Entity(value = "EntityRecord")
@Indexes(@Index(fields = { @Field("dbId") }, options = @IndexOptions(unique = true)))
public class BaseEntityRecord implements EntityRecord{

    @Id
    @JsonIgnore
    private long dbId;

    private String entityId;

    private Entity entity;

    private Aggregation isAggregatedBy;

    private List<EntityProxy> proxies; 
    

    @Override
    @JsonProperty
    @JacksonXmlProperty
    public Entity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    @JsonProperty(WebEntityFields.ID)
    @JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
    public String getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    @JsonProperty(WebEntityFields.IS_AGGREGATED_BY)
    @JacksonXmlProperty(localName = XmlFields.XML_ORE_IS_AGGREGATED_BY)
    public Aggregation getIsAggregatedBy() {
        return isAggregatedBy;
    }

    @Override
    public void setIsAggregatedBy(Aggregation isAggregatedBy) {
        this.isAggregatedBy = isAggregatedBy;
    }

    @Override
    @JsonProperty
    @JacksonXmlElementWrapper(useWrapping=false)
    public List<EntityProxy> getProxies() {
        return proxies;
    }

    @Override
    public void setProxies(List<EntityProxy> proxies) {
        this.proxies = proxies;
    }

	@Override
	public void setDbId(long dbId_param) {
		this.dbId=dbId_param;
	}

	@Override
	public long getDbId() {
		return dbId;
	}
}
