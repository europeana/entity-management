package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RESOURCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PLACE;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import eu.europeana.entitymanagement.definitions.model.Place;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_PLACE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      RESOURCE,
      PREF_LABEL
    })
public class XmlEdmCountry {

  @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.RESOURCE)
  private String resource;
  
  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = PREF_LABEL)
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
