package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import eu.europeana.entitymanagement.definitions.model.Entity;
import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

/*
 * TODO:see how to save the referencedWebResource and isAggregatedBy fields for all entities
 */
public class SolrAgent extends SolrEntity<Agent> {

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
	private Map<String, List<String>> placeOfBirth;

	@Field(AgentSolrFields.PLACE_OF_DEATH_ALL)
	private Map<String, List<String>> placeOfDeath;

	@Field(AgentSolrFields.DATE_OF_ESTABLISHMENT)
	private String dateOfEstablishment;

	@Field(AgentSolrFields.DATE_OF_TERMINATION)
	private String dateOfTermination;

	@Field(AgentSolrFields.GENDER)
	private String gender;

	public SolrAgent(Agent agent) {
		super(agent);

		
		this.date = agent.getDate();
		this.begin = agent.getBegin();
		this.end = agent.getEnd();
		this.dateOfBirth = agent.getDateOfBirth();
		this.dateOfDeath = agent.getDateOfDeath();
		this.wasPresentAt = agent.getWasPresentAt();
		this.hasMet = agent.getHasMet();
		this.name = agent.getName();
		setBiographicalInformation(agent.getBiographicalInformation());
		this.professionOrOccupation = agent.getProfessionOrOccupation();
		setPlaceOfBirth(agent.getPlaceOfBirth());
		setPlaceOfDeath(agent.getPlaceOfDeath());
		this.dateOfEstablishment = agent.getDateOfEstablishment();
		this.dateOfTermination = agent.getDateOfTermination();
		this.gender = agent.getGender();
	}


	private void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		this.biographicalInformation = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.BIOGRAPHICAL_INFORMATION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, biographicalInformation);
	}

	private void setPlaceOfBirth(Map<String, List<String>> placeOfBirth) {
		this.placeOfBirth = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.PLACE_OF_BIRTH + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, placeOfBirth);
	}

	private void setPlaceOfDeath(Map<String, List<String>> placeOfDeath) {
		this.placeOfDeath = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.PLACE_OF_DEATH + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, placeOfDeath);
	}
}
