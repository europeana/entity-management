package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, IS_SHOWN_BY, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL, NAME, BEGIN, DATE_OF_BIRTH, DATE_OF_ESTABLISHMENT,
		END, DATE_OF_DEATH, DATE_OF_TERMINATION, DATE, PLACE_OF_BIRTH, PLACE_OF_DEATH, GENDER, PROFESSION_OR_OCCUPATION, BIOGRAPHICAL_INFORMATION, NOTE,
		HAS_PART, IS_PART_OF, HAS_MET, IS_RELATED_TO, WAS_PRESENT_AT, IDENTIFIER, SAME_AS})
public class Agent extends Entity {

	public Agent(Agent copy) {
		super(copy);
		if(copy.getDate()!=null) this.date = new ArrayList<>(copy.getDate());
		if(copy.getBegin()!=null) this.begin = new ArrayList<>(copy.getBegin());
		if(copy.getEnd()!=null) this.end = new ArrayList<>(copy.getEnd());
		if(copy.getDateOfBirth()!=null) this.dateOfBirth = new ArrayList<>(copy.getDateOfBirth());
		if(copy.getDateOfDeath()!=null) this.dateOfDeath = new ArrayList<>(copy.dateOfDeath);
		if(copy.getWasPresentAt()!=null) this.wasPresentAt = new ArrayList<>(copy.getWasPresentAt());
		if(copy.getHasMet()!=null) this.hasMet = new ArrayList<>(copy.getHasMet());
		if(copy.getName()!=null) this.name = new HashMap<>(copy.getName());
		if(copy.getBiographicalInformation()!=null) this.biographicalInformation = new HashMap<>(copy.getBiographicalInformation());
		if(copy.getProfessionOrOccupation()!=null) this.professionOrOccupation = new ArrayList<>(copy.getProfessionOrOccupation());
		if(copy.getPlaceOfBirth()!=null) this.placeOfBirth = new ArrayList<>(copy.getPlaceOfBirth());
		if(copy.getPlaceOfDeath()!=null) this.placeOfDeath = new ArrayList<>(copy.getPlaceOfDeath());
		this.dateOfEstablishment = copy.getDateOfEstablishment();
		this.dateOfTermination = copy.getDateOfTermination();
		this.gender = copy.getGender();
		this.sameAs = copy.sameAs;
	}

	public Agent() {
		super();
	}

	// TODO: fix cardinality, change to list
	private List<String> date; // format "YYYY"
	private List<String> begin; // format "YYYY-MM-DD"
	private List<String> end; // format "YYYY-MM-DD"
	private List<String> dateOfBirth; // format "YYYY-MM-DD"
	private List<String> dateOfDeath; // format "YYYY"
	private List<String> wasPresentAt;
	private List<String> hasMet;
	private Map<String, String> name;
	private Map<String, List<String>> biographicalInformation;
	// TODO: clarify format. Right now Metis returns a list of Resources
	private List<String> professionOrOccupation;
	private List<String> placeOfBirth;
	private List<String> placeOfDeath;

	private List<String> dateOfEstablishment;
	private List<String> dateOfTermination;
	private List<String> gender;

	private List<String> sameAs;

	@JsonGetter(WAS_PRESENT_AT)
	public List<String> getWasPresentAt() {
		return this.wasPresentAt;
	}

	@JsonSetter(WAS_PRESENT_AT)
	public void setWasPresentAt(List<String> wasPresentAt) {
		this.wasPresentAt=wasPresentAt;
	}

	@JsonGetter(DATE)
	public List<String> getDate() {
		return date;
	}
	@JsonSetter(DATE)
	public void setDate(List<String> date) {
		this.date = date;
	}


	@JsonGetter(BEGIN)
	public List<String> getBegin() {
		return begin;
	}

	@JsonSetter(BEGIN)
	public void setBegin(List<String> begin) {
		this.begin = begin;
	}

	@JsonGetter(END)
	public List<String> getEnd() {
		return end;
	}

	@JsonSetter(END)
	public void setEnd(List<String> end) {
		this.end = end;
	}

	@JsonGetter(HAS_MET)
	public List<String> getHasMet() {
		return hasMet;
	}

	@JsonSetter(HAS_MET)
	public void setHasMet(List<String> hasMet) {
		this.hasMet = hasMet;
	}

	@JsonGetter(NAME)
	public Map<String, String> getName() {
		return name;
	}

	@JsonSetter(NAME)
	public void setName(Map<String, String> name) {
		this.name = name;
	}

	@JsonGetter(BIOGRAPHICAL_INFORMATION)
	public Map<String, List<String>> getBiographicalInformation() {
		return biographicalInformation;
	}

	@JsonSetter(BIOGRAPHICAL_INFORMATION)
	public void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		this.biographicalInformation = biographicalInformation;
	}

	@JsonGetter(DATE_OF_BIRTH)
	public List<String> getDateOfBirth() {
		return dateOfBirth;
	}

	@JsonSetter(DATE_OF_BIRTH)
	public void setDateOfBirth(List<String> dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@JsonGetter(DATE_OF_DEATH)
	public List<String> getDateOfDeath() {
		return dateOfDeath;
	}

	@JsonSetter(DATE_OF_DEATH)
	public void setDateOfDeath(List<String> dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}

	@JsonGetter(PLACE_OF_BIRTH)
	public List<String> getPlaceOfBirth() {
		return placeOfBirth;
	}

	@JsonSetter(PLACE_OF_BIRTH)
	public void setPlaceOfBirth(List<String> placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}


	@JsonGetter(PLACE_OF_DEATH)
	public List<String> getPlaceOfDeath() {
		return placeOfDeath;
	}

	@JsonSetter(PLACE_OF_DEATH)
	public void setPlaceOfDeath(List<String> placeOfDeath) {
		this.placeOfDeath = placeOfDeath;
	}

	@JsonGetter(DATE_OF_ESTABLISHMENT)
	public List<String> getDateOfEstablishment() {
		return dateOfEstablishment;
	}

	@JsonSetter(DATE_OF_ESTABLISHMENT)
	public void setDateOfEstablishment(List<String> dateOfEstablishment) {
		this.dateOfEstablishment = dateOfEstablishment;
	}

	@JsonGetter(DATE_OF_TERMINATION)
	public List<String> getDateOfTermination() {
		return dateOfTermination;
	}

	@JsonSetter(DATE_OF_TERMINATION)
	public void setDateOfTermination(List<String> dateOfTermination) {
		this.dateOfTermination = dateOfTermination;
	}

	@JsonGetter(GENDER)
	public List<String> getGender() {
		return gender;
	}


	@JsonSetter(GENDER)
	public void setGender(List<String> gender) {
		this.gender = gender;
	}

	@JsonGetter(PROFESSION_OR_OCCUPATION)
	public List<String> getProfessionOrOccupation() {
		return professionOrOccupation;
	}

	@JsonSetter(PROFESSION_OR_OCCUPATION)
	public void setProfessionOrOccupation(List<String> professionOrOccupation) {
		this.professionOrOccupation = professionOrOccupation;
	}


	public String getType() {
		return EntityTypes.Agent.getEntityType();
	}


	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}


	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

	@Override
	@JsonSetter(SAME_AS)
	public void setSameReferenceLinks(List<String> uris) {
		this.sameAs = uris;
	}

	@Override
	@JsonGetter(SAME_AS)
	public List<String> getSameReferenceLinks() {
		return this.sameAs;
	}
}
