package eu.europeana.entitymanagement.web.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

@JacksonXmlRootElement(localName = XmlConstants.XML_PLACE)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.DEPICTION, XmlConstants.PREF_LABEL, XmlConstants.ALT_LABEL, XmlConstants.XML_SKOS_HIDDEN_LABEL,
    	XmlConstants.XML_WGS84_POS_LAT, XmlConstants.XML_WGS84_POS_LONG, XmlConstants.XML_WGS84_POS_ALT, XmlConstants.NOTE,
    	XmlConstants.XML_HAS_PART, XmlConstants.XML_IS_PART_OF, XmlConstants.XML_IS_NEXT_IN_SEQUENCE, 
    	XmlConstants.XML_SAME_AS, XmlConstants.IS_AGGREGATED_BY})
public class XmlPlaceImpl extends XmlBaseEntityImpl {
    
    	public XmlPlaceImpl(Place place) {
    	    	super(place);
    	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return getPlace().getLatitude();
	}

	@JacksonXmlProperty(localName = XmlConstants.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return getPlace().getLongitude();
	}

	@JacksonXmlProperty(localName = XmlConstants.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return getPlace().getAltitude();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_SKOS_HIDDEN_LABEL)
	public List<XmlMultilingualString> getHiddenLabel() {
		return RdfXmlUtils.convertToXmlMultilingualString(getPlace().getHiddenLabel());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.NOTE)
	public List<XmlMultilingualString> getNote() {
		return RdfXmlUtils.convertToXmlMultilingualString(getPlace().getNote());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_HAS_PART)
	public List<RdfResource> getHasPart() {
	    	return RdfXmlUtils.convertToRdfResource(getPlace().getHasPart());
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName  = XmlConstants.XML_IS_PART_OF)
	public List<RdfResource> getIsPartOf() {
	    	return RdfXmlUtils.convertToRdfResource(getPlace().getIsPartOfArray());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_IS_NEXT_IN_SEQUENCE)
	public List<RdfResource> getIsNextInSequence() {
		return RdfXmlUtils.convertToRdfResource(getPlace().getIsNextInSequence());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_SAME_AS)
	public List<RdfResource> getSameAs(){
	    	return RdfXmlUtils.convertToRdfResource(getPlace().getSameAs());
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
    	
}
