package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.List;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;

public class BaseEntityRecord implements EntityRecord{

    private String entityId;

    private Entity entity;

    private Aggregation isAggregatedBy;

    private List<EntityProxy> proxies; 
    

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public Aggregation getIsAggregatedBy() {
        return isAggregatedBy;
    }

    @Override
    public void setIsAggregatedBy(Aggregation isAggregatedBy) {
        this.isAggregatedBy = isAggregatedBy;
    }

    @Override
    public List<EntityProxy> getProxies() {
        return proxies;
    }

    @Override
    public void setProxies(List<EntityProxy> proxies) {
        this.proxies = proxies;
    }
}
