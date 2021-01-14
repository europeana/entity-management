package eu.europeana.entitymanagement.utils.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.EntityProxy;;

@JacksonXmlRootElement(localName = XmlConstants.XML_ORE_PROXY)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.XML_RDF_ABOUT, XmlConstants.XML_RDF_TYPE, XmlConstants.XML_ORE_PROXY_FOR, XmlConstants.XML_ORE_PROXY_IN})
public class XmlEntityProxyImpl {

    	@JsonIgnore
    	EntityProxy proxy;
    	
    	/*
    	 * this field is used when the referenced web resources are serialized in a separate xml tag outside of the main element
    	 */
    	@JsonIgnore
    	XmlBaseEntityImpl xmlBaseEntity;
    	
    	@JsonIgnore
    	public XmlBaseEntityImpl getXmlBaseEntity() {
			return xmlBaseEntity;
		}

    	
    	public XmlEntityProxyImpl(EntityProxy proxy) {
    	    this.proxy = proxy;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.XML_RDF_ABOUT)
	public String getAbout() {
		return proxy.getProxyId();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_RDF_TYPE)
	public String getType() {
		return proxy.getType();
	}
	
	@JacksonXmlProperty
    public XmlBaseEntityImpl getEntity() {
	   	if(proxy.getEntity() == null)
	   	    return null;
	   	xmlBaseEntity = new XmlBaseEntityImpl(proxy.getEntity());
	   	return xmlBaseEntity;
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_ORE_PROXY_FOR)
	public String getProxyFor() {
		return proxy.getProxyFor();
	}
	
    @JacksonXmlProperty(localName = XmlConstants.XML_ORE_PROXY_IN)
    public XmlAggregationImpl createXmlAggregation() {
	   	if(proxy.getProxyIn() == null)
	   	    return null;
		return new XmlAggregationImpl(proxy.getProxyIn());
	}
}
