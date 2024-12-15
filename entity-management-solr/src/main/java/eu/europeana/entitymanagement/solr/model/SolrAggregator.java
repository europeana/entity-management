package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_AUDIENCE_ENGAGEMENT_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_GEOGRAPHIC_SCOPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_HERITAGE_DOMAIN;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_CAPACITY_BUILDING_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.FOAF_MBOX;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.COUNTRY;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.COUNTRY_LABEL;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.COUNTRY_LABEL_ALL;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.DC_DESCRIPTION;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.DC_DESCRIPTION_ALL;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.EDM_ACRONYM;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.EDM_ACRONYM_ALL;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.EUROPEANA_ROLE;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.FOAF_HOMEPAGE;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.FOAF_LOGO;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.FOAF_PHONE;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_COUNTRYNAME;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_HAS_ADDRESS;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_HAS_GEO;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_LOCALITY;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_POSTAL_CODE;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_POST_OFFICE_BOX;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_REGION;
import static eu.europeana.entitymanagement.vocabulary.OrganizationSolrFields.VCARD_STREET_ADDRESS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.utils.EntityUtils;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

public class SolrAggregator extends SolrEntity<Aggregator> {

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

  @Field(FOAF_MBOX)
  private String mbox;
  
  @Field(EDM_GEOGRAPHIC_SCOPE)
  private String geographicScope;
  
  @Field(EDM_HERITAGE_DOMAIN)
  private List<String> heritageDomain;
  
  @Field(EDM_PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  private List<String> providesSupportForMediaType;
  
  @Field(EDM_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  private List<String> providesSupportForDataActivity;
  
  @Field(EDM_PROVIDES_CAPACITY_BUILDING_ACTIVITY)
  private List<String> providesCapacityBuildingActivity;
  
  @Field(EDM_AUDIENCE_ENGAGEMENT_ACTIVITY)
  private List<String> providesAudienceEngagementActivity;

  public SolrAggregator() {
  }

  public SolrAggregator(Aggregator aggreg) {
    super(aggreg);

    setDescription(aggreg.getDescription());
    setAcronym(aggreg.getAcronym());
    if (aggreg.getLogo() != null) {
      this.logo = aggreg.getLogo().getId();
    }
    this.homepage = aggreg.getHomepage();
    this.phone = aggreg.getPhone();
    
    if(aggreg.getEuropeanaRoleIds()!=null) this.europeanaRole=new ArrayList<>(aggreg.getEuropeanaRoleIds());
    
    this.country=new ArrayList<>();
    String orgCountryId=aggreg.getCountryId();
    String orgCountryISO=aggreg.getCountryISO();
    if(orgCountryId!=null) {
      this.country.add(orgCountryId);
    }
    if(orgCountryISO!=null) {
      this.country.add(orgCountryISO);
    }
    if(aggreg.getCountry() != null) {
      this.setCountryLabel(aggreg.getCountry().getPrefLabel()); 
    }
    
    if (aggreg.getSameReferenceLinks() != null) {
      this.sameAs = new ArrayList<>(aggreg.getSameReferenceLinks());
    }
    if (aggreg.getAggregatedVia() != null) {
      this.aggregatedVia = new ArrayList<>(aggreg.getAggregatedVia());
    }
    Address organizationAddress = aggreg.getAddress();
    if (organizationAddress != null) {
      this.hasAddress = organizationAddress.getAbout();
      this.streetAddress = organizationAddress.getVcardStreetAddress();
      this.locality = organizationAddress.getVcardLocality();
      this.postalCode = organizationAddress.getVcardPostalCode();
      this.countryName = organizationAddress.getVcardCountryName();
      this.postBox = organizationAddress.getVcardPostOfficeBox();
      this.hasGeo = EntityUtils.toLatLongValue(organizationAddress.getVcardHasGeo());
    }
    
    this.mbox = aggreg.getMbox();   
    this.geographicScope = aggreg.getGeographicScope();
    if (aggreg.getHeritageDomain() != null) {
      this.heritageDomain = new ArrayList<>(aggreg.getHeritageDomain());
    }
    if (aggreg.getProvidesSupportForMediaType() != null) {
      this.providesSupportForMediaType = new ArrayList<>(aggreg.getProvidesSupportForMediaType());
    }
    if (aggreg.getProvidesSupportForDataActivity() != null) {
      this.providesSupportForDataActivity = new ArrayList<>(aggreg.getProvidesSupportForDataActivity());
    }
    if (aggreg.getProvidesCapacityBuildingActivity() != null) {
      this.providesCapacityBuildingActivity = new ArrayList<>(aggreg.getProvidesCapacityBuildingActivity());
    }
    if (aggreg.getProvidesAudienceEngagementActivity() != null) {
      this.providesAudienceEngagementActivity = new ArrayList<>(aggreg.getProvidesAudienceEngagementActivity());
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
  
  public String getMbox() {
    return mbox;
  }

  public String getGeographicScope() {
    return geographicScope;
  }

  public List<String> getHeritageDomain() {
    return heritageDomain;
  }

  public List<String> getProvidesSupportForMediaType() {
    return providesSupportForMediaType;
  }

  public List<String> getProvidesSupportForDataActivity() {
    return providesSupportForDataActivity;
  }

  public List<String> getProvidesCapacityBuildingActivity() {
    return providesCapacityBuildingActivity;
  }

  public List<String> getProvidesAudienceEngagementActivity() {
    return providesAudienceEngagementActivity;
  }
  
}
