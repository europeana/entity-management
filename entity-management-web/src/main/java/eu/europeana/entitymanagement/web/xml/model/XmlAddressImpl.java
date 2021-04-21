package eu.europeana.entitymanagement.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.EntityUtils;

@JacksonXmlRootElement(localName = XmlConstants.XML_ADDRESS)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.XML_STREET_ADDRESS, XmlConstants.XML_POSTAL_CODE, XmlConstants.XML_POST_OFFICE_BOX,
    	XmlConstants.XML_LOCALITY, XmlConstants.XML_REGION, XmlConstants.XML_COUNTRY_NAME, XmlConstants.XML_HAS_GEO})
public class XmlAddressImpl {

    	@JsonIgnore
    	Organization organization;
    	
    	public XmlAddressImpl(Organization organization) {
    	    this.organization = organization;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.ABOUT)
	public String getAbout() {
		return organization.getHasAddress();
	}
    	
	@JacksonXmlProperty(localName = XmlConstants.XML_STREET_ADDRESS)
	public String getStreetAddress() {
	    	if(organization.getStreetAddress() == null || organization.getStreetAddress().isEmpty())
	    	    return null;
		return organization.getStreetAddress();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_POSTAL_CODE)
	public String getPostalCode() {
	    	if(organization.getPostalCode() == null || organization.getPostalCode().isEmpty())
	    	    return null;
		return organization.getPostalCode();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_POST_OFFICE_BOX)
	public String getPostBox() {
	    	if(organization.getPostBox() == null || organization.getPostBox().isEmpty())
	    	    return null;
		return organization.getPostBox();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_LOCALITY)
	public String getLocality() {
	    	if(organization.getLocality() == null || organization.getLocality().isEmpty())
	    	    return null;
		return organization.getLocality();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_REGION)
	public String getRegion() {
	    	if(organization.getRegion() == null || organization.getRegion().isEmpty())
	    	    return null;
		return organization.getRegion();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_COUNTRY_NAME)
	public String getCountryName() {
	    	if(organization.getCountryName() == null || organization.getCountryName().isEmpty())
	    	    return null;
		return organization.getCountryName();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_HAS_GEO)
	public LabelledResource getHasGeo() {
	    	if(organization.getHasGeo() == null || organization.getHasGeo().isEmpty())
	    	    return null;
		return new LabelledResource(EntityUtils.toGeoUri(organization.getHasGeo()));
	}
	
}
