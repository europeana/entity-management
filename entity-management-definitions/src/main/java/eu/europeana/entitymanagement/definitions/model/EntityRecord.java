package eu.europeana.entitymanagement.definitions.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;

@JsonDeserialize(as = EntityRecordImpl.class)
public interface EntityRecord {


    void addProxy(EntityProxy proxies);

    List<EntityProxy> getProxies();

    void setEntityId(String entityId);

    String getEntityId();

    void setEntity(Entity entity);

    Entity getEntity();
    
    boolean isDisabled();
    
    void setDisabled(boolean disabledParam);

    void setCreated(Date date);

    Date getCreated();

    void setModified(Date date);

    Date getModified();
    
    EntityProxy getEuropeanaProxy();
    
    EntityProxy getExternalProxy();
}