package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
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
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ DEPICTION, PREF_LABEL, XML_ACRONYM, ALT_LABEL, XML_DESCRIPTION, XML_LOGO, XML_EUROPEANA_ROLE,
        XML_ORGANIZATION_DOMAIN, XML_GEOGRAPHIC_LEVEL, XML_COUNTRY, XML_HOMEPAGE, XML_PHONE, XML_MBOX, XML_HAS_ADDRESS,
        XML_ADDRESS, XML_IDENTIFIER, XML_SAME_AS, IS_AGGREGATED_BY })
public class XmlOrganizationImpl extends XmlBaseEntityImpl<Organization> {

        
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
	
	public Organization toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            entity.setAcronym(RdfXmlUtils.toLanguageMapList(getAcronym()));
            entity.setDescription(RdfXmlUtils.toLanguageMap(getDescription()));
            entity.setLogo(getLogo());
            entity.setEuropeanaRole(RdfXmlUtils.toLanguageMapList(getEuropeanaRole()));
            entity.setOrganizationDomain(RdfXmlUtils.toLanguageMapList(getOrganizationDomain()));
            entity.setGeographicLevel(RdfXmlUtils.toLanguageMap(getGeographicLevel()));
            entity.setCountry(getCountry());
            if(getHomepage() != null) {
                entity.setHomepage(getHomepage().getResource());
            }
            entity.setPhone(getPhone());
            entity.setMbox(getMbox());
            entity.setHasAddress(getHasAddress());
            entity.setIdentifier(getIdentifier());
            return entity;
        }
	
	
	public XmlOrganizationImpl() {
		// default constructor
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name = XML_ACRONYM)
	public List<LabelledResource> getAcronym() {
		return acronym;
	}
	
	@XmlElement(namespace = NAMESPACE_DC, name =  XML_DESCRIPTION)
	public List<LabelledResource> getDescription() {
		return description;
	}

	@XmlElement(namespace = NAMESPACE_FOAF,name =  XML_LOGO)
        public String getLogo() {
                return logo;            
        }


	//TODO: update implementation when test data is available 
	public EdmWebResource getLogoAsWebResource() {
	    	if(getLogo() == null)
	    	    return null;
		return new EdmWebResource(getLogo());
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name = XML_EUROPEANA_ROLE)
	public List<LabelledResource> getEuropeanaRole() {
		return europeanaRole;
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION_DOMAIN)
	public List<LabelledResource> getOrganizationDomain() {
		return organizationDomain;
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name = XML_GEOGRAPHIC_LEVEL)
	public List<LabelledResource> getGeographicLevel() {
		return geographicLevel;
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY)
	public String getCountry() {
	    	return country;
	}
	
	@XmlElement(namespace = NAMESPACE_FOAF, name = XML_HOMEPAGE)
	public LabelledResource getHomepage() {
	    	return homepage;		
	}
	
	@XmlElement(namespace = NAMESPACE_FOAF, name = XML_PHONE)
	public List<String> getPhone() {
	    	return phone;
	}
	
	@XmlElement(namespace = NAMESPACE_FOAF, name =  XML_MBOX)
	public List<String> getMbox() {
	    	return mbox;
	}
	
	@XmlElement(namespace = NAMESPACE_DC, name =  XML_IDENTIFIER)
	public String[] getIdentifier() {
	    	return identifier;
	}

	@XmlElement(namespace = NAMESPACE_VCARD, name =  XML_ADDRESS)
	public String getHasAddress() {
          return hasAddress;
	}
	
	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Organization;
	}
}
