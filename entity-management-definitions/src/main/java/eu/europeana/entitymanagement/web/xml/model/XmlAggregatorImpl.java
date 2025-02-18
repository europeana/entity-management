package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_AGGREGATOR)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType( propOrder = {
      ABOUT,
      DEPICTION,
      IS_SHOWN_BY,
      PREF_LABEL,
      XML_ACRONYM,
      ALT_LABEL,
      HIDDEN_LABEL,
      XML_DESCRIPTION,
      XML_LOGO,
      XML_EUROPEANA_ROLE,
      XML_COUNTRY,
      XML_LANGUAGE,
      GEOGRAPHIC_SCOPE,
      HERITAGE_DOMAIN,
      XML_HOMEPAGE,
      XML_PHONE,
      XML_MBOX,
      XML_HAS_ADDRESS,
      XML_PROVIDES_SUPPORT_FOR_MEDIA_TYPE,
      XML_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY,
      XML_PROVIDES_CAPACITY_BUILDING_ACTIVITY,
      XML_PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY,
      XML_AGGREGATED_VIA,
      XML_AGGREGATES_FROM,
      XML_IDENTIFIER,
      XML_SAME_AS,
      IS_AGGREGATED_BY
    })
/**
 * class for xml serialization of Aggregators
 */
@SuppressWarnings("java:S2384")
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

  /**
   * Constructor to convert the POJO for XML serialization
   * @param aggregator
   */
  public XmlAggregatorImpl(Aggregator aggregator) {
    super(aggregator);
    this.mbox=aggregator.getMbox();
    this.geographicScope=aggregator.getGeographicScope();
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

  public XmlAggregatorImpl() {
    // default constructor
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
