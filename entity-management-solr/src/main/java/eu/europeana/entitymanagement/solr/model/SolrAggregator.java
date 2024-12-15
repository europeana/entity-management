package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_AUDIENCE_ENGAGEMENT_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_GEOGRAPHIC_SCOPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_HERITAGE_DOMAIN;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_CAPACITY_BUILDING_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.EDM_PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.FOAF_MBOX;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Aggregator;

public class SolrAggregator extends SolrOrganization {

  @Field(FOAF_MBOX)
  private String mbox;
  
  @Field(EDM_GEOGRAPHIC_SCOPE)
  private String geographicScope;
  
  @Field(EDM_HERITAGE_DOMAIN)
  List<String> heritageDomain;
  
  @Field(EDM_PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  List<String> providesSupportForMediaType;
  
  @Field(EDM_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  List<String> providesSupportForDataActivity;
  
  @Field(EDM_PROVIDES_CAPACITY_BUILDING_ACTIVITY)
  List<String> providesCapacityBuildingActivity;
  
  @Field(EDM_AUDIENCE_ENGAGEMENT_ACTIVITY)
  List<String> providesAudienceEngagementActivity;

  public SolrAggregator() {
  }

  public SolrAggregator(Aggregator aggregator) {
    super(aggregator);
    this.mbox = aggregator.getMbox();   
    this.geographicScope = aggregator.getGeographicScope();
    if (aggregator.getHeritageDomain() != null) {
      this.heritageDomain = new ArrayList<>(aggregator.getHeritageDomain());
    }
    if (aggregator.getProvidesSupportForMediaType() != null) {
      this.providesSupportForMediaType = new ArrayList<>(aggregator.getProvidesSupportForMediaType());
    }
    if (aggregator.getProvidesSupportForDataActivity() != null) {
      this.providesSupportForDataActivity = new ArrayList<>(aggregator.getProvidesSupportForDataActivity());
    }
    if (aggregator.getProvidesCapacityBuildingActivity() != null) {
      this.providesCapacityBuildingActivity = new ArrayList<>(aggregator.getProvidesCapacityBuildingActivity());
    }
    if (aggregator.getProvidesAudienceEngagementActivity() != null) {
      this.providesAudienceEngagementActivity = new ArrayList<>(aggregator.getProvidesAudienceEngagementActivity());
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


