package eu.europeana.entitymanagement.utils.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Aggregation;;

@JacksonXmlRootElement(localName = XmlConstants.XML_ORE_AGGREGATION)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.XML_RDF_ABOUT, XmlConstants.XML_RDF_TYPE, XmlConstants.XML_EDM_RIGHTS, XmlConstants.XML_DC_SOURCE, XmlConstants.XML_DCTERMS_CREATED, XmlConstants.XML_DCTERMS_MODIFIED, XmlConstants.XML_ORE_AGGREGATES})
public class XmlAggregationImpl {

    	@JsonIgnore
    	Aggregation aggregation;
    	
    	public XmlAggregationImpl(Aggregation aggregation) {
    	    this.aggregation = aggregation;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.XML_RDF_ABOUT)
	public String getAbout() {
		return aggregation.getId();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_RDF_TYPE)
	public String getType() {
		return aggregation.getType();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_EDM_RIGHTS)
	public String getRights() {
		return aggregation.getRights();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_DC_SOURCE)
	public String getSource() {
		return aggregation.getSource();
	}

	@JacksonXmlProperty
	public int getPageRank() {
		return aggregation.getPageRank();
	}
	
	@JacksonXmlProperty
	public int getRecordCount() {
		return aggregation.getRecordCount();
	}
	
	@JacksonXmlProperty
	public int getScore() {
		return aggregation.getScore();
	}		
    	
	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_CREATED)
	public RdfTypedElement getCreated() {
	    	if(aggregation.getCreated() == null)
	    	    return null;
		return new RdfTypedElement(aggregation.getCreated());
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_MODIFIED)
	public RdfTypedElement getModified() {
	    	if(aggregation.getModified() == null)
	    	    return null;
		return new RdfTypedElement(aggregation.getModified());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_ORE_AGGREGATES)
	public List<String> getAggregates() {
	    	if(aggregation.getAggregates() == null || aggregation.getAggregates().size() == 0)
	    	    return null;
		return aggregation.getAggregates();
	}
	
}
