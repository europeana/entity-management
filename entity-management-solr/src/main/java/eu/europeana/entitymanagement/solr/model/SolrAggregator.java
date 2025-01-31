package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.vocabulary.AggregatorSolrFields.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;
import eu.europeana.entitymanagement.definitions.model.Aggregator;

public class SolrAggregator extends SolrOrganization {

  @Field(MBOX)
  private String mbox;
  
  @Field(GEOGRAPHIC_SCOPE)
  private String geographicScope;
  
  @Field(HERITAGE_DOMAIN)
  private List<String> heritageDomain;
  
  @Field(PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  private List<String> providesSupportForMediaType;
  
  @Field(PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  private List<String> providesSupportForDataActivity;
  
  @Field(PROVIDES_CAPACITY_BUILDING_ACTIVITY)
  private List<String> providesCapacityBuildingActivity;
  
  @Field(AUDIENCE_ENGAGEMENT_ACTIVITY)
  private List<String> providesAudienceEngagementActivity;
  
  /**
   * public constructor
   */
  public SolrAggregator() {
  }

  /**
   * Constructor to convert aggregators to solr representation
   * @param aggregator aggregator POJO 
   */
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


