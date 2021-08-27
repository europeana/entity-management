package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgConcept extends SchemaOrgEntity<Concept> {

    private final eu.europeana.corelib.edm.model.schemaorg.Concept schemaOrgConcept;

    public SchemaOrgConcept(Concept concept) {
    	schemaOrgConcept = new eu.europeana.corelib.edm.model.schemaorg.Concept();
    	SchemaOrgUtils.processEntity(concept, schemaOrgConcept);
    	
//        setCommonProperties(schemaOrgConcept, concept);
    }

    @Override
    public ContextualEntity get() {
        return schemaOrgConcept;
    }
}
