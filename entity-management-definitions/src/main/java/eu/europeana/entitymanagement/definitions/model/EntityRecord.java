package eu.europeana.entitymanagement.definitions.model;

import java.util.List;

public interface EntityRecord {

    void setDbId(Long dbId);
    Long getDbId();

    void setProxies(List<EntityProxy> proxies);

    List<EntityProxy> getProxies();

    void setIsAggregatedBy(Aggregation isAggregatedBy);

    Aggregation getIsAggregatedBy();

    void setEntityId(String entityId);

    String getEntityId();

    void setEntity(Entity entity);

    Entity getEntity();

}
