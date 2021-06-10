package eu.europeana.entitymanagement.solr.model;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.SolrUtils;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SolrEntity<T extends Entity> {
	
	protected T entity;

    @Field(EntitySolrFields.TYPE)
    private String type;

    @Field(EntitySolrFields.ID)
    private String entityId;

    @Field(EntitySolrFields.DEPICTION)
    private String depiction;

    @Field(EntitySolrFields.NOTE_ALL)
    private Map<String, List<String>> note;

    @Field(EntitySolrFields.PREF_LABEL_ALL)
    private Map<String, String> prefLabel;

    @Field(EntitySolrFields.ALT_LABEL_ALL)
    private Map<String, List<String>> altLabel;

    @Field(EntitySolrFields.HIDDEN_LABEL)
    private Map<String, List<String>> hiddenLabel;

    @Field(EntitySolrFields.IDENTIFIER)
    private List<String> identifier;

    @Field(EntitySolrFields.SAME_AS)
    private List<String> sameAs;

    @Field(EntitySolrFields.IS_RELATED_TO)
    private List<String> isRelatedTo;

    @Field(EntitySolrFields.HAS_PART)
    private List<String> hasPart;

    @Field(EntitySolrFields.IS_PART_OF)
    private List<String> isPartOf;

    @Field(EntitySolrFields.PAYLOAD)
    private String payload;

    public SolrEntity(T entity) {
        this.type = entity.getType();
        this.entityId = entity.getEntityId();
        this.depiction = entity.getDepiction();
        setNote(entity.getNote());
        setPrefLabelStringMap(entity.getPrefLabelStringMap());
        setAltLabel(entity.getAltLabel());
        setHiddenLabel(entity.getHiddenLabel());
        if(entity.getIdentifier()!=null) this.identifier = new ArrayList<>(entity.getIdentifier());
        if(entity.getSameAs()!=null) this.sameAs = new ArrayList<>(entity.getSameAs());
        if(entity.getIsRelatedTo()!=null) this.isRelatedTo = new ArrayList<>(entity.getIsRelatedTo());
        if(entity.getHasPart()!=null) this.hasPart = new ArrayList<>(entity.getHasPart());
        if(entity.getIsPartOfArray()!=null) this.isPartOf = new ArrayList<>(entity.getIsPartOfArray());
        this.entity=entity;
    }
    
    public SolrEntity() {
    
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    private void setPrefLabelStringMap(Map<String, String> prefLabel) {
    	if (prefLabel!=null) {
    		this.prefLabel = new HashMap<>(SolrUtils.normalizeStringMapByAddingPrefix(
                AgentSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel));
    	}
    }

    private void setAltLabel(Map<String, List<String>> altLabel) {
    	if (altLabel!=null) {
    		this.altLabel = new HashMap<>(SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel));
    	}
    }

    private void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
    	if (hiddenLabel!=null) {
    		this.hiddenLabel = new HashMap<>(SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, hiddenLabel));
    	}
    }

    private void setNote(Map<String, List<String>> note) {
    	if (note!=null) {
    		this.note = new HashMap<>(SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note));
    	}
    }

    public String getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDepiction() {
        return depiction;
    }

    public Map<String, List<String>> getNote() {
        return note;
    }

    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public Map<String, List<String>> getAltLabel() {
        return altLabel;
    }

    public Map<String, List<String>> getHiddenLabel() {
        return hiddenLabel;
    }

    public List<String> getIdentifier() {
        return identifier;
    }

    public List<String> getSameAs() {
        return sameAs;
    }

    public List<String> getIsRelatedTo() {
        return isRelatedTo;
    }

    public List<String> getHasPart() {
        return hasPart;
    }

    public List<String> getIsPartOf() {
        return isPartOf;
    }

    public String getPayload() {
        return payload;
    }
}
