package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BROADER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BROAD_MATCH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CLOSE_MATCH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.EXACT_MATCH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IN_SCHEME;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NARROWER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NARROW_MATCH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NOTATION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NOTE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.RELATED;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.RELATED_MATCH;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  DEPICTION,
  IS_SHOWN_BY,
  PREF_LABEL,
  ALT_LABEL,
  HIDDEN_LABEL,
  NOTE,
  NOTATION,
  BROADER,
  NARROWER,
  RELATED,
  BROAD_MATCH,
  NARROW_MATCH,
  RELATED_MATCH,
  CLOSE_MATCH,
  EXACT_MATCH,
  IN_SCHEME,
  IS_AGGREGATED_BY
})
public class Concept extends Entity {

  private String type = EntityTypes.Concept.getEntityType();
  private List<String> broader;
  private List<String> narrower;
  private List<String> related;
  private List<String> broadMatch;
  private List<String> narrowMatch;
  private List<String> exactMatch;
  private List<String> coref;
  private List<String> relatedMatch;
  private List<String> closeMatch;
  private Map<String, List<String>> notation;

  public Concept() {
    super();
  }

  public Concept(Concept copy) {
    super(copy);
    if (copy.getBroader() != null) this.broader = new ArrayList<>(copy.getBroader());
    if (copy.getNarrower() != null) this.narrower = new ArrayList<>(copy.getNarrower());
    if (copy.getRelated() != null) this.related = new ArrayList<>(copy.getRelated());
    if (copy.getBroadMatch() != null) this.broadMatch = new ArrayList<>(copy.getBroadMatch());
    if (copy.getNarrowMatch() != null) this.narrowMatch = new ArrayList<>(copy.getNarrowMatch());
    if (copy.getCoref() != null) this.coref = new ArrayList<>(copy.getCoref());
    if (copy.getRelatedMatch() != null) this.relatedMatch = new ArrayList<>(copy.getRelatedMatch());
    if (copy.getCloseMatch() != null) this.closeMatch = new ArrayList<>(copy.getCloseMatch());
    if (copy.getInScheme() != null) this.inScheme = new ArrayList<>(copy.getInScheme());
    if (copy.getNotation() != null) this.notation = new HashMap<>(copy.getNotation());
    if (copy.exactMatch != null) this.exactMatch = (new ArrayList<>(copy.exactMatch));
  }

  @JsonGetter(BROADER)
  public List<String> getBroader() {
    return broader;
  }

  @JsonSetter(BROADER)
  public void setBroader(List<String> broader) {
    this.broader = broader;
  }

  @JsonGetter(NARROWER)
  public List<String> getNarrower() {
    return narrower;
  }

  @JsonSetter(NARROWER)
  public void setNarrower(List<String> narrower) {
    this.narrower = narrower;
  }

  @JsonGetter(RELATED)
  public List<String> getRelated() {
    return related;
  }

  @JsonSetter(RELATED)
  public void setRelated(List<String> related) {
    this.related = related;
  }

  @JsonGetter(BROAD_MATCH)
  public List<String> getBroadMatch() {
    return broadMatch;
  }

  @JsonSetter(BROAD_MATCH)
  public void setBroadMatch(List<String> broadMatch) {
    this.broadMatch = broadMatch;
  }

  @JsonGetter(NARROW_MATCH)
  public List<String> getNarrowMatch() {
    return narrowMatch;
  }

  @JsonSetter(NARROW_MATCH)
  public void setNarrowMatch(List<String> narrowMatch) {
    this.narrowMatch = narrowMatch;
  }

  public List<String> getCoref() {
    return coref;
  }

  public void setCoref(List<String> coref) {
    this.coref = coref;
  }

  @JsonGetter(RELATED_MATCH)
  public List<String> getRelatedMatch() {
    return relatedMatch;
  }

  @JsonSetter(RELATED_MATCH)
  public void setRelatedMatch(List<String> relatedMatch) {
    this.relatedMatch = relatedMatch;
  }

  @JsonGetter(CLOSE_MATCH)
  public List<String> getCloseMatch() {
    return closeMatch;
  }

  @JsonSetter(CLOSE_MATCH)
  public void setCloseMatch(List<String> closeMatch) {
    this.closeMatch = closeMatch;
  }

  @JsonGetter(NOTATION)
  public Map<String, List<String>> getNotation() {
    return notation;
  }

  @JsonSetter(NOTATION)
  public void setNotation(Map<String, List<String>> notation) {
    this.notation = notation;
  }

  public String getType() {
    return type;
  }

  @Override
  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    // method to call the getters for each field individually
    return field.get(this);
  }

  @Override
  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    // method to call the setter for each field individually
    field.set(this, value);
  }

  @Override
  @JsonSetter(EXACT_MATCH)
  public void setSameReferenceLinks(List<String> uris) {
    this.exactMatch = uris;
  }

  @Override
  @JsonGetter(EXACT_MATCH)
  public List<String> getSameReferenceLinks() {
    return this.exactMatch;
  }
}
