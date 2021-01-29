package eu.europeana.entitymanagement.definitions.model;

import dev.morphia.annotations.Embedded;

@Embedded
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
