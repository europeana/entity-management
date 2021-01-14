package eu.europeana.entitymanagement.definitions.model.impl;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;

public class BaseEntityProxy implements EntityProxy {

    String proxyId;
    Entity entity;
    String proxyFor;
    Aggregation proxyIn;
    String type;
    
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProxyId() {
		return proxyId;
	}

	public void setProxyId(String proxyId) {
		this.proxyId = proxyId;
	}	
    
    public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}	
    
    public String getProxyFor() {
		return proxyFor;
	}

	public void setProxyFor(String proxyFor) {
		this.proxyFor = proxyFor;
	}

	public Aggregation getProxyIn() {
		return proxyIn;
	}

	public void setProxyIn(Aggregation proxyIn) {
		this.proxyIn = proxyIn;
	}
}
