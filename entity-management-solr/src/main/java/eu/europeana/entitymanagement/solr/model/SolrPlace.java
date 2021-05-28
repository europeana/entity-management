package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.PlaceSolrFields;

/*
 * TODO:see how to save the wasPresentAt, referencedWebResource, isAggregatedBy, and entityIdentifier fields
 */
public class SolrPlace extends Place {

	public SolrPlace() {
		super();
	}

	public SolrPlace(Place place) {
		super();
		this.setType(place.getType());
		this.setEntityId(place.getEntityId());
		this.setDepiction(place.getDepiction());
		this.setNote(place.getNote());
		this.setPrefLabelStringMap(place.getPrefLabelStringMap());
		this.setAltLabel(place.getAltLabel());
		this.setHiddenLabel(place.getHiddenLabel());
		this.setIdentifier(place.getIdentifier());
		this.setSameAs(place.getSameAs());
		this.setIsRelatedTo(place.getIsRelatedTo());
		this.setHasPart(place.getHasPart());
		this.setIsPartOfArray(place.getIsPartOfArray());
		
		this.setIsNextInSequence(place.getIsNextInSequence());
		this.setLatitude(place.getLatitude());
		this.setLongitude(place.getLongitude());
		this.setAltitude(place.getAltitude());
		this.setExactMatch(place.getExactMatch());
	}
	
	@Override
	@Field(PlaceSolrFields.SAME_AS)
	public void setSameAs(List<String> sameAs) {
		super.setSameAs(sameAs);
	}
	
	@Override
	@Field(PlaceSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}

	@Override
	@Field(PlaceSolrFields.EXACT_MATCH)
	public void setExactMatch(List<String> exactMatch) {
		super.setExactMatch(exactMatch);
	}
	
	@Override
	@Field(PlaceSolrFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		super.setIdentifier(identifier);
	}

	@Override
	@Field(PlaceSolrFields.TYPE)
	public void setType(String type) {
		super.setType(type);
	}

	@Override
	@Field(PlaceSolrFields.ID)
	public void setEntityId(String entityId) {
		super.setEntityId(entityId);
	}

	@Override
	@Field(PlaceSolrFields.HAS_PART)
	public void setHasPart(List<String> hasPart) {
		super.setHasPart(hasPart);
	}

	@Override
	@Field(PlaceSolrFields.IS_PART_OF)
	public void setIsPartOfArray(List<String> isPartOf) {
		super.setIsPartOfArray(isPartOf);
	}
	
	/**
	 * Concept fields
	 */	
	@Override
	@Field(PlaceSolrFields.NOTE_ALL)
	public void setNote(Map<String, List<String>> note) {
		Map<String, List<String>>  normalizedNote = SolrUtils.normalizeStringListMapByAddingPrefix(
				PlaceSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note);
		super.setNote(normalizedNote);
	}
	
	@Override
	@Field(PlaceSolrFields.PREF_LABEL_ALL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMapByAddingPrefix(
				PlaceSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel);
		super.setPrefLabelStringMap(normalizedPrefLabel);
	}

	@Override
	@Field(PlaceSolrFields.ALT_LABEL_ALL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				PlaceSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel);
		super.setAltLabel(normalizedAltLabel);
	}

	@Override
	@Field(PlaceSolrFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
				PlaceSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, hiddenLabel);
		super.setHiddenLabel(normalizedHiddenLabel);
	}
	
	//SPECIFIC FIELDS
	@Override
	@Field(PlaceSolrFields.IS_NEXT_IN_SEQUENCE)
	public void setIsNextInSequence(List<String> isNextInSequence) {
		super.setIsNextInSequence(isNextInSequence);
	}
	
	@Override
	@Field(PlaceSolrFields.LATITUDE)
	public void setLatitude(Float latitude) {
		super.setLatitude(latitude);
	}
	
	@Override
	@Field(PlaceSolrFields.LONGITUDE)
	public void setLongitude(Float longitude) {
		super.setLongitude(longitude);
	}
	
	@Override
	@Field(PlaceSolrFields.ALTITUDE)
	public void setAltitude(Float altitude) {
		super.setAltitude(altitude);
	}

	@Override
	@Field(PlaceSolrFields.DEPICTION)
	public void setDepiction(String depiction) {
		super.setDepiction(depiction);
	}
	
}
