package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;

import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_TIMESPAN)
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTimespanImpl extends XmlBaseEntityImpl<Timespan> {

  @XmlElement(namespace = NAMESPACE_SKOS, name =  HIDDEN_LABEL)
  private List<LabelledResource> hiddenLabel = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name =  XML_BEGIN)
  private String begin;

  @XmlElement(namespace = NAMESPACE_EDM, name =  XML_END)
  private String end;

  @XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
  private List<LabelledResource> note = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_HAS_PART)
  private List<LabelledResource> hasPart = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_IS_PART_OF)
  private List<LabelledResource> isPartOf = new ArrayList<>();

  @XmlElement(namespace = NAMESPACE_EDM, name =  XML_IS_NEXT_IN_SEQUENCE)
  private List<LabelledResource> isNextInSequence;
        
    	public XmlTimespanImpl(Timespan timespan) {
    	    super(timespan);
    	    this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(timespan.getHiddenLabel());
                this.begin = timespan.getBeginString();
                this.end = timespan.getEndString();
            this.note = RdfXmlUtils.convertToXmlMultilingualString(timespan.getNote());
            this.hasPart = RdfXmlUtils.convertToRdfResource(timespan.getHasPart());
            this.isPartOf = RdfXmlUtils.convertToRdfResource(timespan.getIsPartOfArray());
            this.isNextInSequence = RdfXmlUtils.convertToRdfResource(timespan.getIsNextInSequence());
            
    	}
            

	public XmlTimespanImpl() {
		// default constructor
	}
	
	public Timespan toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            entity.setHiddenLabel(RdfXmlUtils.toLanguageMapList(hiddenLabel));
            entity.setBeginString(begin);
            entity.setEndString(end);
            entity.setNote(RdfXmlUtils.toLanguageMapList(note));
            entity.setHasPart(RdfXmlUtils.toStringList(hasPart));
            entity.setIsPartOfArray(RdfXmlUtils.toStringList(isPartOf));
            entity.setIsNextInSequence(RdfXmlUtils.toStringList(isNextInSequence));
            return entity;
        }

  public List<LabelledResource> getHiddenLabel() {
    return hiddenLabel;
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
	    return EntityTypes.Timespan;
	}
}
