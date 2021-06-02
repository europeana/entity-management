package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.ConceptSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields;

public class SolrOrganization extends Organization {

	private String payload;
	
	public SolrOrganization() {
		super();
	}

	public SolrOrganization(Organization organization) {
		super();
		this.setType(organization.getType());
		this.setEntityId(organization.getEntityId());
		this.setDepiction(organization.getDepiction());
		this.setNote(organization.getNote());
		this.setPrefLabelStringMap(organization.getPrefLabelStringMap());
		this.setAltLabel(organization.getAltLabel());
		this.setHiddenLabel(organization.getHiddenLabel());
		this.setIdentifier(organization.getIdentifier());
		this.setSameAs(organization.getSameAs());
		this.setIsRelatedTo(organization.getIsRelatedTo());
		this.setHasPart(organization.getHasPart());
		this.setIsPartOfArray(organization.getIsPartOfArray());
		
		this.setDescription(organization.getDescription());
		this.setAcronym(organization.getAcronym());
		this.setLogo(organization.getLogo());
		this.setHomepage(organization.getHomepage());
		this.setPhone(organization.getPhone());
		this.setMbox(organization.getMbox());
		this.setEuropeanaRole(organization.getEuropeanaRole());
		this.setOrganizationDomain(organization.getOrganizationDomain());
		this.setGeographicLevel(organization.getGeographicLevel());
		this.setCountry(organization.getCountry());
		this.setHasAddress(organization.getHasAddress());
		this.setStreetAddress(organization.getStreetAddress());
		this.setLocality(organization.getLocality());
		this.setRegion(organization.getRegion());
		this.setPostalCode(organization.getPostalCode());
		this.setCountryName(organization.getCountryName());
		this.setPostBox(organization.getPostBox());
		this.setHasGeo(organization.getHasGeo());
	}

	@Override
	@Field(OrganizationSolrFields.DC_DESCRIPTION_ALL)
	public void setDescription(Map<String, String> dcDescription) {
	    Map<String, String> normalizedDescription = SolrUtils.normalizeStringMapByAddingPrefix(
		    OrganizationSolrFields.DC_DESCRIPTION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, dcDescription);
	    super.setDescription(normalizedDescription);
	}
	
	
	@Override
	@Field(OrganizationSolrFields.EDM_ACRONYM_ALL)
	public void setAcronym(Map<String, List<String>>  acronym) {
		Map<String, List<String>> normalizedAcronym = SolrUtils.normalizeStringListMapByAddingPrefix(
				OrganizationSolrFields.EDM_ACRONYM + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, acronym);
		super.setAcronym(normalizedAcronym);
	}

	@Override
	@Field(OrganizationSolrFields.PREF_LABEL_ALL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMapByAddingPrefix(
				OrganizationSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel);
		super.setPrefLabelStringMap(normalizedPrefLabel);
	}

	@Override
	@Field(OrganizationSolrFields.ALT_LABEL_ALL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				OrganizationSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel);
		super.setAltLabel(normalizedAltLabel);
	}

	@Override
	@Field(OrganizationSolrFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				ConceptSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, hiddenLabel);
		super.setHiddenLabel(normalizedHiddenLabel);
	}
	
	@Override
	@Field(OrganizationSolrFields.NOTE_ALL)
	public void setNote(Map<String, List<String>> note) {
		Map<String, List<String>>  normalizedNote = SolrUtils.normalizeStringListMapByAddingPrefix(
				ConceptSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note);
		super.setNote(normalizedNote);
	}
	
	@Override
	@Field(OrganizationSolrFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		super.setIdentifier(identifier);
	}
	
	@Override
	@Field(OrganizationSolrFields.ID)
	public void setEntityId(String entityId) {
		super.setEntityId(entityId);
	}
	
	@Override
	@Field(OrganizationSolrFields.TYPE)
	public void setType(String type) {
		super.setType(type);
	}	
	
	@Override
	@Field(OrganizationSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}
	
	@Override
	@Field(OrganizationSolrFields.HAS_PART)
	public void setHasPart(List<String> hasPart) {
		super.setHasPart(hasPart);
	}

	@Override
	@Field(OrganizationSolrFields.IS_PART_OF)
	public void setIsPartOfArray(List<String> isPartOf) {
		super.setIsPartOfArray(isPartOf);
	}
	
	@Override
	@Field(OrganizationSolrFields.GEOGRAPHIC_LEVEL_ALL)
	public void setGeographicLevel(Map<String, String> geographicLevel) {
		Map<String, String> normalizedGeographicLevel = SolrUtils.normalizeStringMapByAddingPrefix(
				OrganizationSolrFields.GEOGRAPHIC_LEVEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, geographicLevel);
		super.setGeographicLevelStringMap(normalizedGeographicLevel);		
	}

	@Override
	@Field(OrganizationSolrFields.ORGANIZATION_DOMAIN_ALL)
	public void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
		Map<String, List<String>> normalizedOrganizationDomain = SolrUtils.normalizeStringListMapByAddingPrefix(
				OrganizationSolrFields.ORGANIZATION_DOMAIN + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, organizationDomain);
		super.setOrganizationDomain(normalizedOrganizationDomain);		
	}

	@Override
	@Field(OrganizationSolrFields.EUROPEANA_ROLE_ALL)
	public void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
		Map<String, List<String>> normalizedEuropeanaRole = SolrUtils.normalizeStringListMapByAddingPrefix(
				OrganizationSolrFields.EUROPEANA_ROLE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, europeanaRole);
		super.setEuropeanaRole(normalizedEuropeanaRole);
	}

	@Override
	@Field(OrganizationSolrFields.FOAF_HOMEPAGE)
	public void setHomepage(String homepage) {
		super.setHomepage(homepage);
	}

	@Override
	@Field(OrganizationSolrFields.FOAF_LOGO)
	public void setLogo(String logo) {
		super.setLogo(logo);
	}
	
	@Override
	@Field(OrganizationSolrFields.DEPICTION)
	public void setDepiction(String depiction) {
		super.setDepiction(depiction);
	}
	
	@Override
	@Field(OrganizationSolrFields.SAME_AS)
	public void setSameAs(List<String> sameAs) {
		super.setSameAs(sameAs);
	}
	
	@Override
	@Field(OrganizationSolrFields.VCARD_HAS_ADDRESS)
	public void setHasAddress(String hasAddress) {
		super.setHasAddress(hasAddress);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_POSTAL_CODE)
	public void setPostalCode(String postalCode) {
		super.setPostalCode(postalCode);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_POST_OFFICE_BOX)
	public void setPostBox(String postBox) {
		super.setPostBox(postBox);
	}

	@Override
	@Field(OrganizationSolrFields.COUNTRY)
	public void setCountry(String country) {
		super.setCountry(country);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_LOCALITY)
	public void setLocality(String locality) {
		super.setLocality(locality);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_STREET_ADDRESS)
	public void setStreetAddress(String streetAddress) {
		super.setStreetAddress(streetAddress);
	}


	@Override
	@Field(OrganizationSolrFields.VCARD_COUNTRYNAME)
	public void setCountryName(String countryName) {
		super.setCountryName(countryName);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_REGION)
	public void setRegion(String region) {
		super.setRegion(region);
	}

	@Override
	@Field(OrganizationSolrFields.FOAF_PHONE)
	public void setPhone(List<String> phone) {
		super.setPhone(phone);
	}

	@Override
	@Field(OrganizationSolrFields.FOAF_MBOX)
	public void setMbox(List<String> mbox) {
		super.setMbox(mbox);
	}

	@Override
	@Field(OrganizationSolrFields.VCARD_HAS_GEO)
	public void setHasGeo(String hasGeo) {
		super.setHasGeo(hasGeo);
	}

	public String getPayload() {
		return payload;
	}

	@Field(AgentSolrFields.PAYLOAD)
	public void setPayload(String payload) {
		this.payload = payload;
	}
}
