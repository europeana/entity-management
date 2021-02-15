package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = XmlFields.XML_EDM_AGENT)
public class AgentImpl extends BaseEntity implements Agent, eu.europeana.corelib.definitions.edm.entity.Agent {

    //TODO: fix cardinality, change to list 
	private Date date; // format "YYYY"
	private String[] begin; // format "YYYY-MM-DD"
	private String[] end; // format "YYYY-MM-DD"
	private String[] dateOfBirth; // format "YYYY-MM-DD"
	private String[] dateOfDeath; // format "YYYY"
	private String[] hasMet;
	private Map<String, String> name;
	private Map<String, List<String>> biographicalInformation;
	private Map<String, List<String>> professionOrOccupation;
	private Map<String, List<String>> placeOfBirth;
	private Map<String, List<String>> placeOfDeath;

	private String dateOfEstablishment; // format "YYYY"
	private String dateOfTermination; // format "YYYY"
	private String gender;
	
	private String[] exactMatch;

	private Map<String, List<String>> tmpBegin;
	private Map<String, List<String>> tmpEnd;
	private Map<String, List<String>> tmpDateOfBirth;
	private Map<String, List<String>> tmpDateOfDeath;
	private Map<String, List<String>> tmpGender;
	private Map<String, List<String>> tmpDateOfEstablishment;
	private Map<String, List<String>> tmpDateOfTermination;
	private Map<String, List<String>> tmpName;
	private Map<String, List<String>> tmpIdentifier;

	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonProperty(WebEntityFields.BEGIN)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_BEGIN)
	public String[] getBeginArray() {
		return begin;
	}

	public void setBegin(String[] begin) {
		this.begin = begin;
	}

	@JsonProperty(WebEntityFields.END)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_END)
	public String[] getEndArray() {
		return end;
	}

	public void setEnd(String[] end) {
		this.end = end;
	}

	@JsonProperty(WebEntityFields.HAS_MET)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_HASMET)
	public String[] getHasMet() {
		return hasMet;
	}

	public void setHasMet(String[] hasMet) {
		this.hasMet = hasMet;
	}

	@JsonProperty(WebEntityFields.NAME)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_NAME)
	public Map<String, String> getName() {
		return name;
	}

	public void setName(Map<String, String> name) {
		this.name = name;
	}

	@JsonProperty(WebEntityFields.BIOGRAPHICAL_INFORMATION)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_BIOGRAPHICAL_INFORMATION)
	public Map<String, List<String>> getBiographicalInformation() {
		return biographicalInformation;
	}

	public void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		this.biographicalInformation = biographicalInformation;
	}

	@JsonProperty(WebEntityFields.DATE_OF_BIRTH)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_DATE_OF_BIRTH)
	public String[] getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String[] dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@JsonProperty(WebEntityFields.DATE_OF_DEATH)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_DATE_OF_DEATH)
	public String[] getDateOfDeath() {
		return dateOfDeath;
	}

	public void setDateOfDeath(String[] dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}

	@JsonProperty(WebEntityFields.PLACE_OF_BIRTH)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_PLACE_OF_BIRTH)
	public Map<String, List<String>> getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(Map<String, List<String>> placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	@JsonProperty(WebEntityFields.PLACE_OF_DEATH)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_PLACE_OF_DEATH)
	public Map<String, List<String>> getPlaceOfDeath() {
		return placeOfDeath;
	}

	public void setPlaceOfDeath(Map<String, List<String>> placeOfDeath) {
		this.placeOfDeath = placeOfDeath;
	}

	@JsonProperty(WebEntityFields.DATE_OF_ESTABLISHMENT)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_DATE_OF_ESTABLISHMENT)
	public String getDateOfEstablishment() {
		return dateOfEstablishment;
	}

	public void setDateOfEstablishment(String dateOfEstablishment) {
		this.dateOfEstablishment = dateOfEstablishment;
	}

	@JsonProperty(WebEntityFields.DATE_OF_TERMINATION)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_DATE_OF_TERMINATION)
	public String getDateOfTermination() {
		return dateOfTermination;
	}

	public void setDateOfTermination(String dateOfTermination) {
		this.dateOfTermination = dateOfTermination;
	}

	@JsonProperty(WebEntityFields.GENDER)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_GENDER)
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@JsonProperty(WebEntityFields.PROFESSION_OR_OCCUPATION)
	@JacksonXmlProperty(localName = XmlFields.XML_RDAGR2_PROFESSION_OR_OCCUPATION)
	public Map<String, List<String>> getProfessionOrOccupation() {
		return professionOrOccupation;
	}

	public void setProfessionOrOccupation(Map<String, List<String>> professionOrOccupation) {
		this.professionOrOccupation = professionOrOccupation;
	}

	public String[] getExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}	

	@Override
	public Map<String, List<String>> getBegin() {
		//if not available
		if (getBeginArray() == null)
			return null;
		//if not transformed
		if (tmpBegin == null) 
			tmpBegin = fillTmpMap(Arrays.asList(getBeginArray()));

		return tmpBegin;
	}

	@Override
	public Map<String, List<String>> getEnd() {
		//if not available
		if (getEndArray() == null)
			return null;
		//if not transformed
		if (tmpEnd == null) 
			tmpEnd = fillTmpMap(Arrays.asList(getEndArray()));

		return tmpEnd;
	}

	@Override
	@Deprecated
	public void setBegin(Map<String, List<String>> begin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public void setEnd(Map<String, List<String>> end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated  
	public void setEdmWasPresentAt(String[] edmWasPresentAt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated  
	public String[] getEdmWasPresentAt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated  
	public void setEdmHasMet(Map<String, List<String>> edmHasMet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated  
	public Map<String, List<String>> getEdmHasMet() {
		// TODO Auto-generated method stub
//		getHasMet();
		return null;
	}

	@Override
	@Deprecated  
	public void setEdmIsRelatedTo(Map<String, List<String>> edmIsRelatedTo) {
		// TODO Auto-generated method stub		
	}

	@Override
	@Deprecated  
	public Map<String, List<String>> getEdmIsRelatedTo() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	@Deprecated  
	public void setFoafName(Map<String, List<String>> foafName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public Map<String, List<String>> getFoafName() {
		//if not available
		if (getName() == null)
			return null;
		//if not transformed
		if (tmpName == null) {
			tmpName = fillTmpMapToMap(getName());
		}

		return tmpName;
	}

	@Override
	@Deprecated
	public void setDcDate(Map<String, List<String>> dcDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getDcDate() {
		return null;
	}

	@Override
	@Deprecated
	public void setDcIdentifier(Map<String, List<String>> dcIdentifier) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * see {@link #getIdentifier()}
	 */
	@Override
	@Deprecated  
	public Map<String, List<String>> getDcIdentifier() {
		//if not available
		if (getIdentifier() == null)
			return null;
		//if not transformed
		if (tmpIdentifier == null) 
			tmpIdentifier = fillTmpMap(Arrays.asList(getIdentifier()));

		return tmpIdentifier;
	}

	@Override
	@Deprecated  
	public void setRdaGr2DateOfBirth(Map<String, List<String>> rdaGr2DateOfBirth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2DateOfBirth() {
		//if not available
		if (getDateOfBirth() == null)
			return null;
		//if not transformed
		if (tmpDateOfBirth == null) 
			tmpDateOfBirth = fillTmpMap(Arrays.asList(getDateOfBirth()));

		return tmpDateOfBirth;
	}

	@Override
	@Deprecated
	public void setRdaGr2PlaceOfDeath(Map<String, List<String>> rdaGr2PlaceOfDeath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2PlaceOfDeath() {
		return getPlaceOfDeath();
	}

	@Override
	@Deprecated
	public void setRdaGr2PlaceOfBirth(Map<String, List<String>> rdaGr2PlaceOfBirth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2PlaceOfBirth() {
		return getPlaceOfBirth();
	}

	@Override
	@Deprecated
	public void setRdaGr2DateOfDeath(Map<String, List<String>> rdaGr2DateOfDeath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2DateOfDeath() {
		//if not available
		if (getDateOfDeath() == null)
			return null;
		//if not transformed
		if (tmpDateOfDeath == null) 
			tmpDateOfDeath = fillTmpMap(Arrays.asList(getDateOfDeath()));

		return tmpDateOfDeath;
	}

	@Override
	@Deprecated
	public void setRdaGr2DateOfEstablishment(Map<String, List<String>> rdaGr2DateOfEstablishment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public Map<String, List<String>> getRdaGr2DateOfEstablishment() {
		//if not available
		if (getDateOfEstablishment() == null)
			return null;
		//if not transformed
		if (tmpDateOfEstablishment == null) {
			tmpDateOfEstablishment = fillTmpMap(Arrays.asList(getDateOfEstablishment()));
		}

		return tmpDateOfEstablishment;
	}

	@Override
	@Deprecated
	public void setRdaGr2DateOfTermination(Map<String, List<String>> rdaGr2DateOfTermination) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public Map<String, List<String>> getRdaGr2DateOfTermination() {
		//if not available
		if (getDateOfTermination() == null)
			return null;
		//if not transformed
		if (tmpDateOfTermination == null) {
			tmpDateOfTermination = fillTmpMap(Arrays.asList(getDateOfTermination()));
		}

		return tmpDateOfTermination;
	}

	@Override
	@Deprecated
	public void setRdaGr2Gender(Map<String, List<String>> rdaGr2Gender) {

	}

	@Override
	public Map<String, List<String>> getRdaGr2Gender() {
		//if not available
		if (getGender() == null)
			return null;
		//if not transformed
		if (tmpGender == null) 
			tmpGender = fillTmpMap(Arrays.asList(getGender()));

		return tmpGender;
	}

	@Override
	@Deprecated
	public void setRdaGr2ProfessionOrOccupation(Map<String, List<String>> rdaGr2ProfessionOrOccupation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2ProfessionOrOccupation() {
		return getProfessionOrOccupation();
	}

	@Override
	@Deprecated
	public void setRdaGr2BiographicalInformation(Map<String, List<String>> rdaGr2BiographicalInformation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getRdaGr2BiographicalInformation() {
		return getBiographicalInformation();
	}

	@Override
	public String getInternalType() {
		return "Agent";
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
