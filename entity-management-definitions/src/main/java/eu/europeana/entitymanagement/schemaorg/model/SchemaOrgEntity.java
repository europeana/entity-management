package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.Text;
import eu.europeana.corelib.edm.model.schemaorg.Thing;
import eu.europeana.entitymanagement.definitions.model.Entity;
import org.springframework.util.CollectionUtils;

public abstract class SchemaOrgEntity<T extends Entity> {

    public abstract Thing get();

    protected void setCommonProperties(Thing schemaOrgEntity, T entity) {
        schemaOrgEntity.setId(entity.getEntityId());
        if (!CollectionUtils.isEmpty(entity.getSameAs())) {
            entity.getSameAs()
                    .forEach(sameAsEach ->
                            schemaOrgEntity.addSameAs(new Text(sameAsEach))
                    );
        }
    }
}
