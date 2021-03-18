package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName= XmlFields.XML_SKOS_CONCEPT)
public class ConceptImpl extends BaseEntity implements Concept {

	public ConceptImpl(Concept copy) {
		super(copy);
		this.broader = copy.getBroader()!=null ? Arrays.copyOf(copy.getBroader(), copy.getBroader().length) : null;
		this.narrower = copy.getNarrower()!=null ? Arrays.copyOf(copy.getNarrower(), copy.getNarrower().length) : null;
		this.related = copy.getRelated()!=null ? Arrays.copyOf(copy.getRelated(), copy.getRelated().length) : null;
		this.broadMatch = copy.getBroadMatch()!=null ? Arrays.copyOf(copy.getBroadMatch(), copy.getBroadMatch().length) : null;
		this.narrowMatch = copy.getNarrowMatch()!=null ? Arrays.copyOf(copy.getNarrowMatch(), copy.getNarrowMatch().length) : null;
		this.exactMatch = copy.getExactMatch()!=null ? Arrays.copyOf(copy.getExactMatch(), copy.getExactMatch().length) : null;
		this.coref = copy.getCoref()!=null ? Arrays.copyOf(copy.getCoref(), copy.getCoref().length) : null;
		this.relatedMatch = copy.getRelatedMatch()!=null ? Arrays.copyOf(copy.getRelatedMatch(), copy.getRelatedMatch().length) : null;
		this.closeMatch = copy.getCloseMatch()!=null ? Arrays.copyOf(copy.getCloseMatch(), copy.getCloseMatch().length) : null;
		this.inScheme = copy.getInScheme()!=null ? Arrays.copyOf(copy.getInScheme(), copy.getInScheme().length) : null;
		this.notation = copy.getNotation()!=null ? new HashMap<>(copy.getNotation()) : null;
	}

	public ConceptImpl() {
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

	@JsonGetter(WebEntityFields.BROADER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROADER)
	public String[] getBroader() {
		return broader;
	}

	@Override
	@JsonSetter(WebEntityFields.BROADER)
	public void setBroader(String[] broader) {
		this.broader = broader;
	}

	@JsonGetter(WebEntityFields.NARROWER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROWER)
	public String[] getNarrower() {
		return narrower;
	}

	@Override
	@JsonSetter(WebEntityFields.NARROWER)
	public void setNarrower(String[] narrower) {
		this.narrower = narrower;
	}

	@JsonGetter(WebEntityFields.RELATED)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED)
	public String[] getRelated() {
		return related;
	}

	@Override
	@JsonSetter(WebEntityFields.RELATED)
	public void setRelated(String[] related) {
		this.related = related;
	}

	@JsonGetter(WebEntityFields.BROAD_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROAD_MATCH)
	public String[] getBroadMatch() {
		return broadMatch;
	}

	@Override
	@JsonSetter(WebEntityFields.BROAD_MATCH)
	public void setBroadMatch(String[] broadMatch) {
		this.broadMatch = broadMatch;
	}

	@JsonGetter(WebEntityFields.NARROW_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROW_MATCH)
	public String[] getNarrowMatch() {
		return narrowMatch;
	}

	@Override
	@JsonSetter(WebEntityFields.NARROW_MATCH)
	public void setNarrowMatch(String[] narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	@JsonGetter(WebEntityFields.EXACT_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_EXACT_MATCH)
	public String[] getExactMatch() {
		return exactMatch;
	}

	@Override
	@JsonSetter(WebEntityFields.EXACT_MATCH)
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

	@JsonGetter(WebEntityFields.RELATED_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED_MATCH)
	public String[] getRelatedMatch() {
		return relatedMatch;
	}

	@Override
	@JsonSetter(WebEntityFields.RELATED_MATCH)
	public void setRelatedMatch(String[] relatedMatch) {
		this.relatedMatch = relatedMatch;
	}

	@JsonGetter(WebEntityFields.CLOSE_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_CLOSE_MATCH)
	public String[] getCloseMatch() {
		return closeMatch;
	}

	@Override
	@JsonSetter(WebEntityFields.CLOSE_MATCH)
	public void setCloseMatch(String[] closeMatch) {
		this.closeMatch = closeMatch;
	}

	@JsonGetter(WebEntityFields.IN_SCHEME)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_IN_SCHEMA)
	public String[] getInScheme() {
		return inScheme;
	}

	@Override
	@JsonSetter(WebEntityFields.IN_SCHEME)
	public void setInScheme(String[] inScheme) {
		this.inScheme = inScheme;
	}

	@JsonGetter(WebEntityFields.NOTATION)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTATION)
	public Map<String, List<String>> getNotation() {
		return notation;
	}

	@Override
	@JsonSetter(WebEntityFields.NOTATION)
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
