package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgOrganization extends SchemaOrgEntity<Organization> {

  private final eu.europeana.corelib.edm.model.schemaorg.EdmOrganization schemaOrgOrganization;

  public SchemaOrgOrganization(Organization ogranization) {
    schemaOrgOrganization = new eu.europeana.corelib.edm.model.schemaorg.EdmOrganization();
    SchemaOrgUtils.processEntity(ogranization, schemaOrgOrganization);
  }

  @Override
  public ContextualEntity get() {
    return schemaOrgOrganization;
  }
}
