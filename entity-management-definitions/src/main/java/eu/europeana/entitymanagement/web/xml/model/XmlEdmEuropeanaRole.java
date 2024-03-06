package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IN_SCHEME;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RESOURCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_CONCEPT;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_CONCEPT)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      RESOURCE,
      PREF_LABEL,
      IN_SCHEME
    })
public class XmlEdmEuropeanaRole {

  @XmlAttribute(namespace = NAMESPACE_RDF, name = XmlConstants.RESOURCE)
  private String resource;
  
  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = PREF_LABEL)
  private List<LabelledResource> prefLabel = new ArrayList<>();
  
  @XmlElement(namespace = NAMESPACE_SKOS, name = IN_SCHEME)
  private List<LabelledResource> inScheme;

  public XmlEdmEuropeanaRole(Vocabulary vocab) {
    this.resource=vocab.getId();
    this.prefLabel= RdfXmlUtils.convertMapToXmlMultilingualString(vocab.getPrefLabel());
    this.inScheme = RdfXmlUtils.convertToRdfResource(vocab.getInScheme());
  }

  public XmlEdmEuropeanaRole() {
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public List<LabelledResource> getPrefLabel() {
    return this.prefLabel;
  }
  
  public void setPrefLabel(List<LabelledResource> prefLabel) {
    this.prefLabel=prefLabel;
  }
  
  public List<LabelledResource> getInScheme() {
    return this.inScheme;
  }
  
  public void setInScheme(List<LabelledResource> inScheme) {
    this.inScheme=inScheme;
  }
  
}
