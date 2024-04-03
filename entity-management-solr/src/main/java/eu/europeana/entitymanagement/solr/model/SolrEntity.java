package eu.europeana.entitymanagement.solr.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

public abstract class SolrEntity<T extends Entity> {

  private T entity;

  @Field(EntitySolrFields.TYPE)
  private String type;

  @Field(EntitySolrFields.ID)
  private String entityId;

  @Field(EntitySolrFields.DEPICTION)
  private String depiction;

  @Field(EntitySolrFields.NOTE_ALL)
  private Map<String, List<String>> note;

  @Field(EntitySolrFields.PREF_LABEL_ALL)
  private Map<String, String> prefLabel;

  @Field(EntitySolrFields.ALT_LABEL_ALL)
  private Map<String, List<String>> altLabel;

  //  @Field(EntitySolrFields.HIDDEN_LABEL)
  //  private List<String> hiddenLabel;
  @Field(EntitySolrFields.HIDDEN_LABEL_ALL)
  private Map<String, List<String>> hiddenLabel;

  @Field(EntitySolrFields.LABEL_ENRICH_GENERAL)
  private List<String> labelEnrichGeneral;

  @Field(EntitySolrFields.LABEL_ENRICH_ALL)
  private Map<String, List<String>> labelEnrich;

  @Field(EntitySolrFields.IDENTIFIER)
  private List<String> identifier;

  @Field(EntitySolrFields.IS_RELATED_TO)
  private List<String> isRelatedTo;

  @Field(EntitySolrFields.HAS_PART)
  private List<String> hasPart;

  @Field(EntitySolrFields.IS_PART_OF)
  private List<String> isPartOf;

  @Field(EntitySolrFields.PAYLOAD)
  private String payload;

  @Field(EntitySolrFields.IS_SHOWN_BY_ALL)
  private Map<String, String> isShownBy;

  @Field(EntitySolrFields.SUGGEST_FILTERS)
  private List<String> suggestFilters;

  @Field(EntitySolrFields.RIGHTS)
  private List<String> rights;

  @Field(EntitySolrFields.EUROPEANA_DOC_COUNT)
  private Integer docCount;

  @Field(EntitySolrFields.PAGERANK)
  private Float pageRank;

  @Field(EntitySolrFields.DERIVED_SCORE)
  private Float derivedScore;

  @Field(EntitySolrFields.IN_SCHEME)
  private List<String> inScheme;
  
  
  @Field(EntitySolrFields.CREATED)
  private Date created;
  
  @Field(EntitySolrFields.MODIFIED)
  private Date modified;
  

  public SolrEntity(T entity) {
    this.type = entity.getType();
    this.entityId = entity.getEntityId();
    if (entity.getDepiction() != null) {
      this.depiction = entity.getDepiction().getId();
    }
    setNote(entity.getNote());
    setPrefLabelStringMap(entity.getPrefLabel());
    setAltLabel(entity.getAltLabel());
    setHiddenLabelMap(entity.getHiddenLabel());

    setIsShownBy(entity.getIsShownBy());
    if (entity.getIdentifier() != null) {
      this.identifier = new ArrayList<>(entity.getIdentifier());
    }
    if (entity.getIsRelatedTo() != null){
      this.isRelatedTo = new ArrayList<>(entity.getIsRelatedTo());
    }
    if (entity.getHasPart() != null) {
      this.hasPart = new ArrayList<>(entity.getHasPart());
    }
    if (entity.getIsPartOfArray() != null) {
      this.isPartOf = new ArrayList<>(entity.getIsPartOfArray());
    }
    if (entity.getInScheme() != null) {
      this.inScheme = new ArrayList<>(entity.getInScheme());
    }

    if(entity.getIsAggregatedBy() != null) {
      this.created = entity.getIsAggregatedBy().getCreated();
      this.modified = entity.getIsAggregatedBy().getModified();
    }
        
    
    this.entity = entity;
  }

  public SolrEntity() {
    // no-arg constructor
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  private void setPrefLabelStringMap(Map<String, String> prefLabel) {
    if (MapUtils.isNotEmpty(prefLabel)) {
      this.prefLabel =
          new HashMap<>(
              SolrUtils.normalizeStringMapByAddingPrefix(
                  EntitySolrFields.PREF_LABEL_FIELD_NAME + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  prefLabel));
    }
  }

  private void setAltLabel(Map<String, List<String>> altLabel) {
    if (MapUtils.isNotEmpty(altLabel)) {
      this.altLabel =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  EntitySolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel));
    }
  }

  public void setHiddenLabelMap(List<String> hiddenLabel) {
    if (hiddenLabel != null && !hiddenLabel.isEmpty()) {
      this.hiddenLabel = new HashMap<String, List<String>>();
      // convert array to language map
      this.hiddenLabel.put(EntitySolrFields.HIDDEN_LABEL_NO_LANG, new ArrayList<>(hiddenLabel));
    }
  }

  private void setNote(Map<String, List<String>> note) {
    if (MapUtils.isNotEmpty(note)) {
      this.note =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  EntitySolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note));
    }
  }

  private void setIsShownBy(WebResource webResource) {
    if (webResource == null) {
      return;
    }

    this.isShownBy = new HashMap<>();
    this.isShownBy.put(EntitySolrFields.IS_SHOWN_BY, webResource.getId());
    this.isShownBy.put(EntitySolrFields.IS_SHOWN_BY_SOURCE, webResource.getSource());
    this.isShownBy.put(EntitySolrFields.IS_SHOWN_BY_THUMBNAIL, webResource.getThumbnail());
  }

  public String getType() {
    return type;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getDepiction() {
    return depiction;
  }

  public Map<String, List<String>> getNote() {
    return note;
  }

  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  public Map<String, List<String>> getAltLabel() {
    return altLabel;
  }

  public Map<String, List<String>> getHiddenLabel() {
    return hiddenLabel;
  }

  public List<String> getIdentifier() {
    return identifier;
  }

  public List<String> getIsRelatedTo() {
    return isRelatedTo;
  }

  public List<String> getHasPart() {
    return hasPart;
  }

  public List<String> getIsPartOf() {
    return isPartOf;
  }

  public String getPayload() {
    return payload;
  }

  public T getEntity() {
    return entity;
  }

  public List<String> getSuggestFilters() {
    return suggestFilters;
  }

  public void setSuggestFilters(List<String> suggestFilters) {
    this.suggestFilters = suggestFilters;
  }

  public List<String> getRights() {
    return rights;
  }

  public void setRights(List<String> rights) {
    this.rights = rights;
  }

  public Integer getDocCount() {
    return docCount;
  }

  public void setDocCount(Integer docCount) {
    this.docCount = docCount;
  }

  public Float getPageRank() {
    return pageRank;
  }

  public void setPageRank(Float pageRank) {
    this.pageRank = pageRank;
  }

  public Float getDerivedScore() {
    return derivedScore;
  }

  public void setDerivedScore(Float derivedScore) {
    this.derivedScore = derivedScore;
  }

  public List<String> getInScheme() {
    return inScheme;
  }

  public void setInScheme(List<String> inScheme) {
    this.inScheme = inScheme;
  }

  protected abstract void setSameReferenceLinks(ArrayList<String> uris);

  public List<String> getLabelEnrichGeneral() {
    return labelEnrichGeneral;
  }

  public void setLabelEnrichGeneral(List<String> labelEnrichGeneral) {
    this.labelEnrichGeneral = labelEnrichGeneral;
  }

  public Map<String, List<String>> getLabelEnrich() {
    return labelEnrich;
  }

  public void setLabelEnrich(Map<String, List<String>> labelEnrich) {
    if (MapUtils.isNotEmpty(labelEnrich)) {
      this.labelEnrich =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  EntitySolrFields.LABEL_ENRICH_GENERAL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  labelEnrich));
    }
  }

  public Date getCreated() {
    return (Date)created.clone();
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getModified() {
    return (Date)modified.clone();
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }
}
