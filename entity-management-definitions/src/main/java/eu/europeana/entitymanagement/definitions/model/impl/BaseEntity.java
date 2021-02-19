package eu.europeana.entitymanagement.definitions.model.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.groups.Default;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;
import eu.europeana.entitymanagement.normalization.ValidEntityFields;

/*
 * TODO: Define the Jackson annotations, both xml and json, in one place, meaning in this class here and the corresponding extended classes 
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ValidEntityFields(groups = {Default.class})
public class BaseEntity implements Entity {
	
	protected String TMP_KEY = "def";
	protected String internalType;
	protected String entityId;
	// depiction
	protected String depiction;
	protected Map<String, List<String>> note;
	protected Map<String, String> prefLabel;
	protected Map<String, List<String>> altLabel;
	protected Map<String, List<String>> hiddenLabel;
	protected Map<String, List<String>> tmpPrefLabel;
	
	protected String identifier[];
	protected String[] sameAs;
	protected String[] isRelatedTo;

	// hierarchical structure available only for a part of entities. Add set/get
	// methods to the appropriate interfaces
	protected String[] hasPart;
	protected String[] isPartOf;

	// The time at which the Set was created by the user. 
	protected Date created;

	// The time at which the Set was modified, after creation. 
	protected Date modified;
	
	protected WebResource referencedWebResource;

	@Override
	@JsonIgnore
	public WebResource getReferencedWebResource() {
		return referencedWebResource;
	}
	
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
	public String getInternalType() {
		return internalType;
	}

//	public String[] getSameAs() {
//		return sameAs;
//	}
//
//	public void setSameAs(String[] sameAs) {
//		this.sameAs = sameAs;
//	}

	
	public String getEntityId() {
		return entityId;
	}

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

	@JsonGetter(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getAbout() {
		return getEntityId();
	}

	@JsonSetter(WebEntityFields.ID)
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

//	@Override
//	public ObjectId getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setId(ObjectId id) {
//		// TODO Auto-generated method stub
//
//	}

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

	@Override
	@JsonGetter(WebEntityFields.SAME_AS)
	@JacksonXmlProperty(localName = XmlFields.XML_OWL_SAME_AS)
	public String[] getSameAs() {
		return sameAs;
	}

	@JsonSetter(WebEntityFields.SAME_AS)
	public void setSameAs(String[] sameAs) {
		this.sameAs = sameAs;
	}
	
	@Override
	@Deprecated
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
	 * @param map of strings
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
	public void setPrefLabel(Map<String, List<String>> prefLabel) {
		// TODO Auto-generated method stub
	}
	
	@Deprecated
	public void setOwlSameAs(String[] owlSameAs) {
		setSameAs(sameAs);
		
	}

	public String[] getOwlSameAs() {
		return getSameAs();
	}

	@Override
	public String getEntityIdentifier() {
		String[] splitArray = this.getAbout().split("/");
		return splitArray[splitArray.length-1];
	}

	
	@Override
	public Date getCreated() {
		return created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public void setModified(Date modified) {
		this.modified = modified;
	}

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
	@JsonSetter(WebEntityFields.TYPE)
	public String setInternalType(String internalTypeParam) {
		return internalType=internalTypeParam;
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
