package eu.europeana.entitymanagement.definitions.model.impl;

import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.ENTITY_CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.CONTEXT;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.ID;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.TYPE;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.groups.Default;

import dev.morphia.annotations.Transient;
import eu.europeana.entitymanagement.common.config.ComparisonUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.normalization.ValidEntityFields;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

/*
 * TODO: Define the Jackson annotations, both xml and json, in one place, meaning in this class here and the corresponding extended classes 
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ValidEntityFields(groups = {Default.class})
public class BaseEntity implements Entity {
	
	public BaseEntity() {
		// TODO Auto-generated constructor stub
	}

	protected String TMP_KEY = "def";
	protected String type;
	protected String entityId;
	// ID of entityRecord in database
	@Transient
	protected String entityIdentifier;
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

	@Override
	@JsonIgnore
	public WebResource getReferencedWebResource() {
		return referencedWebResource;
	}
	
	@Override
	@JsonSetter
	public void setReferencedWebResource(WebResource resource) {
		this.referencedWebResource = resource;
	}
	
	@JsonGetter(WebEntityFields.PREF_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_PREF_LABEL)
	public Map<String, String> getPrefLabelStringMap() {
		return prefLabel;
	}

	@Override
	@JsonSetter(WebEntityFields.PREF_LABEL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		this.prefLabel = prefLabel;
	}

	@JsonGetter(WebEntityFields.ALT_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_ALT_LABEL)
	public Map<String, List<String>> getAltLabel() {
		return altLabel;
	}

	@Override
	@JsonSetter(WebEntityFields.ALT_LABEL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		this.altLabel = altLabel;
	}

	@JsonGetter(WebEntityFields.HIDDEN_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_HIDDEN_LABEL)
	public Map<String, List<String>> getHiddenLabel() {
		return hiddenLabel;
	}

	@Override
	@JsonSetter(WebEntityFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		this.hiddenLabel = hiddenLabel;
	}

	@JsonGetter(WebEntityFields.NOTE)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTE)
	public Map<String, List<String>> getNote() {
		return note;
	}

	@Override
	@JsonSetter(WebEntityFields.NOTE)
	public void setNote(Map<String, List<String>> note) {
		this.note = note;
	}


	@JsonGetter(WebEntityFields.TYPE)
	@JacksonXmlProperty(localName = XmlFields.XML_RDF_TYPE)
	public String getType() {
		return type;
	}

	@Override
	@JsonSetter(WebEntityFields.TYPE)
	public void setType(String type) {
		this.type=type;
	}



	@Override
	@JsonGetter(WebEntityFields.ID)
	public String getEntityId() {
		return entityId;
	}

	@Override
	@JsonSetter(WebEntityFields.ID)
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@JsonGetter(WebEntityFields.IDENTIFIER)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_IDENTIFIER)
	public String[] getIdentifier() {
		return identifier;
	}

	@Override
	@JsonSetter(WebEntityFields.IDENTIFIER)
	public void setIdentifier(String[] identifier) {
		this.identifier = identifier;
	}

	@JsonIgnore
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getAbout() {
		return getEntityId();
	}

	public void setAbout(String about) {
		setEntityId(about);
		setEntityIdentifier();
	}

	@JsonGetter(WebEntityFields.IS_RELATED_TO)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_RELATED_TO)
	public String[] getIsRelatedTo() {
		return isRelatedTo;
	}

	@Override
	@JsonSetter(WebEntityFields.IS_RELATED_TO)
	public void setIsRelatedTo(String[] isRelatedTo) {
		this.isRelatedTo = isRelatedTo;
	}


	@Override
	@JsonGetter(WebEntityFields.HAS_PART)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_HAS_PART)
	public String[] getHasPart() {
		return hasPart;
	}

	@Override
	@JsonSetter(WebEntityFields.HAS_PART)
	public void setHasPart(String[] hasPart) {
		this.hasPart = hasPart;
	}

	@Override
	@JsonGetter(WebEntityFields.IS_PART_OF)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_IS_PART_OF)
	public String[] getIsPartOfArray() {
		return isPartOf;
	}

	@Override
	@JsonSetter(WebEntityFields.IS_PART_OF)
	public void setIsPartOfArray(String[] isPartOf) {
		this.isPartOf = isPartOf;
	}

	@JsonGetter(WebEntityFields.DEPICTION)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_DEPICTION)
	public String getDepiction() {
		return depiction;
	}
	
	@Override
	@JsonSetter(WebEntityFields.DEPICTION)
	public void setDepiction(String depiction) {
		this.depiction = depiction;
	}

	@Override
	@JsonGetter(WebEntityFields.SAME_AS)
	@JacksonXmlProperty(localName = XmlFields.XML_OWL_SAME_AS)
	public String[] getSameAs() {
		return sameAs;
	}

	@Override
	@JsonSetter(WebEntityFields.SAME_AS)
	public void setSameAs(String[] sameAs) {
		this.sameAs = sameAs;
	}
	
	@Override
	@Deprecated
	@JsonIgnore
	public void setFoafDepiction(String foafDepiction) {
		setDepiction(foafDepiction);
	}

	@Override
	public String getFoafDepiction() {
		return getDepiction();
	}
	
	@Override
	@Deprecated
	public ObjectId getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	@JsonIgnore
	public void setId(ObjectId id) {
		// TODO Auto-generated method stub
	}

	@Override
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
	
	@Override
	@Deprecated
	@JsonIgnore
	public void setPrefLabel(Map<String, List<String>> prefLabel) {
		// TODO Auto-generated method stub
	}
	
	@Deprecated
	@JsonIgnore
	public void setOwlSameAs(String[] owlSameAs) {
		setSameAs(sameAs);
		
	}

	public String[] getOwlSameAs() {
		return getSameAs();
	}

	@Override
	public String getEntityIdentifier() {
		return this.entityIdentifier;
	}
	
	/*
	 * Warning: Please note that for the serialization of this field where the given getter is used, only the id is serialized
	 * which makes it impossible to deserialize the object from its serialization, since for the deserialization the given setter
	 * is used and it needs the whole object, not just the id
	 */
	@Override
	@JsonGetter(WebEntityFields.IS_SHOWN_BY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_SHOWN_BY)
	public String getIsShownBy() {
		if (referencedWebResource!=null)
		{
			return referencedWebResource.getId();
		}
		return null;
	}

	@Override
	@JsonSetter(WebEntityFields.IS_SHOWN_BY)
	public void setIsShownBy(WebResource resource) {
		referencedWebResource=resource;
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

	
	 
	@Override    
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

	@Override
	@JsonSetter(WebEntityFields.IS_AGGREGATED_BY)
	public void setIsAggregatedBy(Aggregation isAggregatedBy) {
	    this.isAggregatedBy = isAggregatedBy;
	}

	@Override
	public void copyShellFrom(Entity original) {
		this.entityId = original.getEntityId();
		this.isAggregatedBy = original.getIsAggregatedBy();
	}

	private void setEntityIdentifier(){
		String[] splitArray = this.getAbout().split("/");
		this.entityIdentifier =  splitArray[splitArray.length-1];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BaseEntity that = (BaseEntity) o;
		return Objects.equals(getDepiction(), that.getDepiction())
				&& ComparisonUtils.areMapsEqual(getNote(), that.getNote())
				&& ComparisonUtils.areMapsEqual(getPrefLabel(), that.getPrefLabel())
				&& ComparisonUtils.areMapsEqual(getAltLabel(), that.getAltLabel())
				&& ComparisonUtils.areMapsEqual(getHiddenLabel(), that.getHiddenLabel())
				&& ComparisonUtils.areMapsEqual(tmpPrefLabel, that.tmpPrefLabel)
				&& Arrays.equals(getIdentifier(), that.getIdentifier())
				&& Objects.equals(getIsAggregatedBy(), that.getIsAggregatedBy())
				&& Objects.equals(getReferencedWebResource(), that.getReferencedWebResource());
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(getDepiction(), getNote(), getPrefLabel(), getAltLabel(), getHiddenLabel(), tmpPrefLabel, getIsAggregatedBy(), getReferencedWebResource());
		result = 31 * result + Arrays.hashCode(getIdentifier());
		return result;
	}

}
