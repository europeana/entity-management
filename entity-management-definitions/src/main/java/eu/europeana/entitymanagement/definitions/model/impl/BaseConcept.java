package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName= XmlFields.XML_SKOS_CONCEPT)
public class BaseConcept extends BaseEntity implements Concept {

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

	public void setBroader(String[] broader) {
		this.broader = broader;
	}

	@JsonProperty(WebEntityFields.NARROWER)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROWER)
	public String[] getNarrower() {
		return narrower;
	}

	public void setNarrower(String[] narrower) {
		this.narrower = narrower;
	}

	@JsonProperty(WebEntityFields.RELATED)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED)
	public String[] getRelated() {
		return related;
	}

	public void setRelated(String[] related) {
		this.related = related;
	}

	@JsonProperty(WebEntityFields.BROAD_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_BROAD_MATCH)
	public String[] getBroadMatch() {
		return broadMatch;
	}

	public void setBroadMatch(String[] broadMatch) {
		this.broadMatch = broadMatch;
	}

	@JsonProperty(WebEntityFields.NARROW_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NARROW_MATCH)
	public String[] getNarrowMatch() {
		return narrowMatch;
	}

	public void setNarrowMatch(String[] narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	@JsonProperty(WebEntityFields.EXACT_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_EXACT_MATCH)
	public String[] getExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(String[] exactMatch) {
		this.exactMatch = exactMatch;
	}

	public String[] getCoref() {
		return coref;
	}

	public void setCoref(String[] coref) {
		this.coref = coref;
	}

	@JsonProperty(WebEntityFields.RELATED_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_RELATED_MATCH)
	public String[] getRelatedMatch() {
		return relatedMatch;
	}

	public void setRelatedMatch(String[] relatedMatch) {
		this.relatedMatch = relatedMatch;
	}

	@JsonProperty(WebEntityFields.CLOSE_MATCH)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_CLOSE_MATCH)
	public String[] getCloseMatch() {
		return closeMatch;
	}

	public void setCloseMatch(String[] closeMatch) {
		this.closeMatch = closeMatch;
	}

	@JsonProperty(WebEntityFields.IN_SCHEMA)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_IN_SCHEMA)
	public String[] getInScheme() {
		return inScheme;
	}

	public void setInScheme(String[] inScheme) {
		this.inScheme = inScheme;
	}

	@JsonProperty(WebEntityFields.NOTATION)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTATION)
	public Map<String, List<String>> getNotation() {
		return notation;
	}

	public void setNotation(Map<String, List<String>> notation) {
		this.notation = notation;
	}

}
