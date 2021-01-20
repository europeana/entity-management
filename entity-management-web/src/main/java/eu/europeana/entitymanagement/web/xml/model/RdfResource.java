package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class RdfResource {

    public RdfResource() {
        // default constructor required for Jackson deserialization
    }

    private String value;
    
    public RdfResource(String value) {
	this.value = value;
    }
    
    @JacksonXmlProperty(isAttribute=true, namespace = XmlConstants.RDF, localName=XmlConstants.RESOURCE)
    public String getValue() {
        return value;
    }
}
