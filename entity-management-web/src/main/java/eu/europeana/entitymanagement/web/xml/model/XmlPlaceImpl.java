package eu.europeana.entitymanagement.web.xml.model;

import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_HAS_PART;
import static eu.europeana.entitymanagement.web.xml.model.XmlConstants.XML_IS_PART_OF;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = XmlConstants.XML_PLACE)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.HIDDEN_LABEL,
    	XmlConstants.XML_WGS84_POS_LAT, XmlConstants.XML_WGS84_POS_LONG, XmlConstants.XML_WGS84_POS_ALT, XmlConstants.NOTE,
    	XmlConstants.XML_HAS_PART, XmlConstants.XML_IS_PART_OF, XmlConstants.XML_IS_NEXT_IN_SEQUENCE, 
    	XmlConstants.XML_SAME_AS, XmlConstants.IS_AGGREGATED_BY})
public class XmlPlaceImpl extends XmlBaseEntityImpl {
    
    
    	Float latitude, longitude, altitude;
	
    	private List<LabelledResource> hiddenLabel = new ArrayList<>();
    	private List<LabelledResource> note = new ArrayList<>();
    	private List<LabelledResource> hasPart = new ArrayList<>();
	private List<LabelledResource> isRelatedTo = new ArrayList<>();
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

	@XmlElement(name = XmlConstants.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	@XmlElement(name = XmlConstants.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	@XmlElement(name = XmlConstants.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}
	
	@XmlElement(name = XmlConstants.HIDDEN_LABEL)
	public List<LabelledResource> getHiddenLabel() {
		return hiddenLabel;
	}
	
	@XmlElement(name = XmlConstants.NOTE)
	public List<LabelledResource> getNote() {
		return note;
	}
	
	
	@XmlElement(name = XML_HAS_PART)
	public List<LabelledResource> getHasPart() {
	    	return hasPart;
	}

	@XmlElement(name = XML_IS_PART_OF)
	public List<LabelledResource> getIsPartOf() {
	    	return isPartOf;
	}

	@XmlElement(name =  XmlConstants.XML_IS_NEXT_IN_SEQUENCE)
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

	@XmlElement(name =  XmlConstants.XML_IS_RELATED_TO)
	public List<LabelledResource> getIsRelatedTo() {
	    return isRelatedTo;
	}
    	
}
