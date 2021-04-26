package eu.europeana.entitymanagement.definitions.model.impl;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, IS_SHOWN_BY, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL, LATITUDE, LONGITUDE,
ALTITUDE, LATITUDE_LONGITUDE, NOTE, HAS_PART, IS_PART_OF, IS_NEXT_IN_SEQUENCE, SAME_AS})
public class PlaceImpl extends BaseEntity implements Place, eu.europeana.corelib.definitions.edm.entity.Place {

	public PlaceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String[] isNextInSequence;
	private Float latitude, longitude, altitude;
	private String[] exactMatch;

	private Map<String, List<String>> tmpIsPartOf;	
	private Map<String, List<String>> tmpHasPart;	
	
	@Override
	@JsonGetter(IS_NEXT_IN_SEQUENCE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
		return isNextInSequence;
	}

	@Override
	@JsonSetter(IS_NEXT_IN_SEQUENCE)
	public void setIsNextInSequence(String[] isNextInSequence) {
		this.isNextInSequence = isNextInSequence;
	}

	@Override
	@JsonGetter(LATITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	@Override
	@JsonSetter(LATITUDE)
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	@Override
	@JsonGetter(LONGITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	@Override
	@JsonSetter(LONGITUDE)
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	@Override
	@JsonGetter(ALTITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}

	@Override
	@JsonSetter(ALTITUDE)
	public void setAltitude(Float altitude) {
		this.altitude = altitude;
	}

	@Override
	public String[] getExactMatch() {
		return exactMatch;
	}

	@Override
	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}

	
	@Override
	@Deprecated
	@JsonIgnore
	public void setIsPartOf(Map<String, List<String>> isPartOf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public void setPosition(Map<String, Float> position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	@JsonIgnore
        public Map<String, Float> getPosition() {
		Map<String, Float> positionMap = new HashMap<String,Float>();	
		if (getLatitude() != null)
			positionMap.put("LATITUDE", getLatitude());
		if (getLongitude() != null)
			positionMap.put("LONGITUDE", getLongitude());
		return positionMap;	
	}

	@Override
	@Deprecated
	@JsonIgnore
	public void setDcTermsHasPart(Map<String, List<String>> dcTermsHasPart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@JsonIgnore
	public Map<String, List<String>> getDcTermsHasPart() {
		//if not available
		if (getHasPart() == null)
			return null;
		//if not transformed
		if (tmpHasPart == null) 
			tmpHasPart = fillTmpMap(Arrays.asList(getHasPart()));
			
		return tmpHasPart;
	}

	@Override
	@JsonIgnore
	public Map<String, List<String>> getIsPartOf() {
		//if not available
		if (getIsPartOfArray() == null)
			return null;
		//if not transformed
		if (tmpIsPartOf == null) 
			tmpIsPartOf = fillTmpMap(Arrays.asList(getIsPartOfArray()));

		return tmpIsPartOf;
	}

	@Override
    public String getType() {
		return EntityTypes.Place.getEntityType();
    }

	
	@Override
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	@Override
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

}
