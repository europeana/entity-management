package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC_TERMS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_WGS84_POS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ALTITUDE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_LATITUDE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_LONGITUDE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PLACE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_ALT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_LAT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_LONG;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_PLACE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={
    XML_LATITUDE,
    XML_LONGITUDE,
    XML_ALTITUDE,
    NOTE,
    XML_HAS_PART,
    XML_IS_PART_OF,
    XML_IS_NEXT_IN_SEQUENCE,
    XML_SAME_AS
})
public class XmlPlaceImpl extends XmlBaseEntityImpl<Place> {

  @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
  private List<LabelledResource> sameAs;

  @XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_LAT)
  private Float latitude;

  @XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_LONG)
  private Float longitude;

  @XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_ALT)
  private Float altitude;

  @XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
  private List<LabelledResource> note;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_HAS_PART)
  private List<LabelledResource> hasPart;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_IS_PART_OF)
  private List<LabelledResource> isPartOf;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_IS_NEXT_IN_SEQUENCE)
  private List<LabelledResource> isNextInSequence;

  public XmlPlaceImpl(Place place) {
    super(place);
    this.sameAs = RdfXmlUtils.convertToRdfResource(place.getSameReferenceLinks());
    this.latitude = place.getLatitude();
    this.longitude = place.getLongitude();
    this.altitude = place.getAltitude();
    this.note = RdfXmlUtils.convertToXmlMultilingualString(place.getNote());
    this.hasPart = RdfXmlUtils.convertToRdfResource(place.getHasPart());
    this.isPartOf = RdfXmlUtils.convertToRdfResource(place.getIsPartOfArray());
    this.isNextInSequence = RdfXmlUtils.convertToRdfResource(place.getIsNextInSequence());
  }

  public XmlPlaceImpl() {
    // default constructor
  }

  @Override
  public Place toEntityModel() throws EntityCreationException {
    super.toEntityModel();

    entity.setLatitude(getLatitude());
    entity.setLongitude(getLongitude());
    entity.setAltitude(getAltitude());
    entity.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
    entity.setHasPart(RdfXmlUtils.toStringList(getHasPart()));
    entity.setIsPartOfArray(RdfXmlUtils.toStringList(getIsPartOf()));
    entity.setIsNextInSequence(RdfXmlUtils.toStringList(getIsNextInSequence()));

    return entity;
  }

  public Float getLatitude() {
    return latitude;
  }

  public Float getLongitude() {
    return longitude;
  }

  public Float getAltitude() {
    return altitude;
  }

  public List<LabelledResource> getNote() {
    return note;
  }

  public List<LabelledResource> getHasPart() {
    return hasPart;
  }

  public List<LabelledResource> getIsPartOf() {
    return isPartOf;
  }

  public List<LabelledResource> getIsNextInSequence() {
    return isNextInSequence;
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.Place;
  }

  @Override
  public List<LabelledResource> getSameReferenceLinks() {
    return this.sameAs;
  }

  @Override
  public void setSameReferenceLinks(List<LabelledResource> uris) {
    this.sameAs = uris;
  }
}
