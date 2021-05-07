package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.vocabulary.XmlFields.XML_SKOS_NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_DC_TERMS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_SKOS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_BEGIN;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_EDM_WEB_RESOURCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_END;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
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

@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_TIMESPAN)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ DEPICTION, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL, XML_BEGIN, XML_END, XML_SKOS_NOTE, XML_HAS_PART,
        XML_IS_PART_OF, XML_IS_NEXT_IN_SEQUENCE, XML_SAME_AS, XML_EDM_WEB_RESOURCE })
public class XmlTimespanImpl extends XmlBaseEntityImpl<Timespan> {
        
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
	
	public Timespan toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            entity.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
            if(getBegin() != null && getBegin().length > 0) {
                entity.setBeginString(getBegin()[0]);
            }
            if(getEnd() != null && getEnd().length > 0) {
                entity.setEndString(getEnd()[0]);
            }
            
            entity.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
            entity.setHasPart(RdfXmlUtils.toStringArray(getHasPart()));
            entity.setIsPartOfArray(RdfXmlUtils.toStringArray(getIsPartOf()));
            entity.setIsNextInSequence(getIsNextInSequence());
            return entity;
        }

	@XmlElement(namespace = NAMESPACE_SKOS, name =  HIDDEN_LABEL)
        public List<LabelledResource> getHiddenLabel() {
                return hiddenLabel;
        }
	
	@XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_IS_PART_OF)
	public List<LabelledResource> getIsPartOf() {
	    	return isPartOf;
	}
	
	@XmlElement(namespace = NAMESPACE_EDM, name =  XML_BEGIN)
	public String[] getBegin() {
	    	return begin;
	}

	@XmlElement(namespace = NAMESPACE_EDM, name =  XML_END)
	public String[] getEnd() {
	    	return end;
	}
	
	@XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
        public List<LabelledResource> getNote() {
                return note;
        }
	
	@XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_HAS_PART)
        public List<LabelledResource> getHasPart() {
                return hasPart;
        }
	

	@XmlElement(namespace = NAMESPACE_EDM, name =  XML_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
	    	return isNextInSequence;
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Timespan;
	}

	
}
