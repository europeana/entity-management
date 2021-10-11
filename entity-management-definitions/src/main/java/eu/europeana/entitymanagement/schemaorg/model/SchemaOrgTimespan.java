package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgTimespan extends SchemaOrgEntity<Timespan> {

  private final eu.europeana.corelib.edm.model.schemaorg.Timespan schemaOrgTimespan;

  public SchemaOrgTimespan(Timespan timespan) {
    schemaOrgTimespan = new eu.europeana.corelib.edm.model.schemaorg.Timespan();
    SchemaOrgUtils.processEntity(timespan, schemaOrgTimespan);
  }

  @Override
  public ContextualEntity get() {
    return schemaOrgTimespan;
  }
}
