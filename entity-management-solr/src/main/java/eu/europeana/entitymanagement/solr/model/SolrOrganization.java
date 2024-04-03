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
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.*;

public class SolrOrganization extends SolrEntity<Organization> {

  @Field(EntitySolrFields.SAME_AS)
  private List<String> sameAs;

  @Field(EntitySolrFields.AGGREGATED_VIA)
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

  @Field(FOAF_MBOX)
  private List<String> mbox;

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
    
    if(organization.getEuropeanaRoleIds()!=null) this.europeanaRole=new ArrayList<>(organization.getEuropeanaRoleIds());
    
    this.country=new ArrayList<>();
    String orgCountryId=organization.getCountryId();
    String orgCoutryISO=organization.getCountryISO();
    if(orgCountryId!=null) {
      this.country.add(orgCountryId);
    }
    if(orgCoutryISO!=null) {
      this.country.add(orgCoutryISO);
    }
    if(organization.getCountry() != null) {
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
      this.hasGeo = EntityUtils.toLatLongValue(organizationAddress.getVcardHasGeo());
    }
  }

  private void setDescription(Map<String, String> dcDescription) {
    if (MapUtils.isNotEmpty(dcDescription)) {
      this.description =
          new HashMap<>(
              SolrUtils.normalizeStringMapByAddingPrefix(
                  DC_DESCRIPTION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                  dcDescription));
    }
  }

  private void setAcronym(Map<String, List<String>> acronym) {
    if (MapUtils.isNotEmpty(acronym)) {
      this.acronym =
          new HashMap<>(
              SolrUtils.normalizeStringListMapByAddingPrefix(
                  EDM_ACRONYM + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
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

  public List<String> getMbox() {
    return mbox;
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
                SolrUtils.normalizeStringMapByAddingPrefix(
                    COUNTRY_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
                    countryLabel));
      }
    }
}
