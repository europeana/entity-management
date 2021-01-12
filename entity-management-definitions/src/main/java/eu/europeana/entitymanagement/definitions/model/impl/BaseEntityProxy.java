package eu.europeana.entitymanagement.definitions.model.impl;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;

public class BaseEntityProxy implements EntityProxy {

    String id;
    
    Entity entity;
    
    String proxyFor;
    
    Aggregation proxyIn;
}
