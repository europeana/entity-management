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
import dev.morphia.annotations.Transient;
import eu.europeana.entitymanagement.normalization.ValidEntityFields;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.groups.Default;
import org.bson.types.ObjectId;

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

	public Entity () {
		
	}
	
	public Entity(Entity copy) {
		this.type = copy.getType();
		this.entityId = copy.getEntityId();
		this.depiction = copy.getDepiction();
		if(copy.getNote()!=null) this.note = new HashMap<>(copy.getNote());
		if(copy.getPrefLabel()!=null) this.prefLabel = new HashMap<>(copy.getPrefLabelStringMap());
		if(copy.getAltLabel()!=null) this.altLabel = new HashMap<>(copy.getAltLabel());
		if(copy.getHiddenLabel()!=null) this.hiddenLabel = new HashMap<>(copy.getHiddenLabel());
		if(copy.getIdentifier()!=null) this.identifier = new ArrayList<>(copy.getIdentifier());
		if(copy.getSameAs()!=null) this.sameAs = new ArrayList<>(copy.getSameAs());
		if(copy.getIsRelatedTo()!=null) this.isRelatedTo = new ArrayList<>(copy.getIsRelatedTo());
		if(copy.getHasPart()!=null) this.hasPart = new ArrayList<>(copy.getHasPart());
		if(copy.getIsPartOfArray()!=null) this.isPartOf = new ArrayList<>(copy.getIsPartOfArray());
		if(copy.getIsAggregatedBy()!=null) this.isAggregatedBy=new Aggregation(copy.getIsAggregatedBy());
		if(copy.getReferencedWebResource()!=null) this.referencedWebResource = new WebResource(copy.getReferencedWebResource());
		this.payload = copy.getPayload();
	}


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

	protected List<String> identifier;
	protected List<String> sameAs;
	protected List<String> isRelatedTo;

	// hierarchical structure available only for a part of entities. Add set/get
	// methods to the appropriate interfaces
	protected List<String> hasPart;
	protected List<String> isPartOf;

	protected Aggregation isAggregatedBy;
	protected WebResource referencedWebResource;
	protected String payload;


	@JsonIgnore
	public WebResource getReferencedWebResource() {
		return referencedWebResource;
	}


	@JsonSetter
	public void setReferencedWebResource(WebResource resource) {
		this.referencedWebResource = resource;
	}

	@JsonGetter(WebEntityFields.PREF_LABEL)
	public Map<String, String> getPrefLabelStringMap() {
		return prefLabel;
	}


	@JsonSetter(WebEntityFields.PREF_LABEL)
	public void setPrefLabelStringMap(Map<String, String> prefLabel) {
		this.prefLabel = prefLabel;
	}

	@JsonGetter(WebEntityFields.ALT_LABEL)
	public Map<String, List<String>> getAltLabel() {
		return altLabel;
	}


	@JsonSetter(WebEntityFields.ALT_LABEL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		this.altLabel = altLabel;
	}

	@JsonGetter(WebEntityFields.HIDDEN_LABEL)
	public Map<String, List<String>> getHiddenLabel() {
		return hiddenLabel;
	}


	@JsonSetter(WebEntityFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		this.hiddenLabel = hiddenLabel;
	}

	@JsonGetter(WebEntityFields.NOTE)
	public Map<String, List<String>> getNote() {
		return note;
	}


	@JsonSetter(WebEntityFields.NOTE)
	public void setNote(Map<String, List<String>> note) {
		this.note = note;
	}


	@JsonGetter(WebEntityFields.TYPE)
	public String getType() {
		return type;
	}


	@JsonSetter(WebEntityFields.TYPE)
	public void setType(String type) {
		this.type=type;
	}




	@JsonGetter(WebEntityFields.ID)
	public String getEntityId() {
		return entityId;
	}


	@JsonSetter(WebEntityFields.ID)
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@JsonGetter(WebEntityFields.IDENTIFIER)
	public List<String> getIdentifier() {
		return identifier;
	}


	@JsonSetter(WebEntityFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		this.identifier = identifier;
	}

	@JsonIgnore
	public String getAbout() {
		return getEntityId();
	}

	public void setAbout(String about) {
		setEntityId(about);
		setEntityIdentifier();
	}

	@JsonGetter(WebEntityFields.IS_RELATED_TO)
	public List<String> getIsRelatedTo() {
		return isRelatedTo;
	}


	@JsonSetter(WebEntityFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		this.isRelatedTo = isRelatedTo;
	}



	@JsonGetter(WebEntityFields.HAS_PART)
	public List<String> getHasPart() {
		return hasPart;
	}


	@JsonSetter(WebEntityFields.HAS_PART)
	public void setHasPart(List<String> hasPart) {
		this.hasPart = hasPart;
	}


	@JsonGetter(WebEntityFields.IS_PART_OF)
	public List<String> getIsPartOfArray() {
		return isPartOf;
	}


	@JsonSetter(WebEntityFields.IS_PART_OF)
	public void setIsPartOfArray(List<String> isPartOf) {
		this.isPartOf = isPartOf;
	}

	@JsonGetter(WebEntityFields.DEPICTION)
	public String getDepiction() {
		return depiction;
	}


	@JsonSetter(WebEntityFields.DEPICTION)
	public void setDepiction(String depiction) {
		this.depiction = depiction;
	}


	@JsonGetter(WebEntityFields.SAME_AS)
	public List<String> getSameAs() {
		return sameAs;
	}


	@JsonSetter(WebEntityFields.SAME_AS)
	public void setSameAs(List<String> sameAs) {
		this.sameAs = sameAs;
	}


	@Deprecated
	@JsonIgnore
	public void setFoafDepiction(String foafDepiction) {
		setDepiction(foafDepiction);
	}


	public String getFoafDepiction() {
		return getDepiction();
	}


	@Deprecated
	public ObjectId getId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Deprecated
	@JsonIgnore
	public void setId(ObjectId id) {
		// TODO Auto-generated method stub
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


	@Deprecated
	@JsonIgnore
	public void setPrefLabel(Map<String, List<String>> prefLabel) {
		// TODO Auto-generated method stub
	}

	@Deprecated
	@JsonIgnore
	public void setOwlSameAs(List<String> owlSameAs) {
		setSameAs(sameAs);

	}

	public List<String> getOwlSameAs() {
		return getSameAs();
	}


	public String getEntityIdentifier() {
		return this.entityIdentifier;
	}

	/*
	 * Warning: Please note that for the serialization of this field where the given getter is used, only the id is serialized
	 * which makes it impossible to deserialize the object from its serialization, since for the deserialization the given setter
	 * is used and it needs the whole object, not just the id
	 */

	@JsonGetter(WebEntityFields.IS_SHOWN_BY)
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
	
	private void setEntityIdentifier(){
		String[] splitArray = this.getAbout().split("/");
		this.entityIdentifier =  splitArray[splitArray.length-1];
	}

	@JsonIgnore
	public String getPayload() {
		return payload;
	}


	public void setPayload(String payload) {
		this.payload = payload;
	}

}
