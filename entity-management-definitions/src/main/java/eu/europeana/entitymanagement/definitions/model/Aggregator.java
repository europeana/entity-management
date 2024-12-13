package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ACRONYM;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.AGGREGATED_VIA;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.AGGREGATES_FROM;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ALT_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.COUNTRY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DEPICTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.DESCRIPTION;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.EUROPEANA_ROLE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_HOMEPAGE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_LOGO;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_MBOX;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.FOAF_PHONE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.GEOGRAPHIC_SCOPE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HAS_ADDRESS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HERITAGE_DOMAIN;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IDENTIFIER;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.LANGUAGE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PREF_LABEL;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROVIDES_SUPPORT_FOR_BUILDING_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.SAME_AS;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;
import java.lang.reflect.Field;
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
  PREF_LABEL,
  ACRONYM,
  ALT_LABEL,
  HIDDEN_LABEL,
  DESCRIPTION,
  FOAF_LOGO,
  EUROPEANA_ROLE,
  COUNTRY,
  LANGUAGE,
  FOAF_HOMEPAGE,
  FOAF_PHONE,
  FOAF_MBOX,
  HAS_ADDRESS,
  AGGREGATES_FROM,
  AGGREGATED_VIA,
  IDENTIFIER,
  SAME_AS,
  IS_AGGREGATED_BY,
  GEOGRAPHIC_SCOPE,
  HERITAGE_DOMAIN,
  PROVIDES_SUPPORT_FOR_MEDIA_TYPE,
  PROVIDES_SUPPORT_FOR_DATA_ACTIVITY,
  PROVIDES_SUPPORT_FOR_BUILDING_ACTIVITY,
  PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY
})
public class Aggregator extends Organization {
  private String mbox;
  private String geographicScope;
  List<String> heritageDomain;
  List<String> providesSupportForMediaType;
  List<String> providesSupportForDataActivity;
  List<String> providesCapacityBuildingActivity;
  List<String> providesAudienceEngagementActivity;
    
  public Aggregator() {
    super();
    type=EntityTypes.Aggregator.getEntityType();
  }

  public Aggregator(Aggregator copy) {
    super(copy);
    this.type=copy.getType();
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

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Object getFieldValue(Field field) throws IllegalAccessException {
    // method to call the getters for each field individually
    return field.get(this);
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

  @JsonGetter(PROVIDES_SUPPORT_FOR_BUILDING_ACTIVITY)
  public List<String> getProvidesCapacityBuildingActivity() {
    return providesCapacityBuildingActivity;
  }

  @JsonSetter(PROVIDES_SUPPORT_FOR_BUILDING_ACTIVITY)
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
