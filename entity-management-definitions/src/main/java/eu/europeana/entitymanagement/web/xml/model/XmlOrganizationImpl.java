package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.springframework.util.CollectionUtils;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlOrganizationImpl extends XmlBaseEntityImpl<Organization> {

	@XmlElement(namespace = NAMESPACE_EDM, name = XML_ACRONYM)
	private List<LabelledResource> acronym = new ArrayList<>();

	@XmlElement(namespace = NAMESPACE_DC, name =  XML_DESCRIPTION)
	private List<LabelledResource> description = new ArrayList<>();

	@XmlElement(namespace = NAMESPACE_FOAF,name =  XML_LOGO)
	private String logo;

	@XmlElement(namespace = NAMESPACE_EDM, name = XML_EUROPEANA_ROLE)
	private List<LabelledResource> europeanaRole = new ArrayList<>();

	@XmlElement(namespace = NAMESPACE_EDM, name = XML_ORGANIZATION_DOMAIN)
	private List<LabelledResource> organizationDomain = new ArrayList<>();

	@XmlElement(namespace = NAMESPACE_EDM, name = XML_GEOGRAPHIC_LEVEL)
	private List<LabelledResource> geographicLevel = new ArrayList<>();

	@XmlElement(namespace = NAMESPACE_EDM, name = XML_COUNTRY)
	private String country;

	@XmlElement(namespace = NAMESPACE_FOAF, name = XML_HOMEPAGE)
	private LabelledResource homepage;

	@XmlElement(namespace = NAMESPACE_FOAF, name = XML_PHONE)
	private List<String> phone;

	@XmlElement(namespace = NAMESPACE_FOAF, name =  XML_MBOX)
	private List<String> mbox;

	@XmlElement(namespace = NAMESPACE_VCARD, name =  XML_HAS_ADDRESS)
	private XmlAddresses hasAddress;

	@XmlElement(namespace = NAMESPACE_DC, name =  XML_IDENTIFIER)
	private List<String> identifier;
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
	    	this.hasAddress = new XmlAddresses(List.of(new XmlAddressImpl(organization.getAddress())));
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
            if(hasAddress != null && !CollectionUtils.isEmpty(hasAddress.getVcardAddressesList())){
							entity.setAddress(hasAddress.getVcardAddressesList().get(0).toAddress());
						}
            entity.setIdentifier(getIdentifier());
            return entity;
        }
	
	
	public XmlOrganizationImpl() {
		// default constructor
	}
	
	public List<LabelledResource> getAcronym() {
		return acronym;
	}
	
	public List<LabelledResource> getDescription() {
		return description;
	}

        public String getLogo() {
                return logo;            
        }

	
	public List<LabelledResource> getEuropeanaRole() {
		return europeanaRole;
	}
	
	public List<LabelledResource> getOrganizationDomain() {
		return organizationDomain;
	}
	
	public List<LabelledResource> getGeographicLevel() {
		return geographicLevel;
	}
	
	public String getCountry() {
	    	return country;
	}
	
	public LabelledResource getHomepage() {
	    	return homepage;		
	}
	
	public List<String> getPhone() {
	    	return phone;
	}
	
	public List<String> getMbox() {
	    	return mbox;
	}
	
	public List<String> getIdentifier() {
	    	return identifier;
	}

	public XmlAddresses getHasAddress() {
          return hasAddress;
	}
	
	@Override
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Organization;
	}
}
