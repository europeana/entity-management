package eu.europeana.entitymanagement.utils.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;;

@JacksonXmlRootElement
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.XML_RDF_ABOUT, XmlConstants.XML_ORE_IS_AGGREGATED_BY})
public class XmlEntityRecordImpl {

    	@JsonIgnore
    	EntityRecord entityRecord;
    	
    	/*
    	 * this field is used when the referenced web resources are serialized in a separate xml tag outside of the main element
    	 */
    	@JsonIgnore
    	XmlBaseEntityImpl xmlBaseEntity;
    	
    	/*
    	 * this field is used when the referenced web resources are serialized in a separate xml tag outside of the main element
    	 */
    	@JsonIgnore
    	List<XmlEntityProxyImpl> xmlEntityProxies;

    	
    	@JsonIgnore
    	public XmlBaseEntityImpl getXmlBaseEntity() {
			return xmlBaseEntity;
		}
    	
    	@JsonIgnore
    	public List<XmlEntityProxyImpl> getXmlEntityProxies() {
    		return xmlEntityProxies;
    	}

		public XmlEntityRecordImpl(EntityRecord entityRecord) {
    	    this.entityRecord = entityRecord; 
    	    this.xmlEntityProxies = new ArrayList<>();
    	    
    	}
    	
    @JacksonXmlProperty(isAttribute= true, localName = XmlConstants.XML_RDF_ABOUT)
    public String getAbout() {
    	return entityRecord.getEntityId();
    }   
    	
    @JacksonXmlProperty
    public XmlBaseEntityImpl getEntity() {
       	if(entityRecord.getEntity() == null)
       	    return null;
       	xmlBaseEntity = new XmlBaseEntityImpl(entityRecord.getEntity());
       	return xmlBaseEntity;
    }
    
    @JacksonXmlProperty(localName = XmlConstants.XML_ORE_IS_AGGREGATED_BY)
    public XmlAggregationImpl createXmlAggregation() {
	   	if(entityRecord.getIsAggregatedBy() == null)
	   	    return null;
		return new XmlAggregationImpl(entityRecord.getIsAggregatedBy());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	public List<XmlEntityProxyImpl> getProxies(){
		if(entityRecord.getProxies() == null)
	   	    return null;
		for (EntityProxy proxy : entityRecord.getProxies()) {
			xmlEntityProxies.add(new XmlEntityProxyImpl(proxy));
		}
		return xmlEntityProxies;

	}



	
}
