package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.AUDIENCE_ENGAGEMENT_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.GEOGRAPHIC_SCOPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.HERITAGE_DOMAIN;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.MBOX;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.PROVIDES_CAPACITY_BUILDING_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.PARENT_TYPE;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

public class SolrAggregator extends SolrOrganization {

  @Field(MBOX)
  private String mbox;
  
  @Field(GEOGRAPHIC_SCOPE)
  private String geographicScope;
  
  @Field(HERITAGE_DOMAIN)
  List<String> heritageDomain;
  
  @Field(PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  List<String> providesSupportForMediaType;
  
  @Field(PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  List<String> providesSupportForDataActivity;
  
  @Field(PROVIDES_CAPACITY_BUILDING_ACTIVITY)
  List<String> providesCapacityBuildingActivity;
  
  @Field(AUDIENCE_ENGAGEMENT_ACTIVITY)
  List<String> providesAudienceEngagementActivity;
  
//  @Field(PARENT_TYPE)
//  String parentType;

  public SolrAggregator() {
  }

  public SolrAggregator(Aggregator aggregator) {
    super(aggregator);
    this.mbox = aggregator.getMbox();   
    this.geographicScope = aggregator.getGeographicScope();
//  this.parentType = EntityTypes.Aggregator.getParentType();
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

//  public String getParentType() {
//    return parentType;
//  }

}


