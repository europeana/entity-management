package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

/**
 * This class defines base organization type of an entity.
 * @author GrafR
 *
 */

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class OrganizationImpl extends BaseEntity 
		implements Organization, eu.europeana.corelib.definitions.edm.entity.Organization {

	public OrganizationImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrganizationImpl(Organization copy) {
		super(copy);
		this.description = copy.getDescription()!=null ? new HashMap<>(copy.getDescription()) : null;
		this.acronym = copy.getAcronym()!=null ? new HashMap<>(copy.getAcronym()) : null;
		this.logo = copy.getLogo();
		this.homepage = copy.getHomepage();
		this.phone = copy.getPhone()!=null ? new ArrayList<>(copy.getPhone()) : null;
		this.mbox = copy.getMbox()!=null ? new ArrayList<>(copy.getMbox()) : null;
		this.europeanaRole = copy.getEuropeanaRole()!=null ? new HashMap<>(copy.getEuropeanaRole()) : null;
		this.organizationDomain = copy.getOrganizationDomain()!=null ? new HashMap<>(copy.getOrganizationDomain()) : null;
		this.geographicLevel = copy.getGeographicLevel()!=null ? new HashMap<>(copy.getGeographicLevel()) : null;
		this.country = copy.getCountry();
		this.hasAddress = copy.getHasAddress();
		this.streetAddress = copy.getStreetAddress();
		this.locality = copy.getLocality();
		this.region = copy.getRegion();
		this.postalCode = copy.getPostalCode();
		this.countryName = copy.getCountryName();
		this.postBox = copy.getPostBox();
		this.hasGeo = copy.getHasGeo();
	}

	private Map<String, String> description;
	private Map<String, List<String>> acronym;	
	private String logo;
	private String homepage;
	private List<String> phone;
	private List<String> mbox;
	private Map<String, List<String>> europeanaRole;	
	private Map<String, List<String>> organizationDomain;
	private Map<String, String> geographicLevel;
	private String country;
	private Map<String, String> countryMap;
	private Map<String, List<String>> tmpIdentifier;
	
	//address fields
	private String hasAddress;
	private String streetAddress;
	private String locality;
	private String region;
	private String postalCode;
	private String countryName;
	private String postBox;
	private String hasGeo;
	private Address address;
	
	@Override
	@JsonGetter(WebEntityFields.DESCRIPTION)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_DESCRIPTION)
	public Map<String, String> getDescription() {
		return description;
	}

	@Override
	@JsonSetter(WebEntityFields.DESCRIPTION)
	public void setDescription(Map<String, String> dcDescription) {
	    	this.description = dcDescription;
	}
	
	@Override
	@JsonGetter(WebEntityFields.ACRONYM)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ACRONYM)
	public Map<String, List<String>> getAcronym() {
		return acronym;
	}

	@Override
	@JsonSetter(WebEntityFields.ACRONYM)
	public void setAcronym(Map<String, List<String>> acronym) {
	    	this.acronym = acronym;
	}
	
	@Override
	@JsonGetter(WebEntityFields.EUROPEANA_ROLE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_EUROPEANA_ROLE)
	public Map<String, List<String>> getEuropeanaRole() {
		return europeanaRole;
	}

	@Override
	@JsonSetter(WebEntityFields.EUROPEANA_ROLE)
	public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
	    	this.europeanaRole = europeanaRole;
	}
	
	@Override
	@JsonGetter(WebEntityFields.FOAF_PHONE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_PHONE)
	public List<String> getPhone() {
		return phone;
	}

	@Override
	@JsonSetter(WebEntityFields.FOAF_PHONE)
	public void setPhone(List<String> phone) {
		this.phone = phone;
	}

	@Override
	@JsonGetter(WebEntityFields.FOAF_MBOX)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_MBOX)
	public List<String> getMbox() {
		return mbox;
	}

	@Override
	@JsonSetter(WebEntityFields.FOAF_MBOX)
	public void setMbox(List<String> mbox) {
		this.mbox = mbox;
	}

	@Override
	@JacksonXmlElementWrapper(localName = XmlFields.XML_VCARD_HAS_ADDRESS)
	@JacksonXmlProperty(localName = XmlFields.XML_VCARD_ADDRESS)
	@JsonGetter(WebEntityFields.ADDRESS_TYPE)
	public String getHasAddress() {
		return hasAddress;
	}

	@Override
	@JsonSetter(WebEntityFields.ADDRESS_TYPE)
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
	@JsonGetter(WebEntityFields.COUNTRY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_COUNTRY)
	public String getCountry() {
		return country;
	}

	@Override
	@JsonSetter(WebEntityFields.COUNTRY)
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
	@JsonGetter(WebEntityFields.GEOGRAPHIC_LEVEL)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_GEOGRAPHIC_LEVEL)
	public Map<String, String> getGeographicLevel() {
		return geographicLevel;
	}

	@Override
	@Deprecated
	@JsonSetter(WebEntityFields.GEOGRAPHIC_LEVEL)
	public void setGeographicLevel(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}

	@Override
	@JsonGetter(WebEntityFields.ORGANIZATION_DOMAIN)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ORGANIZATION_DOMAIN)
	public Map<String, List<String>> getOrganizationDomain() {
		return organizationDomain;
	}

	@Override
	@JsonSetter(WebEntityFields.ORGANIZATION_DOMAIN)
	public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
		this.organizationDomain = organizationDomain;
	}

	@Override
	@JsonGetter(WebEntityFields.FOAF_HOMEPAGE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_HOMEPAGE)
	public String getHomepage() {
		return homepage;
	}

	@Override
	@JsonSetter(WebEntityFields.FOAF_HOMEPAGE)
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	@Override
	@JsonGetter(WebEntityFields.FOAF_LOGO)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_LOGO)
	public String getLogo() {
		return logo;
	}

	@Override
	@JsonSetter(WebEntityFields.FOAF_LOGO)
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
	@JsonIgnore
	public void setDcIdentifier(Map<String, List<String>> dcIdentifier) {
	    // Not used
	    
	}

	@Override
	@JsonIgnore
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
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	@Override
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

	@Override
	@JsonIgnore
	public eu.europeana.corelib.definitions.edm.entity.Address getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public void setAddress(eu.europeana.corelib.definitions.edm.entity.Address arg0) {
		// TODO Auto-generated method stub
		
	}

}
