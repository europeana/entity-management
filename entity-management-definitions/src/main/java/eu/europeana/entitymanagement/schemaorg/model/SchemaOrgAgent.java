package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgAgent extends SchemaOrgEntity<Agent> {

  private final eu.europeana.corelib.edm.model.schemaorg.Person schemaOrgAgent;

  public SchemaOrgAgent(Agent agent) {
    schemaOrgAgent = new eu.europeana.corelib.edm.model.schemaorg.Person();
    SchemaOrgUtils.processEntity(agent, schemaOrgAgent);
  }

  @Override
  public ContextualEntity get() {
    return schemaOrgAgent;
  }
}
