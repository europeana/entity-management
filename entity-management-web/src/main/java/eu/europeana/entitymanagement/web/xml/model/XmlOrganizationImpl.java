package eu.europeana.entitymanagement.web.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;;

@JacksonXmlRootElement(localName = XmlConstants.XML_ORGANIZATION)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.XML_ACRONYM, XmlConstants.ALT_LABEL,
    	XmlConstants.XML_DESCRIPTION, XmlConstants.XML_LOGO, XmlConstants.XML_EUROPEANA_ROLE, XmlConstants.XML_ORGANIZATION_DOMAIN,
    	XmlConstants.XML_GEOGRAPHIC_LEVEL, XmlConstants.XML_COUNTRY, XmlConstants.XML_HOMEPAGE, XmlConstants.XML_PHONE,
    	XmlConstants.XML_MBOX, XmlConstants.XML_HAS_ADDRESS, XmlConstants.XML_ADDRESS, XmlConstants.XML_IDENTIFIER, 
    	XmlConstants.XML_SAME_AS, XmlConstants.IS_AGGREGATED_BY})
public class XmlOrganizationImpl extends XmlBaseEntityImpl {

	public XmlOrganizationImpl(Organization organization) {
	    	super(organization);
	}
	
	public XmlOrganizationImpl() {
		// default constructor
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_ACRONYM)
	public List<LabelResource> getAcronym() {
		return RdfXmlUtils.convertToXmlMultilingualString(getOrganization().getAcronym());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DESCRIPTION)
	public List<LabelResource> getDescription() {
		return RdfXmlUtils.convertMapToXmlMultilingualString(getOrganization().getDescription());
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_LOGO)
	public EdmWebResource getLogo() {
	    	if(getOrganization().getLogo() == null)
	    	    return null;
		return new EdmWebResource(getOrganization().getLogo());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_EUROPEANA_ROLE)
	public List<LabelResource> getEuropeanaRole() {
		return RdfXmlUtils.convertToXmlMultilingualString(getOrganization().getEuropeanaRole());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_ORGANIZATION_DOMAIN)
	public List<LabelResource> getOrganizationDomain() {
		return RdfXmlUtils.convertToXmlMultilingualString(getOrganization().getOrganizationDomain());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_GEOGRAPHIC_LEVEL)
	public List<LabelResource> getGeographicLevel() {
		return RdfXmlUtils.convertMapToXmlMultilingualString(getOrganization().getGeographicLevel());
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_COUNTRY)
	public String getCountry() {
	    	if(getOrganization().getCountry() == null || getOrganization().getCountry().isEmpty())
	    	    return null;
		return getOrganization().getCountry();
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_HOMEPAGE)
	public LabelResource getHomepage() {
	    	if(getOrganization().getHomepage() == null)
	    	    return null;
		return new LabelResource(getOrganization().getHomepage());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_PHONE)
	public List<String> getPhone() {
	    	if(getOrganization().getPhone() == null || getOrganization().getPhone().size() == 0)
	    	    return null;
		return getOrganization().getPhone();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_MBOX)
	public List<String> getMbox() {
	    	if(getOrganization().getMbox() == null || getOrganization().getMbox().size() == 0)
	    	    return null;
		return getOrganization().getMbox();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return getOrganization().getIdentifier();
	}

	@JsonIgnore
	private Organization getOrganization() {
	    return (Organization)entity;
	}
	
	/*
	 *  ElementWrapper works only on lists and maps.
	 *  This conversion is needed, because we have a two level object 
	 *  <vcard:hasAddress><vcard:Address>....</vcard:hasAddress></vcard:Address>
	 */
	@JacksonXmlElementWrapper(localName = XmlConstants.XML_HAS_ADDRESS)
	@JacksonXmlProperty(localName = XmlConstants.XML_ADDRESS)
	public XmlAddressImpl[] getHasAddress() {
	    	XmlAddressImpl[] tmp = {new XmlAddressImpl(getOrganization())};
	    	return tmp;
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Organization;
	}
}
