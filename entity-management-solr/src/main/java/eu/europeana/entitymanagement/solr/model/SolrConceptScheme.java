package eu.europeana.entitymanagement.solr.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
  
  @Field(EntitySolrFields.TOTAL)
  private int total;
  
  @Field(EntitySolrFields.ITEMS)
  private List<String> items;

  @Field(EntitySolrFields.CREATED)
  private Date created;
  
  @Field(EntitySolrFields.MODIFIED)
  private Date modified;

  public SolrConceptScheme() {
    super();
  }

  public SolrConceptScheme(ConceptScheme conceptScheme) {
    this.type = conceptScheme.getType();
    this.entityId = conceptScheme.getEntityId();
    if (conceptScheme.getItems() != null)
      this.items = new ArrayList<>(conceptScheme.getItems());
    this.total = conceptScheme.getTotal();
    this.created = conceptScheme.getCreated();
    this.modified = conceptScheme.getModified();
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
