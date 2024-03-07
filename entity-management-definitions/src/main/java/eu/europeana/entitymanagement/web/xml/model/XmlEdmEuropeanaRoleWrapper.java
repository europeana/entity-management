package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import eu.europeana.entitymanagement.definitions.model.Vocabulary;

/**
 * XML Serialization of the europeanaRole organization field.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlEdmEuropeanaRoleWrapper {

  @XmlElement(namespace = XmlConstants.NAMESPACE_SKOS, name = XmlConstants.XML_CONCEPT)
  private XmlConceptImpl concept;

  public XmlEdmEuropeanaRoleWrapper(Vocabulary vocab) {
    this.concept=new XmlConceptImpl();
    this.concept.setAbout(vocab.getId());
    this.concept.setPrefLabel(RdfXmlUtils.convertMapToXmlMultilingualString(vocab.getPrefLabel()));
    this.concept.setInScheme(RdfXmlUtils.convertToRdfResource(vocab.getInScheme()));
  }

  public XmlEdmEuropeanaRoleWrapper() {
  }

  public XmlConceptImpl getConcept() {
    return concept;
  }

}
