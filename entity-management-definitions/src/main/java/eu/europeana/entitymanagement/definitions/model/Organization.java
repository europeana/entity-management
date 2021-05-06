package eu.europeana.entitymanagement.definitions.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

/**
 * This class defines base organization type of an entity.
 */

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, PREF_LABEL, ACRONYM, ALT_LABEL, DESCRIPTION, FOAF_LOGO, EUROPEANA_ROLE,
		ORGANIZATION_DOMAIN, GEOGRAPHIC_LEVEL, COUNTRY, FOAF_HOMEPAGE, FOAF_PHONE, FOAF_MBOX, HAS_ADDRESS, IDENTIFIER, SAME_AS})
public class Organization extends Entity {

	public Organization() {
		super();
		// TODO Auto-generated constructor stub
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

	
	@JsonGetter(DESCRIPTION)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_DESCRIPTION)
	public Map<String, String> getDescription() {
		return description;
	}

	
	@JsonSetter(DESCRIPTION)
	public void setDescription(Map<String, String> dcDescription) {
		this.description = dcDescription;
	}

	
	@JsonGetter(ACRONYM)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ACRONYM)
	public Map<String, List<String>> getAcronym() {
		return acronym;
	}

	
	@JsonSetter(ACRONYM)
	public void setAcronym(Map<String, List<String>> acronym) {
		this.acronym = acronym;
	}

	
	@JsonGetter(EUROPEANA_ROLE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_EUROPEANA_ROLE)
	public Map<String, List<String>> getEuropeanaRole() {
		return europeanaRole;
	}

	
	@JsonSetter(EUROPEANA_ROLE)
	public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
		this.europeanaRole = europeanaRole;
	}

	
	@JsonGetter(FOAF_PHONE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_PHONE)
	public List<String> getPhone() {
		return phone;
	}

	
	@JsonSetter(FOAF_PHONE)
	public void setPhone(List<String> phone) {
		this.phone = phone;
	}

	
	@JsonGetter(FOAF_MBOX)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_MBOX)
	public List<String> getMbox() {
		return mbox;
	}

	
	@JsonSetter(FOAF_MBOX)
	public void setMbox(List<String> mbox) {
		this.mbox = mbox;
	}

	
	@JacksonXmlElementWrapper(localName = XmlFields.XML_VCARD_HAS_ADDRESS)
	@JacksonXmlProperty(localName = XmlFields.XML_VCARD_ADDRESS)
	@JsonGetter(ADDRESS_TYPE)
	public String getHasAddress() {
		return hasAddress;
	}

	
	@JsonSetter(ADDRESS_TYPE)
	public void setHasAddress(String hasAddress) {
		this.hasAddress = hasAddress;
	}

	
	public String getLocality() {
		return locality;
	}

	
	public void setLocality(String locality) {
		this.locality = locality;
	}

	
	public String getRegion() {
		return region;
	}

	
	public void setRegion(String region) {
		this.region = region;
	}

	
	public String getCountryName() {
		return countryName;
	}

	
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	
	public String getPostalCode() {
		return postalCode;
	}

	
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	
	public String getPostBox() {
		return postBox;
	}

	
	public void setPostBox(String postBox) {
		this.postBox = postBox;
	}

	
	@JsonGetter(COUNTRY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_COUNTRY)
	public String getCountry() {
		return country;
	}

	
	@JsonSetter(COUNTRY)
	public void setCountry(String country) {
		this.country = country;
	}

	
	public String getStreetAddress() {
		return streetAddress;
	}

	
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	
	@JsonGetter(GEOGRAPHIC_LEVEL)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_GEOGRAPHIC_LEVEL)
	public Map<String, String> getGeographicLevel() {
		return geographicLevel;
	}

	
	@Deprecated
	@JsonSetter(GEOGRAPHIC_LEVEL)
	public void setGeographicLevel(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}

	
	@JsonGetter(ORGANIZATION_DOMAIN)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_ORGANIZATION_DOMAIN)
	public Map<String, List<String>> getOrganizationDomain() {
		return organizationDomain;
	}

	
	@JsonSetter(ORGANIZATION_DOMAIN)
	public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
		this.organizationDomain = organizationDomain;
	}

	
	@JsonGetter(FOAF_HOMEPAGE)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_HOMEPAGE)
	public String getHomepage() {
		return homepage;
	}

	
	@JsonSetter(FOAF_HOMEPAGE)
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	
	@JsonGetter(FOAF_LOGO)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_LOGO)
	public String getLogo() {
		return logo;
	}

	
	@JsonSetter(FOAF_LOGO)
	public void setLogo(String logo) {
		this.logo = logo;
	}

	
	public String getHasGeo() {
		return hasGeo;
	}

	
	public void setHasGeo(String hasGeo) {
		this.hasGeo = hasGeo;
	}

	public Map<String, String> getGeographicLevelStringMap() {
		return geographicLevel;
	}

	public void setGeographicLevelStringMap(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}



	
	public Map<String, String> getEdmCountry() {
		if(countryMap == null) {
			countryMap = new HashMap<>();
			countryMap.put(TMP_KEY, country);
		}
		return countryMap;
	}

	
	public String getType() {
		return EntityTypes.Organization.getEntityType();
	}


	
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}
}
