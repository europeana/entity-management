package eu.europeana.entitymanagement.definitions.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

import eu.europeana.entitymanagement.vocabulary.EntityTypes;

/**
 * This class defines base organization type of an entity.
 */

@JsonFilter("solrSuggesterFilter")
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, PREF_LABEL, ACRONYM, ALT_LABEL, DESCRIPTION, FOAF_LOGO, EUROPEANA_ROLE,
		ORGANIZATION_DOMAIN, GEOGRAPHIC_LEVEL, COUNTRY, FOAF_HOMEPAGE, FOAF_PHONE, FOAF_MBOX, HAS_ADDRESS, IDENTIFIER, SAME_AS})
public class Organization extends Entity {

	public Organization() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Organization(Organization copy) {
		super(copy);
		if(copy.getDescription()!=null) this.description = new HashMap<>(copy.getDescription());
		if(copy.getAcronym()!=null) this.acronym = new HashMap<>(copy.getAcronym());
	    this.logo = copy.getLogo();
		this.homepage = copy.getHomepage();
		if(copy.getPhone()!=null) this.phone = new ArrayList<>(copy.getPhone());
		if(copy.getMbox()!=null) this.mbox = new ArrayList<>(copy.getMbox());
		if(copy.getEuropeanaRole()!=null) this.europeanaRole = new HashMap<>(copy.getEuropeanaRole());
		if(copy.getOrganizationDomain()!=null) this.organizationDomain = new HashMap<>(copy.getOrganizationDomain());
		if(copy.getGeographicLevel()!=null) this.geographicLevel = new HashMap<>(copy.getGeographicLevel());
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
	public Map<String, String> getDescription() {
		return description;
	}

	
	@JsonSetter(DESCRIPTION)
	public void setDescription(Map<String, String> dcDescription) {
		this.description = dcDescription;
	}

	
	@JsonGetter(ACRONYM)
	public Map<String, List<String>> getAcronym() {
		return acronym;
	}

	
	@JsonSetter(ACRONYM)
	public void setAcronym(Map<String, List<String>> acronym) {
		this.acronym = acronym;
	}

	
	@JsonGetter(EUROPEANA_ROLE)
	public Map<String, List<String>> getEuropeanaRole() {
		return europeanaRole;
	}

	
	@JsonSetter(EUROPEANA_ROLE)
	public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
		this.europeanaRole = europeanaRole;
	}

	
	@JsonGetter(FOAF_PHONE)
	public List<String> getPhone() {
		return phone;
	}

	
	@JsonSetter(FOAF_PHONE)
	public void setPhone(List<String> phone) {
		this.phone = phone;
	}

	
	@JsonGetter(FOAF_MBOX)
	public List<String> getMbox() {
		return mbox;
	}

	
	@JsonSetter(FOAF_MBOX)
	public void setMbox(List<String> mbox) {
		this.mbox = mbox;
	}

	
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
	public Map<String, String> getGeographicLevel() {
		return geographicLevel;
	}

	
	@Deprecated
	@JsonSetter(GEOGRAPHIC_LEVEL)
	public void setGeographicLevel(Map<String, String> geographicLevel) {
		this.geographicLevel = geographicLevel;
	}

	
	@JsonGetter(ORGANIZATION_DOMAIN)
	public Map<String, List<String>> getOrganizationDomain() {
		return organizationDomain;
	}

	
	@JsonSetter(ORGANIZATION_DOMAIN)
	public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
		this.organizationDomain = organizationDomain;
	}

	
	@JsonGetter(FOAF_HOMEPAGE)
	public String getHomepage() {
		return homepage;
	}

	
	@JsonSetter(FOAF_HOMEPAGE)
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	
	@JsonGetter(FOAF_LOGO)
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
