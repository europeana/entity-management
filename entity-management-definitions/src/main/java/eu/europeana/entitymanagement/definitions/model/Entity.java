package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.ENTITY_CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_PART;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IDENTIFIER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IN_SCHEME;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_PART_OF;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_RELATED_TO;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NOTE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidationInterface;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsDataSourceProxyValidationInterface;
import eu.europeana.entitymanagement.normalization.EntityFieldsEuropeanaProxyValidationGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsEuropeanaProxyValidationInterface;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.ValidationObject;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

@dev.morphia.annotations.Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Agent.class, name = "Agent"),
  @JsonSubTypes.Type(value = Concept.class, name = "Concept"),
  @JsonSubTypes.Type(value = Organization.class, name = "Organization"),
  @JsonSubTypes.Type(value = Place.class, name = "Place"),
  @JsonSubTypes.Type(value = TimeSpan.class, name = "TimeSpan")
})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@EntityFieldsCompleteValidationInterface(groups = {EntityFieldsCompleteValidationGroup.class})
@EntityFieldsEuropeanaProxyValidationInterface(
    groups = {EntityFieldsEuropeanaProxyValidationGroup.class})
@EntityFieldsDataSourceProxyValidationInterface(
    groups = {EntityFieldsDataSourceProxyValidationGroup.class})
public abstract class Entity implements ValidationObject {

  protected String entityId;
  // ID of entityRecord in database

  protected Map<String, List<String>> note;
  protected Map<String, String> prefLabel;
  protected Map<String, List<String>> altLabel;
  protected List<String> hiddenLabel;

  protected List<String> identifier;
  protected List<String> isRelatedTo;
  protected List<String> inScheme;

  // hierarchical structure available only for a part of entities. Add set/get
  // methods to the appropriate interfaces
  protected List<String> hasPart;
  protected List<String> isPartOf;

  protected Aggregation isAggregatedBy;
  protected WebResource isShownBy;
  protected WebResource depiction;
  protected String payload;

  protected Entity() {}

  protected <T extends Entity> Entity(T copy) {
    this.entityId = copy.getEntityId();
    this.depiction = copy.getDepiction();
    if (copy.getNote() != null) this.note = new HashMap<>(copy.getNote());
    if (copy.getPrefLabel() != null) this.prefLabel = new HashMap<>(copy.getPrefLabel());
    if (copy.getAltLabel() != null) this.altLabel = new HashMap<>(copy.getAltLabel());
    if (copy.getHiddenLabel() != null) this.hiddenLabel = new ArrayList<>(copy.getHiddenLabel());
    if (copy.getIdentifier() != null) this.identifier = new ArrayList<>(copy.getIdentifier());
    if (copy.getIsRelatedTo() != null) this.isRelatedTo = new ArrayList<>(copy.getIsRelatedTo());
    if (copy.getInScheme() != null) this.inScheme = new ArrayList<>(copy.getInScheme());
    if (copy.getHasPart() != null) this.hasPart = new ArrayList<>(copy.getHasPart());
    if (copy.getIsPartOfArray() != null) this.isPartOf = new ArrayList<>(copy.getIsPartOfArray());
    if (copy.getIsAggregatedBy() != null)
      this.isAggregatedBy = new Aggregation(copy.getIsAggregatedBy());
    if (copy.getIsShownBy() != null) this.isShownBy = new WebResource(copy.getIsShownBy());

    this.payload = copy.getPayload();
  }

  @JsonGetter(IN_SCHEME)
  public List<String> getInScheme() {
    return inScheme;
  }

  @JsonSetter(IN_SCHEME)
  public void setInScheme(List<String> inScheme) {
    this.inScheme = inScheme;
  }

  @JsonGetter(WebEntityFields.PREF_LABEL)
  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  @JsonSetter(WebEntityFields.PREF_LABEL)
  public void setPrefLabel(Map<String, String> prefLabel) {
    this.prefLabel = prefLabel;
  }

  @JsonGetter(ALT_LABEL)
  public Map<String, List<String>> getAltLabel() {
    return altLabel;
  }

  @JsonSetter(ALT_LABEL)
  public void setAltLabel(Map<String, List<String>> altLabel) {
    this.altLabel = altLabel;
  }

  @JsonGetter(HIDDEN_LABEL)
  public List<String> getHiddenLabel() {
    return hiddenLabel;
  }

  @JsonSetter(HIDDEN_LABEL)
  public void setHiddenLabel(List<String> hiddenLabel) {
    this.hiddenLabel = hiddenLabel;
  }

  @JsonGetter(NOTE)
  public Map<String, List<String>> getNote() {
    return note;
  }

  @JsonSetter(NOTE)
  public void setNote(Map<String, List<String>> note) {
    this.note = note;
  }

  @JsonGetter(TYPE)
  public abstract String getType();

  @JsonGetter(ID)
  public String getEntityId() {
    return entityId;
  }

  @JsonSetter(ID)
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  @JsonGetter(IDENTIFIER)
  public List<String> getIdentifier() {
    return identifier;
  }

  @JsonSetter(IDENTIFIER)
  public void setIdentifier(List<String> identifier) {
    this.identifier = identifier;
  }

  @JsonIgnore
  public String getAbout() {
    return getEntityId();
  }

  public void setAbout(String about) {
    setEntityId(about);
  }

  @JsonGetter(IS_RELATED_TO)
  public List<String> getIsRelatedTo() {
    return isRelatedTo;
  }

  @JsonSetter(IS_RELATED_TO)
  public void setIsRelatedTo(List<String> isRelatedTo) {
    this.isRelatedTo = isRelatedTo;
  }

  @JsonGetter(HAS_PART)
  public List<String> getHasPart() {
    return hasPart;
  }

  @JsonSetter(HAS_PART)
  public void setHasPart(List<String> hasPart) {
    this.hasPart = hasPart;
  }

  @JsonGetter(IS_PART_OF)
  public List<String> getIsPartOfArray() {
    return isPartOf;
  }

  @JsonSetter(IS_PART_OF)
  public void setIsPartOfArray(List<String> isPartOf) {
    this.isPartOf = isPartOf;
  }

  @JsonGetter(WebEntityFields.DEPICTION)
  public WebResource getDepiction() {
    return depiction;
  }

  @JsonSetter(WebEntityFields.DEPICTION)
  public void setDepiction(WebResource depiction) {
    this.depiction = depiction;
  }

  @JsonGetter(WebEntityFields.IS_SHOWN_BY)
  public WebResource getIsShownBy() {
    return isShownBy;
  }

  @JsonSetter(IS_SHOWN_BY)
  public void setIsShownBy(WebResource resource) {
    isShownBy = resource;
  }

  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    return field.get(this);
  }

  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    field.set(this, value);
  }

  @JsonGetter(IS_AGGREGATED_BY)
  public Aggregation getIsAggregatedBy() {
    return isAggregatedBy;
  }

  /** Not included in XML responses */
  @JsonGetter(CONTEXT)
  public String getContext() {
    return ENTITY_CONTEXT;
  }

  @JsonSetter(IS_AGGREGATED_BY)
  public void setIsAggregatedBy(Aggregation isAggregatedBy) {
    this.isAggregatedBy = isAggregatedBy;
  }

  @JsonIgnore
  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public static Map<String, List<String>> toStringListMap(Map<String, String> stringMap) {
    return stringMap.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> Collections.singletonList(entry.getValue())));
  }

  /**
   * Gets list of URIs that refer to the same entity. For {@link Concept}, this is the
   * skos:ExactMatch. For all other entity types, this is owl:sameAs.
   */
  public abstract List<String> getSameReferenceLinks();

  /**
   * Sets list of URIs that refer to the same entity. For {@link Concept}, this is the
   * skos:ExactMatch. For all other entity types, this is owl:sameAs.
   *
   * @param uris List of entity uris
   */
  public abstract void setSameReferenceLinks(List<String> uris);

  /**
   * adds the provided uri to the same reference list if not already included
   *
   * @param uri uri to check
   */
  public void addSameReferenceLink(String uri) {
    if (getSameReferenceLinks() == null) {
      setSameReferenceLinks(new ArrayList<>());
    }
    if (!getSameReferenceLinks().contains(uri)) {
      getSameReferenceLinks().add(uri);
    }
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Entity that = (Entity) o;

    if (! Objects.equals(entityId, that.getEntityId())) return false;
    if (! EntityUtils.compareStringListStringMaps(note, that.getNote())) return false;
    if (! EntityUtils.compareStringStringMaps(prefLabel, that.getPrefLabel())) return false;
    if (! EntityUtils.compareStringListStringMaps(altLabel, that.getAltLabel())) return false;
    if (! EntityUtils.compareLists(hiddenLabel, that.getHiddenLabel())) return false;
    if (! EntityUtils.compareLists(identifier, that.getIdentifier())) return false;
    if (! EntityUtils.compareLists(isRelatedTo, that.getIsRelatedTo())) return false;
    if (! EntityUtils.compareLists(inScheme, that.getInScheme())) return false;
    if (! EntityUtils.compareLists(hasPart, that.getHasPart())) return false;
    if (! EntityUtils.compareLists(isPartOf, that.getIsPartOfArray())) return false;
    if (! Objects.equals(isAggregatedBy, that.getIsAggregatedBy())) return false;
    if (! Objects.equals(isShownBy, that.getIsShownBy())) return false;
    if (! Objects.equals(depiction, that.getDepiction())) return false;
    return Objects.equals(payload, that.getPayload());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
    result = prime * result + ((note == null) ? 0 : note.hashCode());
    result = prime * result + ((prefLabel == null) ? 0 : prefLabel.hashCode());
    result = prime * result + ((altLabel == null) ? 0 : altLabel.hashCode());
    result = prime * result + ((hiddenLabel == null) ? 0 : hiddenLabel.hashCode());
    result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
    result = prime * result + ((isRelatedTo == null) ? 0 : isRelatedTo.hashCode());
    result = prime * result + ((inScheme == null) ? 0 : inScheme.hashCode());
    result = prime * result + ((hasPart == null) ? 0 : hasPart.hashCode());
    result = prime * result + ((isPartOf == null) ? 0 : isPartOf.hashCode());
    result = prime * result + ((isAggregatedBy == null) ? 0 : isAggregatedBy.hashCode());
    result = prime * result + ((isShownBy == null) ? 0 : isShownBy.hashCode());
    result = prime * result + ((depiction == null) ? 0 : depiction.hashCode());
    result = prime * result + ((payload == null) ? 0 : payload.hashCode());
    return result;
  }
  
}
