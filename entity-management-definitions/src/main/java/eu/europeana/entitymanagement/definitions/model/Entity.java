package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.ENTITY_CONTEXT;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.normalization.ValidEntityFields;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.groups.Default;

/*
 * TODO: Define the Jackson annotations, both xml and json, in one place, meaning in this class here and the corresponding extended classes
 */
@dev.morphia.annotations.Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Agent.class, name = "Agent"),
		@JsonSubTypes.Type(value = Concept.class, name = "Concept"),
		@JsonSubTypes.Type(value = Organization.class, name = "Organization"),
		@JsonSubTypes.Type(value = Place.class, name = "Place"),
		@JsonSubTypes.Type(value = Timespan.class, name = "Timespan")
}
)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ValidEntityFields(groups = {Default.class})
public abstract class Entity {

	public Entity() {
		// TODO Auto-generated constructor stub
	}

	protected String TMP_KEY = "def";
	protected String type;
	protected String entityId;
	// depiction
	protected String depiction;
	protected Map<String, List<String>> note;
	protected Map<String, String> prefLabel;
	protected Map<String, List<String>> altLabel;
	protected Map<String, List<String>> hiddenLabel;
	protected Map<String, List<String>> tmpPrefLabel;

	protected String[] identifier;
	protected String[] sameAs;
	protected String[] isRelatedTo;

	// hierarchical structure available only for a part of entities. Add set/get
	// methods to the appropriate interfaces
	protected String[] hasPart;
	protected String[] isPartOf;

	protected Aggregation isAggregatedBy;
	protected WebResource referencedWebResource;

	@JsonIgnore
	public WebResource getReferencedWebResource() {
		return referencedWebResource;
	}

	@JsonSetter
	public void setReferencedWebResource(WebResource resource) {
		this.referencedWebResource = resource;
	}

	/**
	 * Retrieves the preferable label for a contextual class (language,value)
	 *
	 * @return A Map<String, String> for the preferable labels of a contextual class
	 *         (one per language)
	 */
	@JsonGetter(WebEntityFields.PREF_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_PREF_LABEL)
	public Map<String, String> getPrefLabelStringMap() {
		return prefLabel;
	}

	@JsonSetter(WebEntityFields.PREF_LABEL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		this.prefLabel = prefLabel;
	}

	@JsonGetter(WebEntityFields.ALT_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_ALT_LABEL)
	public Map<String, List<String>> getAltLabel() {
		return altLabel;
	}

	@JsonSetter(WebEntityFields.ALT_LABEL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		this.altLabel = altLabel;
	}

	@JsonGetter(WebEntityFields.HIDDEN_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_HIDDEN_LABEL)
	public Map<String, List<String>> getHiddenLabel() {
		return hiddenLabel;
	}

	@JsonSetter(WebEntityFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		this.hiddenLabel = hiddenLabel;
	}

	@JsonGetter(WebEntityFields.NOTE)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTE)
	public Map<String, List<String>> getNote() {
		return note;
	}


	@JsonSetter(WebEntityFields.NOTE)
	public void setNote(Map<String, List<String>> note) {
		this.note = note;
	}


	@JsonGetter(WebEntityFields.TYPE)
	@JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
	public String getType() {
		return type;
	}

	@JsonSetter(WebEntityFields.TYPE)
	public void setType(String type) {
		this.type=type;
	}


	@JsonGetter(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getEntityId() {
		return entityId;
	}

	@JsonSetter(WebEntityFields.ID)
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@JsonGetter(WebEntityFields.IDENTIFIER)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_IDENTIFIER)
	public String[] getIdentifier() {
		return identifier;
	}

	@JsonSetter(WebEntityFields.IDENTIFIER)
	public void setIdentifier(String[] identifier) {
		this.identifier = identifier;
	}

	@JsonIgnore
	public String getAbout() {
		return getEntityId();
	}

	public void setAbout(String about) {
		setEntityId(about);
	}

	@JsonGetter(WebEntityFields.IS_RELATED_TO)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_RELATED_TO)
	public String[] getIsRelatedTo() {
		return isRelatedTo;
	}

	@JsonSetter(WebEntityFields.IS_RELATED_TO)
	public void setIsRelatedTo(String[] isRelatedTo) {
		this.isRelatedTo = isRelatedTo;
	}


	@JsonGetter(WebEntityFields.HAS_PART)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_HAS_PART)
	public String[] getHasPart() {
		return hasPart;
	}

	@JsonSetter(WebEntityFields.HAS_PART)
	public void setHasPart(String[] hasPart) {
		this.hasPart = hasPart;
	}

	@JsonGetter(WebEntityFields.IS_PART_OF)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_IS_PART_OF)
	public String[] getIsPartOfArray() {
		return isPartOf;
	}

	@JsonSetter(WebEntityFields.IS_PART_OF)
	public void setIsPartOfArray(String[] isPartOf) {
		this.isPartOf = isPartOf;
	}

	@JsonGetter(WebEntityFields.DEPICTION)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_DEPICTION)
	public String getDepiction() {
		return depiction;
	}

	@JsonSetter(WebEntityFields.DEPICTION)
	public void setDepiction(String depiction) {
		this.depiction = depiction;
	}


	@JsonGetter(WebEntityFields.SAME_AS)
	@JacksonXmlProperty(localName = XmlFields.XML_OWL_SAME_AS)
	public String[] getSameAs() {
		return sameAs;
	}


	@JsonSetter(WebEntityFields.SAME_AS)
	public void setSameAs(String[] sameAs) {
		this.sameAs = sameAs;
	}

	public void setFoafDepiction(String foafDepiction) {
		setDepiction(foafDepiction);
	}


	public String getFoafDepiction() {
		return getDepiction();
	}


	public Map<String, List<String>> getPrefLabel() {
		//if not available
		if (getPrefLabelStringMap() == null)
			return null;
		//if not transformed
		if (tmpPrefLabel == null)
			tmpPrefLabel = fillTmpMapToMap(getPrefLabelStringMap());

		return tmpPrefLabel;
	}

	/**
	 * This method converts List<String> to Map<String, List<String>>
	 * @param list of strings
	 */
	protected Map<String, List<String>> fillTmpMap(List<String> list) {

		Map<String, List<String>> tmpMap = new HashMap<String, List<String>>();
		tmpMap.put(TMP_KEY, list);
		return tmpMap;
	}

	/**
	 * This method converts  Map<String, String> to Map<String, List<String>>
	 * @param mapOfStrings map of strings
	 */
	protected Map<String, List<String>> fillTmpMapToMap(Map<String, String> mapOfStrings) {

		Map<String, List<String>> tmpMap = null;
		tmpMap = mapOfStrings.entrySet().stream().collect(Collectors.toMap(
				entry -> entry.getKey(),
				entry -> Collections.singletonList(entry.getValue()))
		);

		return tmpMap;
	}

	public void setPrefLabel(Map<String, List<String>> prefLabel) {
		// TODO Auto-generated method stub
	}

	public void setOwlSameAs(String[] owlSameAs) {
		setSameAs(sameAs);

	}

	public String[] getOwlSameAs() {
		return getSameAs();
	}


	/*
	 * Warning: Please note that for the serialization of this field where the given getter is used, only the id is serialized
	 * which makes it impossible to deserialize the object from its serialization, since for the deserialization the given setter
	 * is used and it needs the whole object, not just the id
	 */
	@JsonGetter(WebEntityFields.IS_SHOWN_BY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_SHOWN_BY)
	public String getIsShownBy() {
		if (referencedWebResource!=null)
		{
			return referencedWebResource.getId();
		}
		return null;
	}

	@JsonSetter(WebEntityFields.IS_SHOWN_BY)
	public void setIsShownBy(WebResource resource) {
		referencedWebResource=resource;
	}

	public Object getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the getters for each field individually
		return field.get(this);
	}

	public void setFieldValue(Field field, Object value) throws IllegalArgumentException, IllegalAccessException {
		//TODO:in case of the performance overhead cause by using the reflecion code, change this method to call the setter for each field individually
		field.set(this, value);
	}



	@JsonGetter(WebEntityFields.IS_AGGREGATED_BY)
	@JacksonXmlProperty(localName = XmlFields.XML_ORE_IS_AGGREGATED_BY)
	public Aggregation getIsAggregatedBy() {
		return isAggregatedBy;
	}

	/**
	 * Not included in XML responses
	 */
	@JsonGetter(WebEntityFields.CONTEXT)
	public String getContext() {
		return ENTITY_CONTEXT;
	}

	@JsonSetter(WebEntityFields.IS_AGGREGATED_BY)
	public void setIsAggregatedBy(Aggregation isAggregatedBy) {
		this.isAggregatedBy = isAggregatedBy;
	}

	public void copyShellFrom(Entity original) {
		this.entityId = original.getEntityId();
		this.isAggregatedBy = original.getIsAggregatedBy();
	}
}
