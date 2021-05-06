package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import eu.europeana.entitymanagement.definitions.model.Entity;
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
public class Place extends Entity {

	public Place() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String[] isNextInSequence;
	private Float latitude, longitude, altitude;
	private String[] exactMatch;

	private Map<String, List<String>> tmpIsPartOf;
	private Map<String, List<String>> tmpHasPart;

	
	@JsonGetter(IS_NEXT_IN_SEQUENCE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
		return isNextInSequence;
	}

	
	@JsonSetter(IS_NEXT_IN_SEQUENCE)
	public void setIsNextInSequence(String[] isNextInSequence) {
		this.isNextInSequence = isNextInSequence;
	}

	
	@JsonGetter(LATITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	
	@JsonSetter(LATITUDE)
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	
	@JsonGetter(LONGITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	
	@JsonSetter(LONGITUDE)
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	
	@JsonGetter(ALTITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}

	
	@JsonSetter(ALTITUDE)
	public void setAltitude(Float altitude) {
		this.altitude = altitude;
	}

	
	public String[] getExactMatch() {
		return exactMatch;
	}

	
	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}


	




	


	


	


	


	
	public String getType() {
		return EntityTypes.Place.getEntityType();
	}


	
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

}
