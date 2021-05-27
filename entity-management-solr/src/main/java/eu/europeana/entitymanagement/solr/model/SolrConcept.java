package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.vocabulary.ConceptSolrFields;

public class SolrConcept extends Concept {

	public SolrConcept(Concept concept) {
		super();
		this.setType(concept.getType());
		this.setEntityId(concept.getEntityId());
		this.setDepiction(concept.getDepiction());
		this.setNote(concept.getNote());
		this.setPrefLabelStringMap(concept.getPrefLabelStringMap());
		this.setAltLabel(concept.getAltLabel());
		this.setHiddenLabel(concept.getHiddenLabel());
		this.setIdentifier(concept.getIdentifier());
		this.setSameAs(concept.getSameAs());
		this.setIsRelatedTo(concept.getIsRelatedTo());
		this.setHasPart(concept.getHasPart());
		this.setIsPartOfArray(concept.getIsPartOfArray());
		
		this.setBroader(concept.getBroader());
		this.setNarrower(concept.getNarrower());
		this.setRelated(concept.getRelated());
		this.setBroadMatch(concept.getBroadMatch());
		this.setNarrowMatch(concept.getNarrowMatch());
		this.setExactMatch(concept.getExactMatch());
		this.setCoref(concept.getCoref());
		this.setRelatedMatch(concept.getRelatedMatch());
		this.setCloseMatch(concept.getCloseMatch());
		this.setInScheme(concept.getInScheme());
		this.setNotation(concept.getNotation());
	}

	@Override
	@Field(ConceptSolrFields.BROADER)
	public void setBroader(List<String> broader) {
		super.setBroader(broader);
	}
	
	@Override
	@Field(ConceptSolrFields.NARROWER)
	public void setNarrower(List<String> narrower) {
		super.setNarrower(narrower);
	}
	
	@Override
	@Field(ConceptSolrFields.RELATED)
	public void setRelated(List<String> related) {
		super.setRelated(related);
	}
	
	@Override
	@Field(ConceptSolrFields.BROAD_MATCH)
	public void setBroadMatch(List<String> broadMatch) {
		super.setBroadMatch(broadMatch);
	}
	
	@Override
	@Field(ConceptSolrFields.NARROW_MATCH)
	public void setNarrowMatch(List<String> narrowMatch) {
		super.setNarrowMatch(narrowMatch);
	}
	
	@Override
	@Field(ConceptSolrFields.EXACT_MATCH)
	public void setExactMatch(List<String> exactMatch) {
		super.setExactMatch(exactMatch);
	}
	
	@Override
	@Field(ConceptSolrFields.RELATED_MATCH)
	public void setRelatedMatch(List<String> relatedMatch) {
		super.setRelatedMatch(relatedMatch);
	}
	
	@Override
	@Field(ConceptSolrFields.CLOSE_MATCH)
	public void setCloseMatch(List<String> closeMatch) {
		setCloseMatch(closeMatch);
	}
	
	@Override
	@Field(ConceptSolrFields.NOTATION_ALL)
	public void setNotation(Map<String, List<String>> notation) {
		Map<String, List<String>>  normalizedNotation = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.NOTATION, notation);
		super.setNotation(normalizedNotation);
	}
	
	@Override
	@Field(ConceptSolrFields.IN_SCHEME)
	public void setInScheme(List<String> inScheme) {
		super.setInScheme(inScheme);
	}
	
	@Override
	@Field(ConceptSolrFields.PREF_LABEL_ALL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMap(
				ConceptSolrFields.PREF_LABEL, prefLabel);
		super.setPrefLabelStringMap(normalizedPrefLabel);
	}
	
	@Override
	@Field(ConceptSolrFields.ALT_LABEL_ALL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.ALT_LABEL, altLabel);
		super.setAltLabel(normalizedAltLabel);
	}
	
	@Override
	@Field(ConceptSolrFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.HIDDEN_LABEL, hiddenLabel);
		super.setHiddenLabel(normalizedHiddenLabel);
	}
	
	@Override
	@Field(ConceptSolrFields.NOTE_ALL)
	public void setNote(Map<String, List<String>> note) {
		Map<String, List<String>>  normalizedNote = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.NOTE, note);
		super.setNote(normalizedNote);
	}

	@Override
	@Field(ConceptSolrFields.ID)
	public void setEntityId(String entityId) {
		super.setEntityId(entityId);
	}

	@Override
	@Field(ConceptSolrFields.TYPE)
	public void setType(String type) {
		super.setType(type);
	}
	
	@Override
	@Field(ConceptSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}

	@Override
	@Field(ConceptSolrFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		super.setIdentifier(identifier);
	}
	
	@Override
	@Field(ConceptSolrFields.DEPICTION)
	public void setDepiction(String depiction) {
		super.setDepiction(depiction);
	}

	@Override
	@Field(ConceptSolrFields.SAME_AS)
	public void setSameAs(List<String> sameAs) {
		super.setSameAs(sameAs);
	}
	
	@Override
	@Field(ConceptSolrFields.HAS_PART)
	public void setHasPart(List<String> hasPart) {
		super.setHasPart(hasPart);
	}

	@Override
	@Field(ConceptSolrFields.IS_PART_OF)
	public void setIsPartOfArray(List<String> isPartOf) {
		super.setIsPartOfArray(isPartOf);
	}

}
