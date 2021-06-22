package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_AGENT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_CONCEPT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ORGANIZATION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PLACE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_TIMESPAN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = NAMESPACE_RDF, name = RDF)
@XmlAccessorType(XmlAccessType.FIELD)

/*
 * Wrapper for XML responses
 */
public class RdfBaseWrapper {

  @XmlElements(value = {
      @XmlElement(name = XML_CONCEPT, namespace = NAMESPACE_SKOS, type = XmlConceptImpl.class),
      @XmlElement(name = XML_AGENT, namespace = NAMESPACE_EDM, type = XmlAgentImpl.class),
      @XmlElement(name = XML_PLACE, namespace = NAMESPACE_EDM, type = XmlPlaceImpl.class),
      @XmlElement(name = XML_ORGANIZATION, namespace = NAMESPACE_EDM, type = XmlOrganizationImpl.class),
      @XmlElement(name = XML_TIMESPAN, namespace = NAMESPACE_EDM, type = XmlTimespanImpl.class)})
  private XmlBaseEntityImpl<?> xmlEntity;

  public RdfBaseWrapper() {
    // default no-arg constructor for JAXB
  }

  public RdfBaseWrapper(XmlBaseEntityImpl<?> xmlEntity) {
    this.xmlEntity = xmlEntity;
  }

  public XmlBaseEntityImpl<?> getXmlEntity() {
    return xmlEntity;
  }
}
