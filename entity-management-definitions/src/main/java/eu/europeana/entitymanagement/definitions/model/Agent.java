package eu.europeana.entitymanagement.definitions.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;

@JsonDeserialize(as = AgentImpl.class)
public interface Agent extends Entity {

	public String[] getDate();

	public void setDate(String[] date);

	public String[] getHasMet();

	public void setHasMet(String[] hasMet);

	public Map<String, String> getName();

	public void setName(Map<String, String> name);

	public Map<String, List<String>> getBiographicalInformation();

	public void setBiographicalInformation(Map<String, List<String>> biographicalInformation);

	public Map<String, List<String>> getProfessionOrOccupation();

	public void setProfessionOrOccupation(Map<String, List<String>> professionOrOccupation);

	public String[] getDateOfBirth();

	public void setDateOfBirth(String[] dateOfBirth);

	public String[] getDateOfDeath();

	public void setDateOfDeath(String[] dateOfDeath);
	
	public String[] getBeginArray();

	public void setBegin(String[] begin);

	public String[] getEndArray();

	public void setEnd(String[] end);
	
	public String [] getExactMatch();
	
	public void setExactMatch(String[] exactMatch);

	public Map<String, List<String>> getPlaceOfBirth();

	public void setPlaceOfBirth(Map<String, List<String>> placeOfBirth);

	public Map<String, List<String>> getPlaceOfDeath();

	public void setPlaceOfDeath(Map<String, List<String>> placeOfDeath);

	public String getDateOfEstablishment();

	public void setDateOfEstablishment(String dateOfEstablishment);

	public String getDateOfTermination();

	public void setDateOfTermination(String dateOfTermination);

	public String getGender();

	public void setGender(String gender);

	// methods common for some entities
	public String[] getHasPart();

	public void setHasPart(String[] hasPart);

	public String[] getIsPartOfArray();

	public void setIsPartOfArray(String[] isPartOf);
	
	public void setIsShownBy (WebResource resource);
	
	public String[] getWasPresentAt();

	public void setWasPresentAt(String[] wasPresentAt);


}
