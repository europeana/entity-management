package eu.europeana.entitymanagement.web.xml.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class XmlIsAggregatedByImpl {

  @XmlTransient String id;

  public XmlIsAggregatedByImpl(String aggregation_id) {
    id = aggregation_id;
  }

  @XmlAttribute(name = XmlConstants.RESOURCE)
  public String getAbout() {
    return id;
  }
}
