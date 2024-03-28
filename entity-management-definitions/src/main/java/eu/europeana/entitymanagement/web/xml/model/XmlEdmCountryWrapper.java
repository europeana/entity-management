package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import eu.europeana.entitymanagement.definitions.model.Place;

/**
 * XML Serialization of the country organization field.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlEdmCountryWrapper {
  
  @XmlElement(namespace = XmlConstants.NAMESPACE_EDM, name = XmlConstants.XML_PLACE)
  private XmlPlaceImpl place;
  
  public XmlEdmCountryWrapper(Place place) {
    this.place=new XmlPlaceImpl(place);
  }

  public XmlEdmCountryWrapper() {
  }

  public XmlPlaceImpl getPlace() {
    return place;
  }

}
