package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ABOUT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.BROADER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.BROAD_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.CLOSE_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.EXACT_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IN_SCHEME;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NARROWER;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NARROW_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTATION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RELATED;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.RELATED_MATCH;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_CONCEPT;

import eu.europeana.entitymanagement.definitions.exceptions.EntityCreationException;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = XML_CONCEPT)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      ABOUT,
      DEPICTION,
      IS_SHOWN_BY,
      PREF_LABEL,
      ALT_LABEL,
      HIDDEN_LABEL,
      NOTE,
      NOTATION,
      BROADER,
      NARROWER,
      RELATED,
      BROAD_MATCH,
      NARROW_MATCH,
      RELATED_MATCH,
      CLOSE_MATCH,
      EXACT_MATCH,
      IN_SCHEME,
      IS_AGGREGATED_BY
    })
public class XmlConceptImpl extends XmlBaseEntityImpl<Concept> {

  @XmlElement(namespace = NAMESPACE_SKOS, name = NARROWER)
  private List<LabelledResource> narrower = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = RELATED)
  private List<LabelledResource> related = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = BROADER)
  private List<LabelledResource> broader = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
  private List<LabelledResource> note = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = BROAD_MATCH)
  private List<LabelledResource> broadMatch = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = NARROW_MATCH)
  private List<LabelledResource> narrowMatch = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = EXACT_MATCH)
  private List<LabelledResource> exactMatch = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = RELATED_MATCH)
  private List<LabelledResource> relatedMatch = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = CLOSE_MATCH)
  private List<LabelledResource> closeMatch = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_SKOS, name = NOTATION)
  private List<LabelledResource> notation;

  @XmlElement(namespace = NAMESPACE_SKOS, name = IN_SCHEME)
  private List<LabelledResource> inScheme;

  public XmlConceptImpl() {
    // default constructor required for deserialization
    this.entity = new Concept();
  }

  public XmlConceptImpl(Concept concept) {
    super(concept);
    this.exactMatch = RdfXmlUtils.convertToRdfResource(concept.getSameReferenceLinks());
    this.related = RdfXmlUtils.convertToRdfResource(concept.getRelated());
    this.narrower = RdfXmlUtils.convertToRdfResource(concept.getNarrower());
    this.broader = RdfXmlUtils.convertToRdfResource(concept.getBroader());
    this.broadMatch = RdfXmlUtils.convertToRdfResource(concept.getBroadMatch());
    this.narrowMatch = RdfXmlUtils.convertToRdfResource(concept.getNarrowMatch());
    this.relatedMatch = RdfXmlUtils.convertToRdfResource(concept.getRelatedMatch());
    this.closeMatch = RdfXmlUtils.convertToRdfResource(concept.getCloseMatch());
    this.note = RdfXmlUtils.convertToXmlMultilingualString(concept.getNote());
    this.notation = RdfXmlUtils.convertToXmlMultilingualString(concept.getNotation());
    this.inScheme = RdfXmlUtils.convertToRdfResource(concept.getInScheme());
  }

  @Override
  public Concept toEntityModel() throws EntityCreationException {
    super.toEntityModel();

    entity.setRelated(RdfXmlUtils.toStringList(getRelated()));
    entity.setNarrower(RdfXmlUtils.toStringList(getNarrower()));
    entity.setBroader(RdfXmlUtils.toStringList(getBroader()));
    entity.setBroadMatch(RdfXmlUtils.toStringList(getBroadMatch()));
    entity.setNarrowMatch(RdfXmlUtils.toStringList(getNarrowMatch()));
    entity.setRelatedMatch(RdfXmlUtils.toStringList(getRelatedMatch()));
    entity.setCloseMatch(RdfXmlUtils.toStringList(getCloseMatch()));
    entity.setInScheme(RdfXmlUtils.toStringList(getInScheme()));
    entity.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
    entity.setNotation(RdfXmlUtils.toLanguageMapList(getNotation()));
    this.inScheme = RdfXmlUtils.convertToRdfResource(entity.getInScheme());

    return entity;
  }

  public List<LabelledResource> getBroader() {
    return this.broader;
  }

  public List<LabelledResource> getNarrower() {
    return this.narrower;
  }

  public List<LabelledResource> getRelated() {
    return this.related;
  }

  public List<LabelledResource> getBroadMatch() {
    return this.broadMatch;
  }

  public List<LabelledResource> getNarrowMatch() {
    return this.narrowMatch;
  }

  public List<LabelledResource> getExactMatch() {
    return this.exactMatch;
  }

  public List<LabelledResource> getRelatedMatch() {
    return this.relatedMatch;
  }

  public List<LabelledResource> getCloseMatch() {
    return this.closeMatch;
  }

  public List<LabelledResource> getNotation() {
    return this.notation;
  }

  public List<LabelledResource> getNote() {
    return note;
  }

  public List<LabelledResource> getInScheme() {
    return this.inScheme;
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.Concept;
  }

  @Override
  public List<LabelledResource> getSameReferenceLinks() {
    return this.exactMatch;
  }

  @Override
  public void setSameReferenceLinks(List<LabelledResource> uris) {
    this.exactMatch = uris;
  }
}
