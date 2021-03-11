package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class PlaceImpl extends BaseEntity implements Place, eu.europeana.corelib.definitions.edm.entity.Place {

	private String[] isNextInSequence;
	private Float latitude, longitude, altitude;
	private String[] exactMatch;

	private Map<String, List<String>> tmpIsPartOf;	
	private Map<String, List<String>> tmpHasPart;	
	
	@Override
	@JsonGetter(WebEntityFields.IS_NEXT_IN_SEQUENCE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
		return isNextInSequence;
	}

	@Override
	@JsonSetter(WebEntityFields.IS_NEXT_IN_SEQUENCE)
	public void setIsNextInSequence(String[] isNextInSequence) {
		this.isNextInSequence = isNextInSequence;
	}

	@Override
	@JsonGetter(WebEntityFields.LATITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	@Override
	@JsonSetter(WebEntityFields.LATITUDE)
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	@Override
	@JsonGetter(WebEntityFields.LONGITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	@Override
	@JsonSetter(WebEntityFields.LONGITUDE)
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	@Override
	@JsonGetter(WebEntityFields.ALTITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}

	@Override
	@JsonSetter(WebEntityFields.ALTITUDE)
	public void setAltitude(Float altitude) {
		this.altitude = altitude;
	}

	public String[] getExactMatch() {
		return exactMatch;
	}

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
		return "Place";
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
