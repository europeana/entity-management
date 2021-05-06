package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName= XmlFields.XML_SKOS_CONCEPT)
@JsonPropertyOrder({CONTEXT, ID, TYPE, DEPICTION, IS_SHOWN_BY, PREF_LABEL, ALT_LABEL, HIDDEN_LABEL,NOTE,
		NOTATION, BROADER, NARROWER, RELATED, BROAD_MATCH, NARROW_MATCH, RELATED_MATCH,CLOSE_MATCH,EXACT_MATCH,IN_SCHEME})
public class Concept extends Entity {

	public Concept() {
		super();
	}

	private String[] broader;
	private String[] narrower;
	private String[] related;
	private String[] broadMatch;
	private String[] narrowMatch;
	private String[] exactMatch;
	private String[] coref;
	private String[] relatedMatch;
	private String[] closeMatch;
	private String[] inScheme;
	private Map<String, List<String>> notation;

	@JsonGetter(BROADER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROADER)
	public String[] getBroader() {
		return broader;
	}

	
	@JsonSetter(BROADER)
	public void setBroader(String[] broader) {
		this.broader = broader;
	}

	@JsonGetter(NARROWER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROWER)
	public String[] getNarrower() {
		return narrower;
	}

	
	@JsonSetter(NARROWER)
	public void setNarrower(String[] narrower) {
		this.narrower = narrower;
	}

	@JsonGetter(RELATED)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED)
	public String[] getRelated() {
		return related;
	}

	
	@JsonSetter(RELATED)
	public void setRelated(String[] related) {
		this.related = related;
	}

	@JsonGetter(BROAD_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROAD_MATCH)
	public String[] getBroadMatch() {
		return broadMatch;
	}

	
	@JsonSetter(BROAD_MATCH)
	public void setBroadMatch(String[] broadMatch) {
		this.broadMatch = broadMatch;
	}

	@JsonGetter(NARROW_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROW_MATCH)
	public String[] getNarrowMatch() {
		return narrowMatch;
	}

	
	@JsonSetter(NARROW_MATCH)
	public void setNarrowMatch(String[] narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	@JsonGetter(EXACT_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_EXACT_MATCH)
	public String[] getExactMatch() {
		return exactMatch;
	}

	
	@JsonSetter(EXACT_MATCH)
	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}

	public String[] getCoref() {
		return coref;
	}

	
	public void setCoref(String[] coref) {
		this.coref = coref;
	}

	@JsonGetter(RELATED_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED_MATCH)
	public String[] getRelatedMatch() {
		return relatedMatch;
	}

	
	@JsonSetter(RELATED_MATCH)
	public void setRelatedMatch(String[] relatedMatch) {
		this.relatedMatch = relatedMatch;
	}

	@JsonGetter(CLOSE_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_CLOSE_MATCH)
	public String[] getCloseMatch() {
		return closeMatch;
	}

	
	@JsonSetter(CLOSE_MATCH)
	public void setCloseMatch(String[] closeMatch) {
		this.closeMatch = closeMatch;
	}

	@JsonGetter(IN_SCHEME)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_IN_SCHEMA)
	public String[] getInScheme() {
		return inScheme;
	}

	
	@JsonSetter(IN_SCHEME)
	public void setInScheme(String[] inScheme) {
		this.inScheme = inScheme;
	}

	@JsonGetter(NOTATION)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTATION)
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
