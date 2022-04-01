package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALTITUDE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_PART;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_PART_OF;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LATITUDE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LATITUDE_LONGITUDE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LONGITUDE;
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
  LATITUDE,
  LONGITUDE,
  ALTITUDE,
  LATITUDE_LONGITUDE,
  NOTE,
  HAS_PART,
  IS_PART_OF,
  IS_NEXT_IN_SEQUENCE,
  SAME_AS,
  IS_AGGREGATED_BY
})
public class Place extends Entity {

  private String type = EntityTypes.Place.name();
  private List<String> isNextInSequence;
  private Float latitude;
  private Float longitude;
  private Float altitude;
  private List<String> sameAs;

  public Place(Place copy) {
    super(copy);
    if (copy.getIsNextInSequence() != null)
      this.isNextInSequence = new ArrayList<>(copy.getIsNextInSequence());
    this.latitude = copy.getLatitude();
    this.longitude = copy.getLongitude();
    this.altitude = copy.getAltitude();
    if (copy.sameAs != null) this.sameAs = (new ArrayList<>(copy.sameAs));
  }

  public Place() {
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

  @JsonGetter(LATITUDE)
  @JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LAT)
  public Float getLatitude() {
    return latitude;
  }

  @JsonSetter(LATITUDE)
  public void setLatitude(Float latitude) {
    this.latitude = latitude;
  }

  @JsonGetter(LONGITUDE)
  @JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LONG)
  public Float getLongitude() {
    return longitude;
  }

  @JsonSetter(LONGITUDE)
  public void setLongitude(Float longitude) {
    this.longitude = longitude;
  }

  @JsonGetter(ALTITUDE)
  @JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_ALT)
  public Float getAltitude() {
    return altitude;
  }

  @JsonSetter(ALTITUDE)
  public void setAltitude(Float altitude) {
    this.altitude = altitude;
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

  public String getType() {
    return type;
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
}
