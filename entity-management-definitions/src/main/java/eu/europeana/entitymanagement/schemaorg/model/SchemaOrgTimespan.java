package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.Thing;
import eu.europeana.entitymanagement.definitions.model.Timespan;

public class SchemaOrgTimespan extends SchemaOrgEntity<Timespan> {

    private final eu.europeana.corelib.edm.model.schemaorg.Timespan schemaOrgTimespan;

    public SchemaOrgTimespan(Timespan timespan) {
    	schemaOrgTimespan = new eu.europeana.corelib.edm.model.schemaorg.Timespan();
        setCommonProperties(schemaOrgTimespan, timespan);
    }

    @Override
    public Thing get() {
        return schemaOrgTimespan;
    }
}
