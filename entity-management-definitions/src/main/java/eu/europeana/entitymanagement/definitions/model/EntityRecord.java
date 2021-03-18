package eu.europeana.entitymanagement.definitions.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;

@JsonDeserialize(as = EntityRecordImpl.class)
public interface EntityRecord {

    @Deprecated
    /**
     * should be moved to a different interface
     * @param dbId
     */
    void setDbId(long dbId);
    @Deprecated
    /**
     * should be moved to a different interface
     * @param dbId
     */
    long getDbId();

    void addProxy(EntityProxy proxies);

    List<EntityProxy> getProxies();

    void setEntityId(String entityId);

    String getEntityId();

    void setEntity(Entity entity);

    Entity getEntity();
    
    boolean getDisabled();
    
    void setDisabled(boolean disabledParam);
    
    EntityProxy getEuropeanaProxy();
    
    EntityProxy getExternalProxy();
}