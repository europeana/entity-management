package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName= XmlFields.XML_SKOS_CONCEPT)
public class ConceptImpl extends BaseEntity implements Concept {

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

	@JsonProperty(WebEntityFields.BROADER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROADER)
	public String[] getBroader() {
		return broader;
	}

	@Override
	public void setBroader(String[] broader) {
		this.broader = broader;
	}

	@JsonProperty(WebEntityFields.NARROWER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROWER)
	public String[] getNarrower() {
		return narrower;
	}

	@Override
	public void setNarrower(String[] narrower) {
		this.narrower = narrower;
	}

	@JsonProperty(WebEntityFields.RELATED)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED)
	public String[] getRelated() {
		return related;
	}

	@Override
	public void setRelated(String[] related) {
		this.related = related;
	}

	@JsonProperty(WebEntityFields.BROAD_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROAD_MATCH)
	public String[] getBroadMatch() {
		return broadMatch;
	}

	@Override
	public void setBroadMatch(String[] broadMatch) {
		this.broadMatch = broadMatch;
	}

	@JsonProperty(WebEntityFields.NARROW_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROW_MATCH)
	public String[] getNarrowMatch() {
		return narrowMatch;
	}

	@Override
	public void setNarrowMatch(String[] narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	@JsonProperty(WebEntityFields.EXACT_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_EXACT_MATCH)
	public String[] getExactMatch() {
		return exactMatch;
	}

	@Override
	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}

	public String[] getCoref() {
		return coref;
	}

	@Override
	public void setCoref(String[] coref) {
		this.coref = coref;
	}

	@JsonProperty(WebEntityFields.RELATED_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED_MATCH)
	public String[] getRelatedMatch() {
		return relatedMatch;
	}

	@Override
	public void setRelatedMatch(String[] relatedMatch) {
		this.relatedMatch = relatedMatch;
	}

	@JsonProperty(WebEntityFields.CLOSE_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_CLOSE_MATCH)
	public String[] getCloseMatch() {
		return closeMatch;
	}

	@Override
	public void setCloseMatch(String[] closeMatch) {
		this.closeMatch = closeMatch;
	}

	@JsonProperty(WebEntityFields.IN_SCHEME)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_IN_SCHEMA)
	public String[] getInScheme() {
		return inScheme;
	}

	@Override
	public void setInScheme(String[] inScheme) {
		this.inScheme = inScheme;
	}

	@JsonProperty(WebEntityFields.NOTATION)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTATION)
	public Map<String, List<String>> getNotation() {
		return notation;
	}

	@Override
	public void setNotation(Map<String, List<String>> notation) {
		this.notation = notation;
	}

	@Override
	public String getType() {
		return "Concept";
	}
	
	@Override
	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	@Override
	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}


}
