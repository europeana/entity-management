package eu.europeana.entitymanagement.definitions.model;

import java.util.List;

import org.bson.types.ObjectId;

public interface EntityRecord {

    void setDbId(ObjectId dbId);
    ObjectId getDbId();

    void setProxies(List<EntityProxy> proxies);

    List<EntityProxy> getProxies();

    void setIsAggregatedBy(Aggregation isAggregatedBy);

    Aggregation getIsAggregatedBy();

    void setEntityId(String entityId);

    String getEntityId();

    void setEntity(EntityRoot entity);

    EntityRoot getEntity();

}
