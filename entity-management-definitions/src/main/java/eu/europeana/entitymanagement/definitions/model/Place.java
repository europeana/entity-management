package eu.europeana.entitymanagement.definitions.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.PlaceImpl;

@JsonDeserialize(as = PlaceImpl.class)
public interface Place extends Entity {

	void setAltitude(Float altitude);

	Float getAltitude();

	void setLongitude(Float longitude);

	Float getLongitude();

	void setLatitude(Float latitude);

	Float getLatitude();

	void setIsNextInSequence(String[] isNextInSequence);

	String[] getIsNextInSequence();

	// methods common for some entities
	public String[] getHasPart();

	public void setHasPart(String[] hasPart);

	public String[] getIsPartOfArray();

	public void setIsPartOfArray(String[] isPartOf);
	
	public String[] getExactMatch();
	
	public void setExactMatch(String[] exactMatch);
}
