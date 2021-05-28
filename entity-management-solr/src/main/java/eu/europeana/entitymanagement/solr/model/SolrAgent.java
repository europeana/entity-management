package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

/*
 * TODO:see how to save the referencedWebResource and isAggregatedBy fields for all entities
 */
public class SolrAgent extends Agent {
	
	public SolrAgent() {
		super();
	}

	public SolrAgent(Agent agent) {
		super();
		this.setType(agent.getType());
		this.setEntityId(agent.getEntityId());
		this.setDepiction(agent.getDepiction());
		this.setNote(agent.getNote());
		this.setPrefLabelStringMap(agent.getPrefLabelStringMap());
		this.setAltLabel(agent.getAltLabel());
		this.setHiddenLabel(agent.getHiddenLabel());
		this.setIdentifier(agent.getIdentifier());
		this.setSameAs(agent.getSameAs());
		this.setIsRelatedTo(agent.getIsRelatedTo());
		this.setHasPart(agent.getHasPart());
		this.setIsPartOfArray(agent.getIsPartOfArray());
		
		this.setDate(agent.getDate());
		this.setBegin(agent.getBegin());
		this.setEnd(agent.getEnd());
		this.setDateOfBirth(agent.getDateOfBirth());
		this.setDateOfDeath(agent.getDateOfDeath());
		this.setWasPresentAt(agent.getWasPresentAt());
		this.setHasMet(agent.getHasMet());
		this.setName(agent.getName());
		this.setBiographicalInformation(agent.getBiographicalInformation());
		this.setProfessionOrOccupation(agent.getProfessionOrOccupation());
		this.setPlaceOfBirth(agent.getPlaceOfBirth());
		this.setPlaceOfDeath(agent.getPlaceOfDeath());
		this.setDateOfEstablishment(agent.getDateOfEstablishment());
		this.setDateOfTermination(agent.getDateOfTermination());
		this.setGender(agent.getGender());
		this.setExactMatch(agent.getExactMatch());
	}

	@Override
	@Field(AgentSolrFields.SAME_AS)
	public void setSameAs(List<String> sameAs) {
		super.setSameAs(sameAs);
	}
	
	@Override
	@Field(AgentSolrFields.EXACT_MATCH)
	public void setExactMatch(List<String> exactMatch) {
		super.setExactMatch(exactMatch);
	}
	
	@Override
	@Field(AgentSolrFields.TYPE)
	public void setType(String type) {
		super.setType(type);
	}

	@Override
	@Field(AgentSolrFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		super.setIdentifier(identifier);
	}
	
	@Override
	@Field(AgentSolrFields.ID)
	public void setEntityId(String entityId) {
		super.setEntityId(entityId);
	}
	
	@Override
	@Field(AgentSolrFields.HAS_PART)
	public void setHasPart(List<String> hasPart) {
		super.setHasPart(hasPart);
	}
	
	@Override
	@Field(AgentSolrFields.WAS_PRESENT_AT)
	public void setWasPresentAt(List<String> wasPresentAt) {
		super.setWasPresentAt(wasPresentAt);
	}

	@Override
	@Field(AgentSolrFields.IS_PART_OF)
	public void setIsPartOfArray(List<String> isPartOf) {
		super.setIsPartOfArray(isPartOf);
	}

	@Override
	@Field(AgentSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}

	@Override
	@Field(AgentSolrFields.PREF_LABEL_ALL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMapByAddingPrefix(
				AgentSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel);
		super.setPrefLabelStringMap(normalizedPrefLabel);
	}

	@Override
	@Field(AgentSolrFields.ALT_LABEL_ALL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel);
		super.setAltLabel(normalizedAltLabel);
	}

	@Override
	@Field(AgentSolrFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, hiddenLabel);
		super.setHiddenLabel(normalizedHiddenLabel);
	}

	@Override
	@Field(AgentSolrFields.NOTE_ALL)
	public void setNote(Map<String, List<String>> note) {
		Map<String, List<String>>  normalizedNote = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note);
		super.setNote(normalizedNote);
	}
	
	@Override
	@Field(AgentSolrFields.DEPICTION)
	public void setDepiction(String depiction) {
		super.setDepiction(depiction);
	}
	
	/**
	 *  Agent Specific Fields
	 */
	@Override
	@Field(AgentSolrFields.BEGIN)
	public void setBegin(List<String> begin) {
		super.setBegin(begin);
	}

	@Override
	@Field(AgentSolrFields.END)
	public void setEnd(List<String> end) {
		super.setEnd(end);
	}

	@Override
	@Field(AgentSolrFields.HAS_MET)
	public void setHasMet(List<String> hasMet) {
		super.setHasMet(hasMet);
	}
	@Override
	@Field(AgentSolrFields.NAME)
	public void setName(Map<String, String> name) {
		super.setName(name);
	}
	
	@Override
	@Field(AgentSolrFields.BIOGRAPHICAL_INFORMATION_ALL)
	public void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		Map<String, List<String>> normalizedBiographicalInformation = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.BIOGRAPHICAL_INFORMATION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, biographicalInformation);
		super.setBiographicalInformation(normalizedBiographicalInformation);
	}

	@Override
	@Field(AgentSolrFields.DATE_OF_BIRTH_ALL)
	public void setDateOfBirth(List<String> dateOfBirth) {
		super.setDateOfBirth(dateOfBirth);
	}

	@Override
	@Field(AgentSolrFields.DATE_OF_DEATH_ALL)
	public void setDateOfDeath(List<String> dateOfDeath) {
		super.setDateOfDeath(dateOfDeath);
	}

	@Override
	@Field(AgentSolrFields.PLACE_OF_BIRTH_ALL)
	public void setPlaceOfBirth(Map<String, List<String>> placeOfBirth) {
		Map<String, List<String>> normalizedPlaceOfBirth = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.PLACE_OF_BIRTH + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, placeOfBirth);
		super.setPlaceOfBirth(normalizedPlaceOfBirth);
	}

	@Override
	@Field(AgentSolrFields.PLACE_OF_DEATH_ALL)
	public void setPlaceOfDeath(Map<String, List<String>> placeOfDeath) {
		Map<String, List<String>> normalizedPlaceOfDeath = SolrUtils.normalizeStringListMapByAddingPrefix(
				AgentSolrFields.PLACE_OF_DEATH + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, placeOfDeath);
		super.setPlaceOfDeath(normalizedPlaceOfDeath);
	}

	@Override
	@Field(AgentSolrFields.DATE_OF_ESTABLISHMENT)
	public void setDateOfEstablishment(String dateOfEstablishment) {
		super.setDateOfEstablishment(dateOfEstablishment);
	}

	@Override
	@Field(AgentSolrFields.GENDER)
	public void setGender(String gender) {
		super.setGender(gender);
	}
	
	@Override
	@Field(AgentSolrFields.PROFESSION_OR_OCCUPATION_ALL)
	public void setProfessionOrOccupation(List<String> professionOrOccupation) {
		super.setProfessionOrOccupation(professionOrOccupation);
	}

	/**
	 *  Technical Fields
	 */
	@Override
	@Field(AgentSolrFields.DATE)
	public void setDate(List<String> date) {
		super.setDate(date);
	}
	
}
