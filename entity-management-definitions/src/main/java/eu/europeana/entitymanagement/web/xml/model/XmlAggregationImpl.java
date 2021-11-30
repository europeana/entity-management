package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class XmlAggregationImpl {

  @XmlAttribute(namespace = NAMESPACE_RDF, name = ABOUT)
  private String id;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_CREATED)
  private Date created;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_MODIFIED)
  private Date modified;

  @XmlElement(namespace = NAMESPACE_ORE, name = XML_AGGREGATES)
  private List<LabelledResource> aggregates;

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
