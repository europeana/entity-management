package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ABOUT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC_TERMS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_ORE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_RDF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_AGGREGATES;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_CREATED;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_MODIFIED;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import eu.europeana.entitymanagement.definitions.model.Aggregation;

/**
 * class for xml serialization of aggregations
 */
public class XmlAggregationImpl {

  @XmlAttribute(namespace = NAMESPACE_RDF, name = ABOUT)
  private String id;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_CREATED)
  private Date created;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_MODIFIED)
  private Date modified;

  @XmlElement(namespace = NAMESPACE_ORE, name = XML_AGGREGATES)
  private List<LabelledResource> aggregates;

  /**
   * Build object for xml serialization from POJO
   * @param aggregation the aggregator object to serialize 
   */
  public XmlAggregationImpl(Aggregation aggregation) {
    this.id = aggregation.getId();
    this.created = aggregation.getCreated();
    this.modified = aggregation.getModified();
    // convert aggregate string values to LabelledResource for xml
    this.aggregates =
        aggregation.getAggregates().stream()
            .map(LabelledResource::new)
            .collect(Collectors.toList());
  }

  public XmlAggregationImpl() {}
}
