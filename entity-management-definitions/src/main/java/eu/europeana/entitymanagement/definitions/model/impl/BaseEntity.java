package eu.europeana.entitymanagement.definitions.model.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eu.europeana.entitymanagement.definitions.model.EntityRoot;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.definitions.model.vocabulary.XmlFields;


/*
 * TODO: Define the Jackson annotations, both xml and json, in one place, meaning in this class here and the corresponding extended classes 
 */
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class BaseEntity implements EntityRoot {

	protected String TMP_KEY = "def";
	private String internalType;
	private String entityId;
	// depiction
	private String depiction;
	private Map<String, List<String>> note;
	private Map<String, String> prefLabel;
	private Map<String, List<String>> altLabel;
	private Map<String, List<String>> hiddenLabel;
	private Map<String, List<String>> tmpPrefLabel;
	
	private String identifier[];
	private String[] sameAs;
	private String[] isRelatedTo;

	// hierarchical structure available only for a part of entities. Add set/get
	// methods to the appropriate interfaces
	private String[] hasPart;
	private String[] isPartOf;

	// The time at which the Set was created by the user. 
	private Date created;

	// The time at which the Set was modified, after creation. 
	private Date modified;
	
	protected WebResource referencedWebResource;
	
	@Override
	@JsonIgnore
	public WebResource getReferencedWebResource() {
		return referencedWebResource;
	}
	
	@JsonProperty(WebEntityFields.PREF_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_PREF_LABEL)
	public Map<String, String> getPrefLabelStringMap() {
		return prefLabel;
	}

	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		this.prefLabel = prefLabel;
	}

	@JsonProperty(WebEntityFields.ALT_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_ALT_LABEL)
	public Map<String, List<String>> getAltLabel() {
		return altLabel;
	}

	public void setAltLabel(Map<String, List<String>> altLabel) {
		this.altLabel = altLabel;
	}

	@JsonProperty(WebEntityFields.HIDDEN_LABEL)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_HIDDEN_LABEL)
	public Map<String, List<String>> getHiddenLabel() {
		return hiddenLabel;
	}

	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		this.hiddenLabel = hiddenLabel;
	}

	@JsonProperty(WebEntityFields.NOTE)
	@JacksonXmlProperty(localName = XmlFields.XML_SKOS_NOTE)
	public Map<String, List<String>> getNote() {
		return note;
	}

	public void setNote(Map<String, List<String>> note) {
		this.note = note;
	}


	@JsonProperty(WebEntityFields.TYPE)
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

	@JsonProperty(WebEntityFields.IDENTIFIER)
	@JacksonXmlProperty(localName = XmlFields.XML_DC_IDENTIFIER)
	public String[] getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String[] identifier) {
		this.identifier = identifier;
	}

	@JsonProperty(WebEntityFields.ID)
	@JacksonXmlProperty(isAttribute= true, localName = XmlFields.XML_RDF_ABOUT)
	public String getAbout() {
		return getEntityId();
	}

	public void setAbout(String about) {
		setEntityId(about);
	}

	@JsonProperty(WebEntityFields.IS_RELATED_TO)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_RELATED_TO)
	public String[] getIsRelatedTo() {
		return isRelatedTo;
	}

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

	@JsonProperty(WebEntityFields.HAS_PART)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_HAS_PART)
	public String[] getHasPart() {
		return hasPart;
	}

	public void setHasPart(String[] hasPart) {
		this.hasPart = hasPart;
	}

	@JsonProperty(WebEntityFields.IS_PART_OF)
	@JacksonXmlProperty(localName = XmlFields.XML_DCTERMS_IS_PART_OF)
	public String[] getIsPartOfArray() {
		return isPartOf;
	}

	public void setIsPartOfArray(String[] isPartOf) {
		this.isPartOf = isPartOf;
	}

	@JsonProperty(WebEntityFields.DEPICTION)
	@JacksonXmlProperty(localName = XmlFields.XML_FOAF_DEPICTION)
	public String getDepiction() {
		return depiction;
	}
	
	public void setDepiction(String depiction) {
		this.depiction = depiction;
	}

	@Override
	@JsonProperty(WebEntityFields.SAME_AS)
	@JacksonXmlProperty(localName = XmlFields.XML_OWL_SAME_AS)
	public String[] getSameAs() {
		return sameAs;
	}

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
	@JsonProperty(WebEntityFields.IS_SHOWN_BY)
	@JacksonXmlProperty(localName = XmlFields.XML_EDM_IS_SHOWN_BY)
	public String getIsShownBy() {
		if (referencedWebResource!=null)
		{
			return referencedWebResource.getId();
		}
		return null;
	}

	@Override
	public void setIsShownBy(WebResource resource) {
		referencedWebResource=resource;
	}

	@Override
	public String setInternalType(String internalTypeParam) {
		return internalType=internalTypeParam;
	}

}
