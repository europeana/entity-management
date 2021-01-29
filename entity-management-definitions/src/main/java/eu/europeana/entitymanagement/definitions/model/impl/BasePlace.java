package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class BasePlace extends BaseEntity implements Place, eu.europeana.corelib.definitions.edm.entity.Place {

	private String[] isNextInSequence;
	private Float latitude, longitude, altitude;
	private String[] exactMatch;

	private Map<String, List<String>> tmpIsPartOf;	
	private Map<String, List<String>> tmpHasPart;	
	
	@Override
	@JsonProperty(WebEntityFields.IS_NEXT_IN_SEQUENCE)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_NEXT_IN_SEQUENCE)
	public String[] getIsNextInSequence() {
		return isNextInSequence;
	}

	@Override
	public void setIsNextInSequence(String[] isNextInSequence) {
		this.isNextInSequence = isNextInSequence;
	}

	@Override
	@JsonProperty(WebEntityFields.LATITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LAT)
	public Float getLatitude() {
		return latitude;
	}

	@Override
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	@Override
	@JsonProperty(WebEntityFields.LONGITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_LONG)
	public Float getLongitude() {
		return longitude;
	}

	@Override
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	@Override
	@JsonProperty(WebEntityFields.ALTITUDE)
	@JacksonXmlProperty(localName = XmlFields.XML_WGS84_POS_ALT)
	public Float getAltitude() {
		return altitude;
	}

	@Override
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
	public void setDcTermsHasPart(Map<String, List<String>> dcTermsHasPart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@JsonProperty(WebEntityFields.HAS_PART)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_HAS_PART)
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
	@JsonProperty(WebEntityFields.IS_PART_OF)
	@JacksonXmlProperty(localName  = XmlFields.XML_DCTERMS_IS_PART_OF)
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
	public String getInternalType() {
		return "Place";
	}
}
