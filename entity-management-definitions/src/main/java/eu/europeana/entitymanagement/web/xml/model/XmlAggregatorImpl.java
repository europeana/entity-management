package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_FOAF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_AGGREGATOR;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_GEOGRAPHIC_SCOPE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HERITAGE_DOMAIN;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_MBOX;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PROVIDES_CAPACITY_BUILDING_ACTIVITY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_AGGREGATOR)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAggregatorImpl extends XmlOrganizationImpl {

  @XmlElement(namespace = NAMESPACE_FOAF, name = XML_MBOX)
  private String mbox;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_GEOGRAPHIC_SCOPE)
  private String geographicScope;
  
  @XmlElement(namespace = NAMESPACE_EDM, name = XML_HERITAGE_DOMAIN)
  private List<String> heritageDomain;
  
  @XmlElement(namespace = NAMESPACE_EDM, name = XML_PROVIDES_SUPPORT_FOR_MEDIA_TYPE)
  private List<String> providesSupportForMediaType;
  
  @XmlElement(namespace = NAMESPACE_EDM, name = XML_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY)
  private List<String> providesSupportForDataActivity;
  
  @XmlElement(namespace = NAMESPACE_EDM, name = XML_PROVIDES_CAPACITY_BUILDING_ACTIVITY)
  private List<String> providesCapacityBuildingActivity;
  
  @XmlElement(namespace = NAMESPACE_EDM, name = XML_PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY)
  private List<String> providesAudienceEngagementActivity;

  public XmlAggregatorImpl(Aggregator aggregator) {
    super(aggregator);
    this.mbox=aggregator.getMbox();
    this.geographicScope=aggregator.getGeographicScope();
    if (aggregator.getHeritageDomain() != null) {
      this.heritageDomain = new ArrayList<String>(aggregator.getHeritageDomain());
    }
    if (aggregator.getProvidesSupportForMediaType() != null) {
      this.providesSupportForMediaType = new ArrayList<String>(aggregator.getProvidesSupportForMediaType());
    }
    if (aggregator.getProvidesSupportForDataActivity() != null) {
      this.providesSupportForDataActivity = new ArrayList<String>(aggregator.getProvidesSupportForDataActivity());
    }
    if (aggregator.getProvidesCapacityBuildingActivity() != null) {
      this.providesCapacityBuildingActivity = new ArrayList<String>(aggregator.getProvidesCapacityBuildingActivity());
    }
    if (aggregator.getProvidesAudienceEngagementActivity() != null) {
      this.providesAudienceEngagementActivity = new ArrayList<String>(aggregator.getProvidesAudienceEngagementActivity());
    }    
  }

  @Override
  public Aggregator toEntityModel() throws EntityModelCreationException {
    super.toEntityModel();
    ((Aggregator)entity).setMbox(getMbox());
    ((Aggregator)entity).setGeographicScope(getGeographicScope());
    ((Aggregator)entity).setProvidesSupportForMediaType(getProvidesSupportForMediaType());
    ((Aggregator)entity).setProvidesSupportForDataActivity(getProvidesSupportForDataActivity());
    ((Aggregator)entity).setProvidesCapacityBuildingActivity(getProvidesCapacityBuildingActivity());
    ((Aggregator)entity).setProvidesAudienceEngagementActivity(getProvidesAudienceEngagementActivity());
    return ((Aggregator)entity);
  }

  public XmlAggregatorImpl() {
    // default constructor
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.Aggregator;
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
