package eu.europeana.entity.definitions.model;

import java.util.List;

public interface EntityRecord {

    void setProxies(List<EntityProxy> proxies);

    List<EntityProxy> getProxies();

    void setIsAggregatedBy(Aggregation isAggregatedBy);

    Aggregation getIsAggregatedBy();

    void setEntityId(String entityId);

    String getEntityId();

    void setEntity(Entity entity);

    Entity getEntity();

}
