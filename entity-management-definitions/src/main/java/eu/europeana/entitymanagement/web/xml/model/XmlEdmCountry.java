package eu.europeana.entitymanagement.web.xml.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import eu.europeana.entitymanagement.definitions.model.Place;

/**
 * XML Serialization of the country organization field.
 */
@XmlRootElement(namespace = XmlConstants.NAMESPACE_EDM, name = XmlConstants.XML_PLACE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
        XmlConstants.RESOURCE,
        XmlConstants.PREF_LABEL
    })
public class XmlEdmCountry {

  @XmlAttribute(namespace = XmlConstants.NAMESPACE_RDF, name = XmlConstants.RESOURCE)
  private String resource;
  
  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.PREF_LABEL)
  private List<LabelledResource> prefLabel = new ArrayList<>();
  
  public XmlEdmCountry(Place place) {
    this.resource=place.getAbout();
    this.prefLabel= RdfXmlUtils.convertMapToXmlMultilingualString(place.getPrefLabel());
  }

  public XmlEdmCountry() {
  }

  public List<LabelledResource> getPrefLabel() {
    return this.prefLabel;
  }
  
  public void setPrefLabel(List<LabelledResource> prefLabel) {
    this.prefLabel=prefLabel;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }
}
