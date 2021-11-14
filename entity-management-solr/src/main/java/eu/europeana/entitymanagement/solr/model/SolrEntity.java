package eu.europeana.entitymanagement.solr.model;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;

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

  @Field(EntitySolrFields.HIDDEN_LABEL)
  private Map<String, List<String>> hiddenLabel;

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
  private int docCount;

  @Field(EntitySolrFields.PAGERANK)
  private float pageRank;

  @Field(EntitySolrFields.DERIVED_SCORE)
  private float derivedScore;

  public SolrEntity(T entity) {
    this.type = entity.getType();
    this.entityId = entity.getEntityId();
    if (entity.getDepiction() != null) {
      this.depiction = entity.getDepiction().getId();
    }
    setNote(entity.getNote());
    setPrefLabelStringMap(entity.getPrefLabel());
    setAltLabel(entity.getAltLabel());
    setHiddenLabel(entity.getHiddenLabel());
    setIsShownBy(entity.getIsShownBy());
    if (entity.getIdentifier() != null) this.identifier = new ArrayList<>(entity.getIdentifier());
    if (entity.getSameReferenceLinks() != null)
      this.setSameReferenceLinks(new ArrayList<>(entity.getSameReferenceLinks()));
    if (entity.getIsRelatedTo() != null)
      this.isRelatedTo = new ArrayList<>(entity.getIsRelatedTo());
    if (entity.getHasPart() != null) this.hasPart = new ArrayList<>(entity.getHasPart());
    if (entity.getIsPartOfArray() != null)
      this.isPartOf = new ArrayList<>(entity.getIsPartOfArray());

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
                  AgentSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  prefLabel));
    }
  }

  private void setAltLabel(Map<String, List<String>> altLabel) {
    if (MapUtils.isNotEmpty(altLabel)) {
      this.altLabel =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  AgentSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel));
    }
  }

  private void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
    if (MapUtils.isNotEmpty(hiddenLabel)) {
      this.hiddenLabel =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  AgentSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  hiddenLabel));
    }
  }

  private void setNote(Map<String, List<String>> note) {
    if (MapUtils.isNotEmpty(note)) {
      this.note =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  AgentSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note));
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

  public int getDocCount() {
    return docCount;
  }

  public void setDocCount(int docCount) {
    this.docCount = docCount;
  }

  public Float getPageRank() {
    return pageRank;
  }

  public void setPageRank(float pageRank) {
    this.pageRank = pageRank;
  }

  public float getDerivedScore() {
    return derivedScore;
  }

  public void setDerivedScore(float derivedScore) {
    this.derivedScore = derivedScore;
  }

  protected abstract void setSameReferenceLinks(ArrayList<String> uris);
}
