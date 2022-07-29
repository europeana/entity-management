package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ABOUT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_SHOWN_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC_TERMS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_BEGIN;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_END;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_TIMESPAN;

import eu.europeana.entitymanagement.definitions.exceptions.EntityModelCreationException;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_TIMESPAN)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    propOrder = {
      ABOUT,
      DEPICTION,
      IS_SHOWN_BY,
      PREF_LABEL,
      ALT_LABEL,
      HIDDEN_LABEL,
      XML_BEGIN,
      XML_END,
      NOTE,
      XML_HAS_PART,
      XML_IS_PART_OF,
      XML_IS_NEXT_IN_SEQUENCE,
      XML_SAME_AS,
      IS_AGGREGATED_BY
    })
public class XmlTimeSpanImpl extends XmlBaseEntityImpl<TimeSpan> {

  @XmlElement(namespace = XmlConstants.NAMESPACE_OWL, name = XmlConstants.XML_SAME_AS)
  private List<LabelledResource> sameAs;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_BEGIN)
  private String begin;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_END)
  private String end;

  @XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
  private List<LabelledResource> note;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_HAS_PART)
  private List<LabelledResource> hasPart;

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_IS_PART_OF)
  private List<LabelledResource> isPartOf;

  @XmlElement(namespace = NAMESPACE_EDM, name = XML_IS_NEXT_IN_SEQUENCE)
  private List<LabelledResource> isNextInSequence;

  public XmlTimeSpanImpl(TimeSpan timespan) {
    super(timespan);
    this.sameAs = RdfXmlUtils.convertToRdfResource(timespan.getSameReferenceLinks());
    this.begin = timespan.getBeginString();
    this.end = timespan.getEndString();
    this.note = RdfXmlUtils.convertToXmlMultilingualString(timespan.getNote());
    this.hasPart = RdfXmlUtils.convertToRdfResource(timespan.getHasPart());
    this.isPartOf = RdfXmlUtils.convertToRdfResource(timespan.getIsPartOfArray());
    this.isNextInSequence = RdfXmlUtils.convertToRdfResource(timespan.getIsNextInSequence());
  }

  public XmlTimeSpanImpl() {
    // default constructor
  }

  @Override
  public TimeSpan toEntityModel() throws EntityModelCreationException {
    super.toEntityModel();
    entity.setBeginString(begin);
    entity.setEndString(end);
    entity.setNote(RdfXmlUtils.toLanguageMapList(note));
    entity.setHasPart(RdfXmlUtils.toStringList(hasPart));
    entity.setIsPartOfArray(RdfXmlUtils.toStringList(isPartOf));
    entity.setIsNextInSequence(RdfXmlUtils.toStringList(isNextInSequence));
    return entity;
  }

  public String getBegin() {
    return begin;
  }

  public String getEnd() {
    return end;
  }

  public List<LabelledResource> getNote() {
    return note;
  }

  public List<LabelledResource> getHasPart() {
    return hasPart;
  }

  public List<LabelledResource> getIsPartOf() {
    return isPartOf;
  }

  public List<LabelledResource> getIsNextInSequence() {
    return isNextInSequence;
  }

  @Override
  protected EntityTypes getTypeEnum() {
    return EntityTypes.TimeSpan;
  }

  @Override
  public List<LabelledResource> getSameReferenceLinks() {
    return this.sameAs;
  }

  @Override
  public void setSameReferenceLinks(List<LabelledResource> uris) {
    this.sameAs = uris;
  }
}
