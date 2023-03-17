package eu.europeana.entitymanagement.solr.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.ConceptScheme;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.vocabulary.ConceptSchemeSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

public class SolrConceptScheme {

  @Field(EntitySolrFields.ID)
  private String entityId;

  @Field(EntitySolrFields.TYPE)
  private String type;

  @Field(EntitySolrFields.PREF_LABEL_ALL)
  private Map<String, String> prefLabel;

  @Field(ConceptSchemeSolrFields.DEFINITION_ALL)
  private Map<String, String> definition;

  public SolrConceptScheme() {
    super();
  }

  public SolrConceptScheme(ConceptScheme conceptScheme) {
    this.type = conceptScheme.getType();
    this.entityId = conceptScheme.getEntityId();
    setDefinitionStringMap(conceptScheme.getDefinition());
    setPrefLabelStringMap(conceptScheme.getPrefLabel());
  }

  private void setDefinitionStringMap(Map<String, String> definition) {
    if (MapUtils.isNotEmpty(definition)) {
      this.definition = new HashMap<>(
          SolrUtils.normalizeStringMapByAddingPrefix(
              ConceptSchemeSolrFields.DEFINITION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, definition
          )
      );
    }
  }

  private void setPrefLabelStringMap(Map<String, String> prefLabel) {
    if (MapUtils.isNotEmpty(prefLabel)) {
      this.prefLabel =
          new HashMap<>(
              SolrUtils.normalizeStringMapByAddingPrefix(
                  EntitySolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  prefLabel));
    }
  }
  
  public String getEntityId() {
    return entityId;
  }

}
