package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BEGIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.END;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_PART;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
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
import java.util.Map;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  DEPICTION,
  IS_SHOWN_BY,
  PREF_LABEL,
  ALT_LABEL,
  BEGIN,
  END,
  NOTE,
  HAS_PART,
  IS_PART_OF,
  IS_NEXT_IN_SEQUENCE,
  SAME_AS
})
public class Timespan extends Entity {

  public Timespan(Timespan copy) {
    super(copy);
    if (copy.getIsNextInSequence() != null)
      this.isNextInSequence = new ArrayList<>(copy.getIsNextInSequence());
    this.begin = copy.getBeginString();
    this.end = copy.getEndString();
  }

  public Timespan() {
    super();
    // TODO Auto-generated constructor stub
  }

  private List<String> isNextInSequence;
  private String begin;
  private String end;
  private List<String> sameAs;

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

  @Deprecated
  public void setIsPartOf(Map<String, List<String>> isPartOf) {
    // TODO Auto-generated method stub
  }

  @Deprecated
  public Map<String, List<String>> getBegin() {
    // TODO Auto-generated method stub
    return null;
  }

  @Deprecated
  public Map<String, List<String>> getDctermsHasPart() {
    // TODO Auto-generated method stub
    return null;
  }

  @Deprecated
  public Map<String, List<String>> getEnd() {
    // TODO Auto-generated method stub
    return null;
  }

  @Deprecated
  public Map<String, List<String>> getIsPartOf() {
    // TODO Auto-generated method stub
    return null;
  }

  @Deprecated
  public void setBegin(Map<String, List<String>> arg0) {
    // TODO Auto-generated method stub

  }

  @Deprecated
  public void setDctermsHasPart(Map<String, List<String>> arg0) {
    // TODO Auto-generated method stub

  }

  @Deprecated
  public void setEnd(Map<String, List<String>> arg0) {
    // TODO Auto-generated method stub

  }

  public String getType() {
    return EntityTypes.Timespan.getEntityType();
  }

  public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
    // TODO:in case of the performance overhead cause by using the reflecion code, change this
    // method to call the getters for each field individually
    return field.get(this);
  }

  public void setFieldValue(Field field, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    // TODO:in case of the performance overhead cause by using the reflecion code, change this
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
