package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ACRONYM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ADDRESS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_COUNTRY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_DESCRIPTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_EUROPEANA_ROLE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_GEOGRAPHIC_LEVEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_ADDRESS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HOMEPAGE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IDENTIFIER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_LOGO;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_MBOX;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ORGANIZATION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_ORGANIZATION_DOMAIN;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PHONE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;


@JacksonXmlRootElement(localName = XML_ORGANIZATION)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ DEPICTION, PREF_LABEL, XML_ACRONYM, ALT_LABEL, XML_DESCRIPTION, XML_LOGO, XML_EUROPEANA_ROLE,
        XML_ORGANIZATION_DOMAIN, XML_GEOGRAPHIC_LEVEL, XML_COUNTRY, XML_HOMEPAGE, XML_PHONE, XML_MBOX, XML_HAS_ADDRESS,
        XML_ADDRESS, XML_IDENTIFIER, XML_SAME_AS, IS_AGGREGATED_BY })
public class XmlOrganizationImpl extends XmlBaseEntityImpl {

        
        private List<LabelledResource> acronym = new ArrayList<>();
        private List<LabelledResource> description = new ArrayList<>();
        private String logo;
        private List<LabelledResource> europeanaRole = new ArrayList<>();
        private List<LabelledResource> organizationDomain = new ArrayList<>();
        private List<LabelledResource> geographicLevel = new ArrayList<>();
        private String country;
        private LabelledResource homepage;
        private List<String> phone;
        private List<String> mbox;
        private String hasAddress;
        private String[] identifier;
        //TODO: implement support for address when available in Metis
//        private Address address;
        
	public XmlOrganizationImpl(Organization organization) {
	    	super(organization);
	    	this.acronym = RdfXmlUtils.convertToXmlMultilingualString(organization.getAcronym());
	    	this.description = RdfXmlUtils.convertMapToXmlMultilingualString(organization.getDescription());
	    	this.logo = organization.getLogo();
	    	this.europeanaRole = RdfXmlUtils.convertToXmlMultilingualString(organization.getEuropeanaRole());
	    	this.organizationDomain = RdfXmlUtils.convertToXmlMultilingualString(organization.getOrganizationDomain());
	    	this.geographicLevel = RdfXmlUtils.convertMapToXmlMultilingualString(organization.getGeographicLevel());
	    	this.country = organization.getCountry();
	    	if(organization.getHomepage() != null) {
	    	    this.homepage = new LabelledResource(organization.getHomepage());                   
	    	}
                this.phone = organization.getPhone();
	    	this.mbox = organization.getMbox();
	    	this.hasAddress = organization.getHasAddress();
	    	this.identifier = organization.getIdentifier();
	}
	
	public Entity toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            Organization organization = (Organization) getEntity(); 
            organization.setAcronym(RdfXmlUtils.toLanguageMapList(getAcronym()));
            organization.setDescription(RdfXmlUtils.toLanguageMap(getDescription()));
            organization.setLogo(getLogo());
            organization.setEuropeanaRole(RdfXmlUtils.toLanguageMapList(getEuropeanaRole()));
            organization.setOrganizationDomain(RdfXmlUtils.toLanguageMapList(getOrganizationDomain()));
            organization.setGeographicLevel(RdfXmlUtils.toLanguageMap(getGeographicLevel()));
            organization.setCountry(getCountry());
            if(getHomepage() != null) {
                organization.setHomepage(getHomepage().getResource());
            }
            organization.setPhone(getPhone());
            organization.setMbox(getMbox());
            organization.setHasAddress(getHasAddress());
            organization.setIdentifier(getIdentifier());
            return organization;
        }
	
	
	public XmlOrganizationImpl() {
		// default constructor
	}
	
	@XmlElement(name = XML_ACRONYM)
	public List<LabelledResource> getAcronym() {
		return acronym;
	}
	
	@XmlElement(name =  XML_DESCRIPTION)
	public List<LabelledResource> getDescription() {
		return description;
	}

	@XmlElement(name =  XML_LOGO)
        public String getLogo() {
                return logo;            
        }

        
//	@XmlElement(name =  XML_LOGO)
	//TODO: update implementation when test data is available 
	public EdmWebResource getLogoAsWebResource() {
	    	if(getLogo() == null)
	    	    return null;
		return new EdmWebResource(getLogo());
	}
	
	@XmlElement(name = XML_EUROPEANA_ROLE)
	public List<LabelledResource> getEuropeanaRole() {
		return europeanaRole;
	}
	
	@XmlElement(name = XML_ORGANIZATION_DOMAIN)
	public List<LabelledResource> getOrganizationDomain() {
		return organizationDomain;
	}
	
	@XmlElement(name = XML_GEOGRAPHIC_LEVEL)
	public List<LabelledResource> getGeographicLevel() {
		return geographicLevel;
	}
	
	@XmlElement(name = XML_COUNTRY)
	public String getCountry() {
	    	return country;
	}
	
	@XmlElement(name = XML_HOMEPAGE)
	public LabelledResource getHomepage() {
	    	return homepage;		
	}
	
	@XmlElement(name = XML_PHONE)
	public List<String> getPhone() {
	    	return phone;
	}
	
	@XmlElement(name =  XML_MBOX)
	public List<String> getMbox() {
	    	return mbox;
	}
	
	@XmlElement(name =  XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return identifier;
	}

//	@JsonIgnore
//	private Organization getOrganization() {
//	    return (Organization)entity;
//	}
	
	/*
	 *  ElementWrapper works only on lists and maps.
	 *  This conversion is needed, because we have a two level object 
	 *  <vcard:hasAddress><vcard:Address>....</vcard:hasAddress></vcard:Address>
	 */
//	@JacksonXmlElementWrapper(localName = XML_HAS_ADDRESS)
//	@JacksonXmlProperty(localName = XML_ADDRESS)
//	public XmlAddressImpl[] getHasAddress() {
//	    	XmlAddressImpl[] tmp = {new XmlAddressImpl(getOrganization())};
//	    	return tmp;
//	}
	
	@XmlElement(name =  XML_ADDRESS)
	public String getHasAddress() {
          return hasAddress;
	}
	
	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Organization;
	}
}
