package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Entity;

public abstract class SchemaOrgEntity<T extends Entity> {

    public abstract ContextualEntity get();

}
