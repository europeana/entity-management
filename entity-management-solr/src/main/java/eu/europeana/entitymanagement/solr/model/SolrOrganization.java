package eu.europeana.entitymanagement.solr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields;

public class SolrOrganization extends SolrEntity<Organization> {

  @Field(EntitySolrFields.SAME_AS)
  private List<String> sameAs;

  @Field(EntitySolrFields.AGGREGATED_VIA)
  private List<String> aggregatedVia;

  @Field(OrganizationSolrFields.DC_DESCRIPTION_ALL)
  private Map<String, String> description;

  @Field(OrganizationSolrFields.EDM_ACRONYM_ALL)
  private Map<String, List<String>> acronym;

  @Field(OrganizationSolrFields.FOAF_LOGO)
  private String logo;

  @Field(OrganizationSolrFields.FOAF_HOMEPAGE)
  private String homepage;

  @Field(OrganizationSolrFields.FOAF_PHONE)
  private List<String> phone;

  @Field(OrganizationSolrFields.FOAF_MBOX)
  private List<String> mbox;

  @Field(OrganizationSolrFields.EUROPEANA_ROLE_ALL)
  private Map<String, List<String>> europeanaRole;

  @Field(OrganizationSolrFields.ORGANIZATION_DOMAIN_ALL)
  private Map<String, List<String>> organizationDomain;

  @Field(OrganizationSolrFields.GEOGRAPHIC_LEVEL_ALL)
  private Map<String, String> geographicLevel;

  @Field(OrganizationSolrFields.COUNTRY)
  private String country;

  @Field(OrganizationSolrFields.VCARD_HAS_ADDRESS)
  private String hasAddress;

  @Field(OrganizationSolrFields.VCARD_STREET_ADDRESS)
  private String streetAddress;

  @Field(OrganizationSolrFields.VCARD_LOCALITY)
  private String locality;

  @Field(OrganizationSolrFields.VCARD_REGION)
  private String region;

  @Field(OrganizationSolrFields.VCARD_POSTAL_CODE)
  private String postalCode;

  @Field(OrganizationSolrFields.VCARD_COUNTRYNAME)
  private String countryName;

  @Field(OrganizationSolrFields.VCARD_POST_OFFICE_BOX)
  private String postBox;

  @Field(OrganizationSolrFields.VCARD_HAS_GEO)
  private String hasGeo;

  public SolrOrganization() {
    super();
  }

  public SolrOrganization(Organization organization) {
    super(organization);

    setDescription(organization.getDescription());
    setAcronym(organization.getAcronym());
    if (organization.getLogo() != null) {
      this.logo = organization.getLogo().getId();
    }
    this.homepage = organization.getHomepage();
    this.phone = organization.getPhone();
    if (organization.getMbox() != null) this.mbox = new ArrayList<>(organization.getMbox());
    setEuropeanaRole(organization.getEuropeanaRole());
    setOrganizationDomain(organization.getOrganizationDomain());
    setGeographicLevel(organization.getGeographicLevel());
    this.country = organization.getCountry();
    if (organization.getSameReferenceLinks() != null) {
      this.sameAs = new ArrayList<>(organization.getSameReferenceLinks());
    }
    if (organization.getAggregatedVia() != null) {
      this.aggregatedVia = new ArrayList<>(organization.getAggregatedVia());
    }
    Address organizationAddress = organization.getAddress();
    if (organizationAddress != null) {
      this.hasAddress = organizationAddress.getAbout();
      this.streetAddress = organizationAddress.getVcardStreetAddress();
      this.locality = organizationAddress.getVcardLocality();
      this.postalCode = organizationAddress.getVcardPostalCode();
      this.countryName = organizationAddress.getVcardCountryName();
      this.postBox = organizationAddress.getVcardPostOfficeBox();
      this.hasGeo = EntityUtils.toLatLongValue(organizationAddress.getVcardHasGeo());
    }
  }

  private void setDescription(Map<String, String> dcDescription) {
    if (MapUtils.isNotEmpty(dcDescription)) {
      this.description =
          new HashMap<>(
              SolrUtils.normalizeStringMapByAddingPrefix(
                  OrganizationSolrFields.DC_DESCRIPTION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  dcDescription));
    }
  }

  private void setAcronym(Map<String, List<String>> acronym) {
    if (MapUtils.isNotEmpty(acronym)) {
      this.acronym =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  OrganizationSolrFields.EDM_ACRONYM + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  acronym));
    }
  }

  private void setGeographicLevel(Map<String, String> geographicLevel) {
    if (MapUtils.isNotEmpty(geographicLevel)) {
      this.geographicLevel =
          new HashMap<>(
              SolrUtils.normalizeStringMapByAddingPrefix(
                  OrganizationSolrFields.GEOGRAPHIC_LEVEL
                      + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  geographicLevel));
    }
  }

  private void setOrganizationDomain(Map<String, List<String>> organizationDomain) {
    if (MapUtils.isNotEmpty(organizationDomain)) {
      this.organizationDomain =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  OrganizationSolrFields.ORGANIZATION_DOMAIN
                      + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  organizationDomain));
    }
  }

  private void setEuropeanaRole(Map<String, List<String>> europeanaRole) {
    if (MapUtils.isNotEmpty(europeanaRole)) {
      this.europeanaRole =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  OrganizationSolrFields.EUROPEANA_ROLE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  europeanaRole));
    }
  }

  public Map<String, String> getDescription() {
    return description;
  }

  public Map<String, List<String>> getAcronym() {
    return acronym;
  }

  public String getLogo() {
    return logo;
  }

  public String getHomepage() {
    return homepage;
  }

  public List<String> getPhone() {
    return phone;
  }

  public List<String> getMbox() {
    return mbox;
  }

  public Map<String, List<String>> getEuropeanaRole() {
    return europeanaRole;
  }

  public Map<String, List<String>> getOrganizationDomain() {
    return organizationDomain;
  }

  public Map<String, String> getGeographicLevel() {
    return geographicLevel;
  }

  public String getCountry() {
    return country;
  }

  public String getHasAddress() {
    return hasAddress;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public String getLocality() {
    return locality;
  }

  public String getRegion() {
    return region;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountryName() {
    return countryName;
  }

  public String getPostBox() {
    return postBox;
  }

  public String getHasGeo() {
    return hasGeo;
  }

  @Override
  protected void setSameReferenceLinks(ArrayList<String> uris) {
    this.sameAs = uris;
  }

  public List<String> getAggregatedVia() {
    return aggregatedVia;
  }
}
