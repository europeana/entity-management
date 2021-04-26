package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.ALT_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.DEPICTION;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.HIDDEN_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.IS_AGGREGATED_BY;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NAMESPACE_EDM;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.*;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.NOTE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.PREF_LABEL;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_NEXT_IN_SEQUENCE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_PLACE;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_SAME_AS;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_ALT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_LAT;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_WGS84_POS_LONG;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

//@JacksonXmlRootElement(localName = XML_PLACE)
@XmlRootElement(namespace = NAMESPACE_EDM, name = XML_PLACE)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ DEPICTION, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL, XML_WGS84_POS_LAT, XML_WGS84_POS_LONG,
        XML_WGS84_POS_ALT, NOTE, XML_HAS_PART, XML_IS_PART_OF, XML_IS_NEXT_IN_SEQUENCE, XML_SAME_AS, IS_AGGREGATED_BY })
public class XmlPlaceImpl extends XmlBaseEntityImpl {
    
    
    	Float latitude, longitude, altitude;
	
    	private List<LabelledResource> hiddenLabel = new ArrayList<>();
    	private List<LabelledResource> note = new ArrayList<>();
    	private List<LabelledResource> hasPart = new ArrayList<>();
    	private List<LabelledResource> isPartOf = new ArrayList<>();
        private String[] isNextInSequence;
	
        public XmlPlaceImpl(Place place) {
            super(place);
            this.latitude = place.getLatitude();
            this.longitude = place.getLongitude();
            this.altitude = place.getAltitude();
            this.hiddenLabel = RdfXmlUtils.convertToXmlMultilingualString(place.getHiddenLabel());
            this.note = RdfXmlUtils.convertToXmlMultilingualString(place.getNote());
            this.hasPart = RdfXmlUtils.convertToRdfResource(place.getHasPart());
            this.isPartOf = RdfXmlUtils.convertToRdfResource(place.getIsPartOfArray());
            this.isNextInSequence = place.getIsNextInSequence();
        }
	
	public XmlPlaceImpl() {
		// default constructor
	    System.out.println();
	}
	
	public Entity toEntityModel() throws EntityCreationException {
            super.toEntityModel();
            Place place = (Place) getEntity(); 
            
            place.setLatitude(getLatitude());
            place.setLongitude(getLongitude());
            place.setAltitude(getAltitude());
            place.setHiddenLabel(RdfXmlUtils.toLanguageMapList(getHiddenLabel()));
            place.setNote(RdfXmlUtils.toLanguageMapList(getNote()));
            place.setHasPart(RdfXmlUtils.toStringArray(getHasPart()));
            place.setIsPartOfArray(RdfXmlUtils.toStringArray(getIsPartOf()));
            place.setIsNextInSequence(getIsNextInSequence());
            
            return place;
        }

	@XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	@XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	@XmlElement(namespace = NAMESPACE_WGS84_POS, name = XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}
	
	@XmlElement(namespace = NAMESPACE_SKOS, name = HIDDEN_LABEL)
	public List<LabelledResource> getHiddenLabel() {
		return hiddenLabel;
	}
	
	@XmlElement(namespace = NAMESPACE_SKOS, name = NOTE)
	public List<LabelledResource> getNote() {
		return note;
	}
	
	
	@XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_HAS_PART)
	public List<LabelledResource> getHasPart() {
	    	return hasPart;
	}

	@XmlElement(namespace = NAMESPACE_DC_TERMS, name = XML_IS_PART_OF)
	public List<LabelledResource> getIsPartOf() {
	    	return isPartOf;
	}

	@XmlElement(namespace = NAMESPACE_EDM, name =  XML_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
		return isNextInSequence;
	}
	
	@JsonIgnore
	private Place getPlace() {
	    return (Place)entity;
	}

	@Override
	@JsonIgnore
	protected EntityTypes getTypeEnum() {
	    return EntityTypes.Place;
	}

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
    	
}
