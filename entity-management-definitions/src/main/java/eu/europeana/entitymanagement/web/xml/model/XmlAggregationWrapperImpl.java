package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlElement;

public class XmlAggregationWrapperImpl {

  @XmlElement(namespace = XmlConstants.NAMESPACE_ORE, name = XmlConstants.AGGREGATION)
  private XmlAggregationImpl aggregation;

  public XmlAggregationWrapperImpl(XmlAggregationImpl aggregation) {
    this.aggregation=aggregation;
  }

  public XmlAggregationWrapperImpl() {}
}
