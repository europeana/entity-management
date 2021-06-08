package eu.europeana.entitymanagement.solr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.vocabulary.ConceptSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;

public class SolrConcept extends SolrEntity<Concept> {

	@Field(ConceptSolrFields.BROADER)
	private List<String> broader;
	
	@Field(ConceptSolrFields.NARROWER)
	private List<String> narrower;

	@Field(ConceptSolrFields.RELATED)
	private List<String> related;

	@Field(ConceptSolrFields.BROAD_MATCH)
	private List<String> broadMatch;

	@Field(ConceptSolrFields.NARROW_MATCH)
	private List<String> narrowMatch;

	@Field(ConceptSolrFields.EXACT_MATCH)
	private List<String> exactMatch;

	@Field(ConceptSolrFields.COREF)
	private List<String> coref;

	@Field(ConceptSolrFields.RELATED_MATCH)
	private List<String> relatedMatch;

	@Field(ConceptSolrFields.CLOSE_MATCH)
	private List<String> closeMatch;

	@Field(ConceptSolrFields.IN_SCHEME)
	private List<String> inScheme;

	@Field(ConceptSolrFields.NOTATION_ALL)
	Map<String, List<String>> notation;
	
	public SolrConcept() {
		super();
	}

	public SolrConcept(Concept concept) {
		super(concept);
		
		if(concept.getBroader()!=null) this.broader=new ArrayList<>(concept.getBroader());
		if(concept.getNarrower()!=null) this.narrower= new ArrayList<>(concept.getNarrower());
		if(concept.getRelated()!=null) this.related= new ArrayList<>(concept.getRelated());
		if(concept.getBroadMatch()!=null) this.broadMatch=new ArrayList<>(concept.getBroadMatch());
		if(concept.getNarrowMatch()!=null) this.narrowMatch= new ArrayList<>(concept.getNarrowMatch());
		if(concept.getExactMatch()!=null) this.exactMatch= new ArrayList<>(concept.getExactMatch());
		if(concept.getCoref()!=null) this.coref=new ArrayList<>(concept.getCoref());
		if(concept.getRelatedMatch()!=null) this.relatedMatch= new ArrayList<>(concept.getRelatedMatch());
		if(concept.getCloseMatch()!=null) this.closeMatch= new ArrayList<>(concept.getCloseMatch());
		if(concept.getInScheme()!=null) this.inScheme= new ArrayList<>(concept.getInScheme());
		setNotation(concept.getNotation());
	}

	private void setNotation(Map<String, List<String>> notation) {
		if (notation!=null) {
			this.notation = new HashMap<>(SolrUtils.normalizeStringListMapByAddingPrefix(ConceptSolrFields.NOTATION + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, notation));
		}
	}

	public List<String> getBroader() {
		return broader;
	}

	public List<String> getNarrower() {
		return narrower;
	}

	public List<String> getRelated() {
		return related;
	}

	public List<String> getBroadMatch() {
		return broadMatch;
	}

	public List<String> getNarrowMatch() {
		return narrowMatch;
	}

	public List<String> getExactMatch() {
		return exactMatch;
	}

	public List<String> getCoref() {
		return coref;
	}

	public List<String> getRelatedMatch() {
		return relatedMatch;
	}

	public List<String> getCloseMatch() {
		return closeMatch;
	}

	public List<String> getInScheme() {
		return inScheme;
	}

	public Map<String, List<String>> getNotation() {
		return notation;
	}
}
