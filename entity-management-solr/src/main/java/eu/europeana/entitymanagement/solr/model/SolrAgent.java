package eu.europeana.entitymanagement.solr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europeana.entitymanagement.solr.SolrUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import org.springframework.util.CollectionUtils;

/*
 * TODO:see how to save the referencedWebResource and isAggregatedBy fields for all entities
 */
public class SolrAgent extends SolrEntity<Agent> {

	public SolrAgent() {
		super();
	}

	@Field(AgentSolrFields.DATE)
	private List<String> date;

	@Field(AgentSolrFields.BEGIN)
	private List<String> begin;

	@Field(AgentSolrFields.END)
	private List<String> end;

	@Field(AgentSolrFields.DATE_OF_BIRTH_ALL)
	private List<String> dateOfBirth;

	@Field(AgentSolrFields.DATE_OF_DEATH_ALL)
	private List<String> dateOfDeath;

	@Field(AgentSolrFields.WAS_PRESENT_AT)
	private List<String> wasPresentAt;

	@Field(AgentSolrFields.HAS_MET)
	private List<String> hasMet;

	@Field(AgentSolrFields.NAME)
	private Map<String, String> name;

	@Field(AgentSolrFields.BIOGRAPHICAL_INFORMATION_ALL)
	private Map<String, List<String>> biographicalInformation;

	@Field(AgentSolrFields.PROFESSION_OR_OCCUPATION_ALL)
	private List<String> professionOrOccupation;

	@Field(AgentSolrFields.PLACE_OF_BIRTH_ALL)
	private List<String> placeOfBirth;

	@Field(AgentSolrFields.PLACE_OF_DEATH_ALL)
	private List<String> placeOfDeath;

	@Field(AgentSolrFields.DATE_OF_ESTABLISHMENT)
	private String dateOfEstablishment;

	@Field(AgentSolrFields.DATE_OF_TERMINATION)
	private String dateOfTermination;

	@Field(AgentSolrFields.GENDER)
	private String gender;

	public SolrAgent(Agent agent) {
		super(agent);
		
		if(agent.getDate()!=null) this.date = new ArrayList<>(agent.getDate());
		if(agent.getBegin()!=null) this.begin = new ArrayList<>(agent.getBegin());
		if(agent.getEnd()!=null) this.end = new ArrayList<>(agent.getEnd());
		if(agent.getDateOfBirth()!=null) this.dateOfBirth = new ArrayList<>(agent.getDateOfBirth());
		if(agent.getDateOfDeath()!=null) this.dateOfDeath = new ArrayList<>(agent.getDateOfDeath());
		if(agent.getWasPresentAt()!=null) this.wasPresentAt = new ArrayList<>(agent.getWasPresentAt());
		if(agent.getHasMet()!=null) this.hasMet = agent.getHasMet();
		setName(agent.getName());
		setBiographicalInformation(agent.getBiographicalInformation());
		if(agent.getProfessionOrOccupation()!=null) {
			this.professionOrOccupation = new ArrayList<>(agent.getProfessionOrOccupation());
		}
		if(agent.getPlaceOfBirth() != null) {
			this.placeOfBirth = new ArrayList<>(agent.getPlaceOfBirth());
		}
		if(agent.getPlaceOfDeath() != null) {
			this.placeOfDeath = new ArrayList<>(agent.getPlaceOfDeath());
		}
		this.dateOfEstablishment = CollectionUtils.lastElement(agent.getDateOfEstablishment());
		this.dateOfTermination = CollectionUtils.lastElement(agent.getDateOfTermination());
		this.gender = CollectionUtils.lastElement(agent.getGender());
	}


	private void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		if (MapUtils.isNotEmpty(biographicalInformation))  {
			this.biographicalInformation = new HashMap<>(SolrUtils.normalizeStringListMapByAddingPrefix(AgentSolrFields.BIOGRAPHICAL_INFORMATION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, biographicalInformation));
		}
	}

	
	private void setName(Map<String, String> name) {
		if (MapUtils.isNotEmpty(name)) {
			this.name = new HashMap<>(SolrUtils.normalizeStringMapByAddingPrefix(AgentSolrFields.NAME + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, name));
		}
	}


	public List<String> getDate() {
		return date;
	}


	public List<String> getBegin() {
		return begin;
	}


	public List<String> getEnd() {
		return end;
	}


	public List<String> getDateOfBirth() {
		return dateOfBirth;
	}


	public List<String> getDateOfDeath() {
		return dateOfDeath;
	}


	public List<String> getWasPresentAt() {
		return wasPresentAt;
	}


	public List<String> getHasMet() {
		return hasMet;
	}


	public Map<String, String> getName() {
		return name;
	}


	public Map<String, List<String>> getBiographicalInformation() {
		return biographicalInformation;
	}


	public List<String> getProfessionOrOccupation() {
		return professionOrOccupation;
	}


	public List<String> getPlaceOfBirth() {
		return placeOfBirth;
	}


	public List<String> getPlaceOfDeath() {
		return placeOfDeath;
	}


	public String getDateOfEstablishment() {
		return dateOfEstablishment;
	}


	public String getDateOfTermination() {
		return dateOfTermination;
	}


	public String getGender() {
		return gender;
	}
}
