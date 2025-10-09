package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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

  private String type = EntityTypes.TimeSpan.getEntityType();
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
    if (copy.getSameReferenceLinks() != null) this.sameAs = (new ArrayList<>(copy.getSameReferenceLinks()));
  }

  public TimeSpan() {
    super();
  }

  @JsonGetter(IS_NEXT_IN_SEQUENCE)
  @JacksonXmlProperty(namespace = NAMESPACE_EDM, localName = IS_NEXT_IN_SEQUENCE)
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
  @JacksonXmlProperty(namespace = NAMESPACE_EDM, localName = BEGIN)
  public String getBeginString() {
    return begin;
  }

  @JsonGetter(END)
  @JacksonXmlProperty(namespace = NAMESPACE_EDM, localName = END)
  public String getEndString() {
    return end;
  }

  public String getType() {
    return type;
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
