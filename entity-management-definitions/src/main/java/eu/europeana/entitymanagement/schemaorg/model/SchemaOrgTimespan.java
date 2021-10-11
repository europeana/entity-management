package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgTimespan extends SchemaOrgEntity<TimeSpan> {

  private final eu.europeana.corelib.edm.model.schemaorg.Timespan schemaOrgTimespan;

  public SchemaOrgTimespan(TimeSpan timespan) {
    schemaOrgTimespan = new eu.europeana.corelib.edm.model.schemaorg.Timespan();
    SchemaOrgUtils.processEntity(timespan, schemaOrgTimespan);
  }

  @Override
  public ContextualEntity get() {
    return schemaOrgTimespan;
  }
}
