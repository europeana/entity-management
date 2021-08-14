package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.Thing;
import eu.europeana.entitymanagement.definitions.model.Concept;

public class SchemaOrgConcept extends SchemaOrgEntity<Concept> {

    private final eu.europeana.corelib.edm.model.schemaorg.Concept schemaOrgConcept;

    public SchemaOrgConcept(Concept concept) {
    	schemaOrgConcept = new eu.europeana.corelib.edm.model.schemaorg.Concept();
        setCommonProperties(schemaOrgConcept, concept);
    }

    @Override
    public Thing get() {
        return schemaOrgConcept;
    }
}
