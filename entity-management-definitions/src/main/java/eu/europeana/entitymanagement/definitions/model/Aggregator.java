package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
  CONTEXT,
  ID,
  TYPE,
  DEPICTION,
  IS_SHOWN_BY,
  PREF_LABEL,
  ACRONYM,
  ALT_LABEL,
  HIDDEN_LABEL,
  DESCRIPTION,
  FOAF_LOGO,
  EUROPEANA_ROLE,
  COUNTRY,
  LANGUAGE,
  GEOGRAPHIC_SCOPE,
  HERITAGE_DOMAIN,
  FOAF_HOMEPAGE,
  FOAF_PHONE,
  FOAF_MBOX,
  HAS_ADDRESS,
  PROVIDES_SUPPORT_FOR_MEDIA_TYPE,
  PROVIDES_SUPPORT_FOR_DATA_ACTIVITY,
  PROVIDES_SUPPORT_FOR_CAPACITY_BUILDING_ACTIVITY,
  PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY,
  AGGREGATES_FROM,
  AGGREGATED_VIA,
  IDENTIFIER,
  SAME_AS,
  IS_AGGREGATED_BY
})
/**
 * Class implementing aggregator extension for organizations
 */
public class Aggregator extends Organization {
  protected String mbox;
  protected String geographicScope;
  protected List<String> heritageDomain;
  protected List<String> providesSupportForMediaType;
  protected List<String> providesSupportForDataActivity;
  protected List<String> providesCapacityBuildingActivity;
  protected List<String> providesAudienceEngagementActivity;
  
  /**
   * Public constructor
   */
  public Aggregator() {
    super();
    type=EntityTypes.Aggregator.getEntityType();
  }

  /**
   * Public constructor creating a copy of the provided aggregator
   * @param copy
   */
  public Aggregator(Aggregator copy) {
//    this((Organization) copy);
    super(copy);
    type=EntityTypes.Aggregator.getEntityType();
    
    this.mbox = copy.getMbox();
    this.geographicScope = copy.getGeographicScope();
    if (copy.getHeritageDomain() != null) {
      this.heritageDomain = new ArrayList<>(copy.getHeritageDomain());
    }
    if (copy.getProvidesSupportForMediaType() != null) {
      this.providesSupportForMediaType = new ArrayList<>(copy.getProvidesSupportForMediaType());
    }
    if (copy.getProvidesSupportForDataActivity() != null) {
      this.providesSupportForDataActivity = new ArrayList<>(copy.getProvidesSupportForDataActivity());
    }
    if (copy.getProvidesCapacityBuildingActivity() != null) {
      this.providesCapacityBuildingActivity = new ArrayList<>(copy.getProvidesCapacityBuildingActivity());
    }
    if (copy.getProvidesAudienceEngagementActivity() != null) {
      this.providesAudienceEngagementActivity = new ArrayList<>(copy.getProvidesAudienceEngagementActivity());
    }
  }
  
  /**
   * Public constructor creating an aggregator object from an organization 
   * @param copy
   */
  public Aggregator(Organization org) {
    super(org);
    type=EntityTypes.Aggregator.getEntityType();
  }

  @Override
  public String getType() {
    return type;
  }

  @JsonGetter(FOAF_MBOX)
  public String getMbox() {
    return mbox;
  }

  @JsonSetter(FOAF_MBOX)
  public void setMbox(String mbox) {
    this.mbox = mbox;
  }

  @JsonGetter(GEOGRAPHIC_SCOPE)
  public String getGeographicScope() {
    return geographicScope;
  }

  @JsonSetter(GEOGRAPHIC_SCOPE)
  public void setGeographicScope(String geographicScope) {
    this.geographicScope = geographicScope;
  }

  @JsonGetter(HERITAGE_DOMAIN)
  public List<String> getHeritageDomain() {
    return heritageDomain;
  }

  @JsonSetter(HERITAGE_DOMAIN)
  public void setHeritageDomain(List<String> heritageDomain) {
    this.heritageDomain = heritageDomain;
  }

  @JsonGetter(PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  public List<String> getProvidesSupportForMediaType() {
    return providesSupportForMediaType;
  }

  @JsonSetter(PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  public void setProvidesSupportForMediaType(List<String> providesSupportForMediaType) {
    this.providesSupportForMediaType = providesSupportForMediaType;
  }

  @JsonGetter(PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  public List<String> getProvidesSupportForDataActivity() {
    return providesSupportForDataActivity;
  }

  @JsonSetter(PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  public void setProvidesSupportForDataActivity(List<String> providesSupportForDataActivity) {
    this.providesSupportForDataActivity = providesSupportForDataActivity;
  }

  @JsonGetter(PROVIDES_SUPPORT_FOR_CAPACITY_BUILDING_ACTIVITY)
  public List<String> getProvidesCapacityBuildingActivity() {
    return providesCapacityBuildingActivity;
  }

  @JsonSetter(PROVIDES_SUPPORT_FOR_CAPACITY_BUILDING_ACTIVITY)
  public void setProvidesCapacityBuildingActivity(List<String> providesCapacityBuildingActivity) {
    this.providesCapacityBuildingActivity = providesCapacityBuildingActivity;
  }

  @JsonGetter(PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY)
  public List<String> getProvidesAudienceEngagementActivity() {
    return providesAudienceEngagementActivity;
  }

  @JsonSetter(PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY)
  public void setProvidesAudienceEngagementActivity(List<String> providesAudienceEngagementActivity) {
    this.providesAudienceEngagementActivity = providesAudienceEngagementActivity;
  }

}
