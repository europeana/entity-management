package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_TIMESPAN;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

//@JacksonXmlRootElement(localName = XmlConstants.XML_TIMESPAN)
@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_TIMESPAN)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.HIDDEN_LABEL,
    	XmlConstants.XML_BEGIN,XmlConstants.XML_END,XmlConstants.XML_SKOS_NOTE, XmlConstants.XML_HAS_PART, XmlConstants.XML_IS_PART_OF, XmlConstants.XML_IS_NEXT_IN_SEQUENCE, XmlConstants.XML_SAME_AS,
    	XmlConstants.XML_EDM_WEB_RESOURCE})
public class XmlTimespanImpl extends XmlBaseEntityImpl {
        
        private List<LabelledResource> hiddenLabel = new ArrayList<>();
        private String[] begin;
        private String[] end;
        private List<LabelledResource> note = new ArrayList<>();
        private List<LabelledResource> hasPart = new ArrayList<>();
        private List<LabelledResource> isPartOf = new ArrayList<>();
        private String[] isNextInSequence;
        
    	public XmlTimespanImpl(Timespan timespan) {
    	    super(timespan);
    	    this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(timespan.getHiddenLabel());
    	    if(timespan.getBeginString() != null) {
                this.begin = new String[] {timespan.getBeginString()};    
            }
            if(timespan.getEndString() != null) {
                this.end = new String[] {timespan.getEndString()};    
            }
            this.note = RdfXmlUtils.convertToXmlMultilingualString(timespan.getNote());
            this.hasPart = RdfXmlUtils.convertToRdfResource(timespan.getHasPart());
            this.isPartOf = RdfXmlUtils.convertToRdfResource(timespan.getIsPartOfArray());
            this.isNextInSequence = timespan.getIsNextInSequence();
            
    	}
            

	public XmlTimespanImpl() {
		// default constructor
	}
	
	public Entity toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            Timespan timespan = (Timespan) getEntity(); 
            
            timespan.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
            if(getBegin() != null && getBegin().length > 0) {
                timespan.setBeginString(getBegin()[0]);
            }
            if(getEnd() != null && getEnd().length > 0) {
                timespan.setEndString(getEnd()[0]);
            }
            
            timespan.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
            timespan.setHasPart(RdfXmlUtils.toStringArray(getHasPart()));
            timespan.setIsPartOfArray(RdfXmlUtils.toStringArray(getIsPartOf()));
            timespan.setIsNextInSequence(getIsNextInSequence());
            return timespan;
        }

	@XmlElement(name = XmlConstants.XML_IS_PART_OF)
	public List<LabelledResource> getIsPartOf() {
	    	return isPartOf;
	}
	
	@XmlElement(name =  XmlConstants.XML_BEGIN)
	public String[] getBegin() {
	    	return begin;
	}

	@XmlElement(name =  XmlConstants.XML_END)
	public String[] getEnd() {
	    	return end;
	}
	
	@XmlElement(name = NOTE, namespace = NAMESPACE_SKOS)
        public List<LabelledResource> getNote() {
                return note;
        }
	
	@XmlElement(name = XML_HAS_PART)
        public List<LabelledResource> getHasPart() {
                return hasPart;
        }
	
	@XmlElement(name =  XmlConstants.HIDDEN_LABEL)
	public List<LabelledResource> getHiddenLabel() {
		return hiddenLabel;
	}

	@XmlElement(name =  XmlConstants.XML_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
	    	return isNextInSequence;
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Timespan;
	}

	
}
