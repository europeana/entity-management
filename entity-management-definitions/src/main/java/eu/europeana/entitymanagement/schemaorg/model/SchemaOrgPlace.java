package eu.europeana.entitymanagement.schemaorg.model;

import eu.europeana.corelib.edm.model.schemaorg.ContextualEntity;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.utils.SchemaOrgUtils;

public class SchemaOrgPlace extends SchemaOrgEntity<Place> {

  private final eu.europeana.corelib.edm.model.schemaorg.Place schemaOrgPlace;

  public SchemaOrgPlace(Place place) {
    schemaOrgPlace = new eu.europeana.corelib.edm.model.schemaorg.Place();
    SchemaOrgUtils.processEntity(place, schemaOrgPlace);
  }

  @Override
  public ContextualEntity get() {
    return schemaOrgPlace;
  }
}
