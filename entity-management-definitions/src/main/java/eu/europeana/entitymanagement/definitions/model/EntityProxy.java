package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.definitions.model.impl.EntityProxyImpl;

@Embedded
@JsonDeserialize(as = EntityProxyImpl.class)
public interface EntityProxy {

    public String getProxyId();

    public void setProxyId(String proxyId);

    public Entity getEntity();

    public void setEntity(Entity entity);

    public String getProxyFor();

    public void setProxyFor(String proxyFor);

    public Aggregation getProxyIn();

    public void setProxyIn(Aggregation proxyIn);

    public String getType();

    public void setType(String type);

}
