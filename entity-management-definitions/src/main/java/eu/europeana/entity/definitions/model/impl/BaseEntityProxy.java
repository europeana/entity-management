package eu.europeana.entity.definitions.model.impl;

import eu.europeana.entity.definitions.model.Aggregation;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.EntityProxy;

public class BaseEntityProxy implements EntityProxy {

    String id;
    
    Entity entity;
    
    String proxyFor;
    
    Aggregation proxyIn;
}
