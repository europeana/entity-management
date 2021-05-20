package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Entity;
import javax.xml.bind.annotation.XmlRootElement;;

@XmlRootElement(name = XmlConstants.XML_ORE_AGGREGATION)
@JsonPropertyOrder({XmlConstants.XML_CREATED, XmlConstants.XML_MODIFIED, XmlConstants.XML_AGGREGATES})
public class XmlAggregationImpl {

    	@JsonIgnore
    	Entity entity;
    	@JsonIgnore
    	String id;
    	@JsonIgnore
    	private final String AGGREGATION_TAG = "#aggregation";
    	
    	public XmlAggregationImpl(Entity entity) {
    	    this.entity = entity;
    	    id = entity.getEntityId() + AGGREGATION_TAG;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.ABOUT)
	public String getAbout() {
		return id;
	}
    	
//	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_CREATED)
//	public RdfTypedElement getCreated() {
//	    	if(entity.getCreated() == null)
//	    	    return null;
//		return new RdfTypedElement(entity.getCreated());
//	}
	
//	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_MODIFIED)
//	public RdfTypedElement getModified() {
//	    	if(entity.getIsAggregatedBy() == null)
//	    	    return null;
//		return new RdfTypedElement(entity.getIsAggregatedBy());
//	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_AGGREGATES)
	public LabelledResource getAggregates() {
		return new LabelledResource(entity.getAbout());
	}
	
}
