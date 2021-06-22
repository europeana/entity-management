package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, IS_SHOWN_BY, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL,NOTE,
		NOTATION, BROADER, NARROWER, RELATED, BROAD_MATCH, NARROW_MATCH, RELATED_MATCH,CLOSE_MATCH,EXACT_MATCH,IN_SCHEME})
public class Concept extends Entity {

	public Concept() {
		super();
	}

	public Concept(Concept copy) {
		super(copy);
		if(copy.getBroader()!=null) this.broader = new ArrayList<>(copy.getBroader());
		if(copy.getNarrower()!=null) this.narrower = new ArrayList<>(copy.getNarrower());
		if(copy.getRelated()!=null) this.related = new ArrayList<>(copy.getRelated());
		if(copy.getBroadMatch()!=null) this.broadMatch = new ArrayList<>(copy.getBroadMatch());
		if(copy.getNarrowMatch()!=null) this.narrowMatch = new ArrayList<>(copy.getNarrowMatch());
		if(copy.getExactMatch()!=null) this.exactMatch = new ArrayList<>(copy.getExactMatch());
		if(copy.getCoref()!=null) this.coref = new ArrayList<>(copy.getCoref());
		if(copy.getRelatedMatch()!=null) this.relatedMatch = new ArrayList<>(copy.getRelatedMatch());
		if(copy.getCloseMatch()!=null) this.closeMatch = new ArrayList<>(copy.getCloseMatch());
		if(copy.getInScheme()!=null) this.inScheme = new ArrayList<>(copy.getInScheme());
		if(copy.getNotation()!=null) this.notation = new HashMap<>(copy.getNotation());
	}

	private List<String> broader;
	private List<String> narrower;
	private List<String> related;
	private List<String> broadMatch;
	private List<String> narrowMatch;
	private List<String> exactMatch;
	private List<String> coref;
	private List<String> relatedMatch;
	private List<String> closeMatch;
	private List<String> inScheme;
	private Map<String, List<String>> notation;

	@JsonGetter(BROADER)
	public List<String> getBroader() {
		return broader;
	}

	
	@JsonSetter(BROADER)
	public void setBroader(List<String> broader) {
		this.broader = broader;
	}

	@JsonGetter(NARROWER)
	public List<String> getNarrower() {
		return narrower;
	}

	
	@JsonSetter(NARROWER)
	public void setNarrower(List<String> narrower) {
		this.narrower = narrower;
	}

	@JsonGetter(RELATED)
	public List<String> getRelated() {
		return related;
	}

	
	@JsonSetter(RELATED)
	public void setRelated(List<String> related) {
		this.related = related;
	}

	@JsonGetter(BROAD_MATCH)
	public List<String> getBroadMatch() {
		return broadMatch;
	}

	
	@JsonSetter(BROAD_MATCH)
	public void setBroadMatch(List<String> broadMatch) {
		this.broadMatch = broadMatch;
	}

	@JsonGetter(NARROW_MATCH)
	public List<String> getNarrowMatch() {
		return narrowMatch;
	}

	
	@JsonSetter(NARROW_MATCH)
	public void setNarrowMatch(List<String> narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	@JsonGetter(EXACT_MATCH)
	public List<String> getExactMatch() {
		return exactMatch;
	}

	
	@JsonSetter(EXACT_MATCH)
	public void setExactMatch(List<String> exactMatch) {
		this.exactMatch = exactMatch;
	}

	public List<String> getCoref() {
		return coref;
	}

	
	public void setCoref(List<String> coref) {
		this.coref = coref;
	}

	@JsonGetter(RELATED_MATCH)
	public List<String> getRelatedMatch() {
		return relatedMatch;
	}

	
	@JsonSetter(RELATED_MATCH)
	public void setRelatedMatch(List<String> relatedMatch) {
		this.relatedMatch = relatedMatch;
	}

	@JsonGetter(CLOSE_MATCH)
	public List<String> getCloseMatch() {
		return closeMatch;
	}

	
	@JsonSetter(CLOSE_MATCH)
	public void setCloseMatch(List<String> closeMatch) {
		this.closeMatch = closeMatch;
	}

	@JsonGetter(IN_SCHEME)
	public List<String> getInScheme() {
		return inScheme;
	}

	
	@JsonSetter(IN_SCHEME)
	public void setInScheme(List<String> inScheme) {
		this.inScheme = inScheme;
	}

	@JsonGetter(NOTATION)
	public Map<String, List<String>> getNotation() {
		return notation;
	}

	
	@JsonSetter(NOTATION)
	public void setNotation(Map<String, List<String>> notation) {
		this.notation = notation;
	}

	
	public String getType() {
		return "Concept";
	}

	
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}

}
