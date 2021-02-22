package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class RdfResource {

    public RdfResource() {
	// default constructor required for Jackson deserialization
    }

    @XmlAttribute(namespace = XmlConstants.NAMESPACE_RDF, name = XmlConstants.RESOURCE)
    private String value;

    public RdfResource(String value) {
	this.value = value;
    }

    @JacksonXmlProperty(isAttribute = true, namespace = XmlConstants.RDF, localName = XmlConstants.RESOURCE)
    public String getValue() {
	return value;
    }
}
