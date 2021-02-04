package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;

/**
 * This class defines base organization type of an entity.
 * @author GrafR
 *
 */

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class BaseOrganization extends BaseEntity 
		implements Organization, eu.europeana.corelib.definitions.edm.entity.Organization {

	protected Map<String, String> description;
	protected Map<String, List<String>> acronym;	
	protected String logo;
	protected String homepage;
	protected List<String> phone;
	protected List<String> mbox;
	protected Map<String, List<String>> europeanaRole;	
	protected Map<String, List<String>> organizationDomain;
	protected Map<String, String> geographicLevel;
	protected String country;
	protected Map<String, String> countryMap;
	protected Map<String, List<String>> tmpIdentifier;
	
	//address fields
	protected String hasAddress;
	protected String streetAddress;
	protected String locality;
	protected String region;
	protected String postalCode;
	protected String countryName;
	protected String postBox;
	protected String hasGeo;
	protected Address address;
	
	@Override
	@JsonProperty(WebEntityFields.DESCRIPTION)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_DESCRIPTION)
	public Map<String, String> getDescription() {
		return description;
	}

	@Override
	public void setDescription(Map<String, String> dcDescription) {
	    	this.description = dcDescription;
	}
	
	@Override
	@JsonProperty(WebEntityFields.ACRONYM)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ACRONYM)
	public Map<String, List<String>> getAcronym() {
		return acronym;
	}

	@Override
	public void setAcronym(Map<String, List<String>> acronym) {
	    	this.acronym = acronym;
	}
	
	@Override
	@JsonProperty(WebEntityFields.EUROPEANA_ROLE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_EUROPEANA_ROLE)
	public Map<String, List<String>> getEuropeanaRole() {
		return europeanaRole;
	}

	@Override
	public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
	    	this.europeanaRole = europeanaRole;
	}
	
	@Override
	@JsonProperty(WebEntityFields.FOAF_PHONE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_PHONE)
	public List<String> getPhone() {
		return phone;
	}

	@Override
	public void setPhone(List<String> phone) {
		this.phone = phone;
	}

	@Override
	@JsonProperty(WebEntityFields.FOAF_MBOX)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_MBOX)
	public List<String> getMbox() {
		return mbox;
	}

	@Override
	public void setMbox(List<String> mbox) {
		this.mbox = mbox;
	}

	@Override
	@JacksonXmlElementWrapper(localName = XmlFields.XML_VCARD_HAS_ADDRESS)
	@JacksonXmlProperty(localName = XmlFields.XML_VCARD_ADDRESS)
	@JsonProperty(WebEntityFields.ADDRESS_TYPE)
	public String getHasAddress() {
		return hasAddress;
	}

	@Override
	public void setHasAddress(String hasAddress) {
		this.hasAddress = hasAddress;
	}

	@Override
	public String getLocality() {
		return locality;
	}

	@Override
	public void setLocality(String locality) {
		this.locality = locality;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String getCountryName() {
		return countryName;
	}

	@Override
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	@Override
	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	@Override
	public String getPostBox() {
		return postBox;
	}

	@Override
	public void setPostBox(String postBox) {
		this.postBox = postBox;
	}

	@Override
	@JsonProperty(WebEntityFields.COUNTRY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_COUNTRY)
	public String getCountry() {
		return country;
	}

	@Override
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String getStreetAddress() {
		return streetAddress;
	}

	@Override
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	@Override
	@JsonProperty(WebEntityFields.GEOGRAPHIC_LEVEL)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_GEOGRAPHIC_LEVEL)
	public Map<String, String> getGeographicLevel() {
		return geographicLevel;
	}

	@Override
	@Deprecated
	public void setGeographicLevel(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}

	@Override
	@JsonProperty(WebEntityFields.ORGANIZATION_DOMAIN)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ORGANIZATION_DOMAIN)
	public Map<String, List<String>> getOrganizationDomain() {
		return organizationDomain;
	}

	@Override
	public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
		this.organizationDomain = organizationDomain;
	}

	@Override
	@JsonProperty(WebEntityFields.FOAF_HOMEPAGE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_HOMEPAGE)
	public String getHomepage() {
		return homepage;
	}

	@Override
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	@Override
	@JsonProperty(WebEntityFields.FOAF_LOGO)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_LOGO)
	public String getLogo() {
		return logo;
	}

	@Override
	public void setLogo(String logo) {
		this.logo = logo;
	}

	@Override
	public String getHasGeo() {
		return hasGeo;
	}

	@Override
	public void setHasGeo(String hasGeo) {
		this.hasGeo = hasGeo;
	}

	public Map<String, String> getGeographicLevelStringMap() {
		return geographicLevel;
	}

	public void setGeographicLevelStringMap(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}
	
	@Override
	public Map<String, List<String>> getEdmAcronym() {
		return getAcronym();
	}

	@Override
	@Deprecated
	public void setEdmAcronym(Map<String, List<String>> edmAcronym) {
		// Not used
	}

	@Override
	public Map<String, String> getEdmOrganizationScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setEdmOrganizationScope(Map<String, String> edmOrganizationScope) {
		// Not used
		
	}

	@Override
	public Map<String, String> getEdmOrganizationDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setEdmOrganizationDomain(Map<String, String> edmOrganizationDomain) {
		// Not used
		
	}

	@Override
	public Map<String, String> getEdmOrganizationSector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setEdmOrganizationSector(Map<String, String> edmOrganizationSector) {
		// Not used
		
	}

	@Override
	public Map<String, String> getEdmGeographicLevel() {
		return getGeographicLevel();
	}

	@Override
	@Deprecated
	public void setEdmGeorgraphicLevel(Map<String, String> edmGeographicLevel) {
		// Not used
		
	}

	@Override
	public Map<String, String> getEdmCountry() {
		if(countryMap == null) {
		    countryMap = new HashMap<>();
		    countryMap.put(TMP_KEY, country);
		}
		return countryMap;
	}

	@Override
	@Deprecated
	public void setEdmCountry(Map<String, String> edmCountry) {
		// Not used
	}

	@Override
	@Deprecated
	public void setFoafMbox(List<String> foafMbox) {
		// Not used
		
	}

	@Override
	public List<String> getFoafMbox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setFoafPhone(List<String> foafPhone) {
		// Not used
	}

	@Override
	public List<String> getFoafPhone() {
		return getPhone();
	}

	@Override
	@Deprecated
	public void setDcDescription(Map<String, String> dcDescription) {
		// Not used
	}

	@Override
	public Map<String, String> getDcDescription() {
		return getDescription();
	}

	@Override
	@Deprecated
	public void setRdfType(String rdfType) {
		// Not used
		
	}

	@Override
	public String getRdfType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setFoafLogo(String foafLogo) {
		// Not used
	}

	@Override
	public String getFoafLogo() {
		return getLogo();
	}

	@Override
	@Deprecated
	public void setFoafHomepage(String foafHomePage) {
		// Not used
	}

	@Override
	public String getFoafHomepage() {
		return getHomepage();
	}

	@Override
	@Deprecated
	public void setEdmEuropeanaRole(Map<String, List<String>> edmEuropeanaRole) {
		// Not used
		
	}

	@Override
	public Map<String, List<String>> getEdmEuropeanaRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setAddress(Address address) {
	    	// Not used
	}

	@Override
	public Address getAddress() {
	    	if(address == null) {
	    	    address = new BaseAddress();
	    	    address.setAbout(hasAddress);
	    	    address.setVcardStreetAddress(streetAddress);
	    	    address.setVcardPostalCode(postalCode);
	    	    address.setVcardPostOfficeBox(postBox);
	    	    address.setVcardLocality(locality);
	    	    address.setVcardCountryName(countryName);
	    	}
		return this.address;
	}

	@Override
	@Deprecated
	public void setDcIdentifier(Map<String, List<String>> dcIdentifier) {
	    // Not used
	    
	}

	@Override
	
	@JsonProperty(WebEntityFields.IDENTIFIER)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_IDENTIFIER)
	public Map<String, List<String>> getDcIdentifier() {
		//if not available
		if (getIdentifier() == null)
			return null;
		//if not transformed
		if (tmpIdentifier == null) 
			tmpIdentifier = fillTmpMap(Arrays.asList(getIdentifier()));

		return tmpIdentifier;
	}

	@Override
	public String getInternalType() {
		return "Organization";
	}
}
