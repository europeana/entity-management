package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgOrganization extends SchemaOrgEntity<Organization> {

    private final eu.europeana.corelib.edm.model.schemaorg.EdmOrganization schemaOrgOrganization;

    public SchemaOrgOrganization(Organization ogranization) {
        schemaOrgOrganization = new eu.europeana.corelib.edm.model.schemaorg.EdmOrganization();
        SchemaOrgUtils.processEntity(ogranization, schemaOrgOrganization);        
        
//        if(ogranization.getDescription()!=null) {
//			for (Entry<String, String> descriptionEntry : ogranization.getDescription().entrySet()) {
//				MultilingualString descriptionEntrySchemaOrg = new MultilingualString();
//				descriptionEntrySchemaOrg.setLanguage(descriptionEntry.getKey());
//				descriptionEntrySchemaOrg.setValue(descriptionEntry.getValue());
//				schemaOrgOrganization.addDescription(descriptionEntrySchemaOrg);
//			}
//		}	
//
//        setCommonProperties(schemaOrgOrganization, ogranization);
    }

    @Override
    public ContextualEntity get() {
        return schemaOrgOrganization;
    }
}
