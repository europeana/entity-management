package eu.europeana.entitymanagement.schemaorg.model;

import java.util.Map.Entry;

import eu.europeana.corelib.edm.model.schemaorg.MultilingualString;
import eu.europeana.corelib.edm.model.schemaorg.Thing;
import eu.europeana.entitymanagement.definitions.model.Organization;

public class SchemaOrgOrganization extends SchemaOrgEntity<Organization> {

    private final eu.europeana.corelib.edm.model.schemaorg.Organization schemaOrgOrganization;

    public SchemaOrgOrganization(Organization ogranization) {
        schemaOrgOrganization = new eu.europeana.corelib.edm.model.schemaorg.Organization();
		if(ogranization.getDescription()!=null) {
			for (Entry<String, String> descriptionEntry : ogranization.getDescription().entrySet()) {
				MultilingualString descriptionEntrySchemaOrg = new MultilingualString();
				descriptionEntrySchemaOrg.setLanguage(descriptionEntry.getKey());
				descriptionEntrySchemaOrg.setValue(descriptionEntry.getValue());
				schemaOrgOrganization.addDescription(descriptionEntrySchemaOrg);
			}
		}	

        setCommonProperties(schemaOrgOrganization, ogranization);
    }

    @Override
    public Thing get() {
        return schemaOrgOrganization;
    }
}
