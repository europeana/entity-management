package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BEGIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.END;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_PART;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_PART_OF;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.NOTE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SAME_AS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
  BEGIN,
  END,
  NOTE,
  HAS_PART,
  IS_PART_OF,
  IS_NEXT_IN_SEQUENCE,
  SAME_AS,
  IS_AGGREGATED_BY
})
public class TimeSpan extends Entity {

  private String type = EntityTypes.TimeSpan.name();
  private List<String> isNextInSequence;
  private String begin;
  private String end;
  private List<String> sameAs;

  public TimeSpan(TimeSpan copy) {
    super(copy);
    if (copy.getIsNextInSequence() != null)
      this.isNextInSequence = new ArrayList<>(copy.getIsNextInSequence());
    this.begin = copy.getBeginString();
    this.end = copy.getEndString();
    if (copy.sameAs != null) this.sameAs = (new ArrayList<>(copy.sameAs));
  }

  public TimeSpan() {
    super();
  }

  @JsonGetter(IS_NEXT_IN_SEQUENCE)
  @JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
  public List<String> getIsNextInSequence() {
    return isNextInSequence;
  }

  @JsonSetter(IS_NEXT_IN_SEQUENCE)
  public void setIsNextInSequence(List<String> isNextInSequence) {
    this.isNextInSequence = isNextInSequence;
  }

  @JsonSetter(BEGIN)
  public void setBeginString(String begin) {
    this.begin = begin;
  }

  @JsonSetter(END)
  public void setEndString(String end) {
    this.end = end;
  }

  @JsonGetter(BEGIN)
  @JacksonXmlProperty(localName = XmlFields.XML_EDM_BEGIN)
  public String getBeginString() {
    return begin;
  }

  @JsonGetter(END)
  @JacksonXmlProperty(localName = XmlFields.XML_EDM_END)
  public String getEndString() {
    return end;
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
  @JsonSetter(SAME_AS)
  public void setSameReferenceLinks(List<String> uris) {
    this.sameAs = uris;
  }

  @Override
  @JsonGetter(SAME_AS)
  public List<String> getSameReferenceLinks() {
    return this.sameAs;
  }
}
