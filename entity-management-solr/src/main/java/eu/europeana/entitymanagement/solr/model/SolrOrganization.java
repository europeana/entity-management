package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europeana.entitymanagement.definitions.model.GeoLocation;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.utils.SolrGeneralUtils;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

public class SolrOrganization extends SolrEntity<Organization> {

  @Field(SAME_AS)
  private List<String> sameAs;

  @Field(AGGREGATED_VIA)
  private List<String> aggregatedVia;

  @Field(DC_DESCRIPTION_ALL)
  private Map<String, String> description;

  @Field(EDM_ACRONYM_ALL)
  private Map<String, List<String>> acronym;

  @Field(FOAF_LOGO)
  private String logo;

  @Field(FOAF_HOMEPAGE)
  private String homepage;

  @Field(FOAF_PHONE)
  private List<String> phone;

  @Field(EUROPEANA_ROLE)
  private List<String> europeanaRole;

  @Field(COUNTRY)
  private List<String> country;
  
  @Field(COUNTRY_LABEL_ALL)
  private Map<String, String> countryLabel;

  @Field(VCARD_HAS_ADDRESS)
  private String hasAddress;

  @Field(VCARD_STREET_ADDRESS)
  private String streetAddress;

  @Field(VCARD_LOCALITY)
  private String locality;

  @Field(VCARD_REGION)
  private String region;

  @Field(VCARD_POSTAL_CODE)
  private String postalCode;

  @Field(VCARD_COUNTRYNAME)
  private String countryName;

  @Field(VCARD_POST_OFFICE_BOX)
  private String postBox;

  @Field(VCARD_HAS_GEO)
  private String hasGeo;

  public SolrOrganization() {
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
    
    if(organization.getEuropeanaRoleIds()!=null) this.europeanaRole=new ArrayList<>(organization.getEuropeanaRoleIds());
    
    this.country=new ArrayList<>();
    String orgCountryId=organization.getCountryId();
    String orgCountryISO=organization.getCountryISO();
    if (orgCountryId != null) {
      this.country.add(orgCountryId);
    }
    if (orgCountryISO != null) {
      this.country.add(orgCountryISO);
    }
    if (organization.getCountry() != null) {
      this.setCountryLabel(organization.getCountry().getPrefLabel()); 
    }
    
    
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
      if (organizationAddress.getVcardHasGeo() != null && organizationAddress.getVcardHasGeo().getId() != null) {
        this.hasGeo = EntityUtils.toLatLongValue(organizationAddress.getVcardHasGeo().getId());
      }
    }
  }

  private void setDescription(Map<String, String> dcDescription) {
    if (MapUtils.isNotEmpty(dcDescription)) {
      this.description =
          new HashMap<>(
              SolrGeneralUtils.normalizeStringMapByAddingPrefix(
                  DC_DESCRIPTION + DYNAMIC_FIELD_SEPARATOR,
                  dcDescription));
    }
  }

  private void setAcronym(Map<String, List<String>> acronym) {
    if (MapUtils.isNotEmpty(acronym)) {
      this.acronym =
          new HashMap<>(
              SolrGeneralUtils.normalizeStringListMapByAddingPrefix(
                  EDM_ACRONYM + DYNAMIC_FIELD_SEPARATOR,
                  acronym));
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

  public List<String> getEuropeanaRole() {
    return europeanaRole;
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
  
    public List<String> getCountry() {
    return country;
  }

    public Map<String, String> getCountryLabel() {
      return countryLabel;
    }

    public void setCountryLabel(Map<String, String> countryLabel) {
      if (MapUtils.isNotEmpty(countryLabel)) {
        this.countryLabel =
            new HashMap<>(
                SolrGeneralUtils.normalizeStringMapByAddingPrefix(
                    COUNTRY_LABEL + DYNAMIC_FIELD_SEPARATOR,
                    countryLabel));
      }
    }
}
