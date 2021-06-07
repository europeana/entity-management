package eu.europeana.entitymanagement.solr.model;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import org.apache.solr.client.solrj.beans.Field;

import java.util.List;
import java.util.Map;

public abstract class SolrEntity<T extends Entity> {

    @Field(AgentSolrFields.TYPE)
    private String type;

    @Field(AgentSolrFields.ID)
    private String entityId;

    @Field(AgentSolrFields.DEPICTION)
    private String depiction;

    @Field(AgentSolrFields.NOTE_ALL)
    private Map<String, List<String>> note;

    @Field(AgentSolrFields.PREF_LABEL_ALL)
    private Map<String, String> prefLabel;

    @Field(AgentSolrFields.ALT_LABEL_ALL)
    private Map<String, List<String>> altLabel;

    @Field(AgentSolrFields.HIDDEN_LABEL)
    private Map<String, List<String>> hiddenLabel;

    @Field(AgentSolrFields.IDENTIFIER)
    private List<String> identifier;

    @Field(AgentSolrFields.SAME_AS)
    private List<String> sameAs;

    @Field(AgentSolrFields.IS_RELATED_TO)
    private List<String> isRelatedTo;

    @Field(AgentSolrFields.HAS_PART)
    private List<String> hasPart;

    @Field(AgentSolrFields.IS_PART_OF)
    private List<String> isPartOf;

    @Field(AgentSolrFields.PAYLOAD)
    private String payload;

    public SolrEntity(T entity) {
        this.type = entity.getType();
        this.entityId = entity.getEntityId();
        this.depiction = entity.getDepiction();
        setNote(entity.getNote());
        setPrefLabelStringMap(entity.getPrefLabelStringMap());
        setAltLabel(entity.getAltLabel());
        setHiddenLabel(entity.getHiddenLabel());
        this.identifier = entity.getIdentifier();
        this.sameAs = entity.getSameAs();
        this.isRelatedTo = entity.getIsRelatedTo();
        this.hasPart = entity.getHasPart();
        this.isPartOf = entity.getIsPartOfArray();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    private void setPrefLabelStringMap(Map<String, String> prefLabel) {
        this.prefLabel = SolrUtils.normalizeStringMapByAddingPrefix(
                AgentSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel);
    }

    private void setAltLabel(Map<String, List<String>> altLabel) {
        this.altLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, altLabel);
    }

    private void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
        this.hiddenLabel = SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, hiddenLabel);
    }

    private void setNote(Map<String, List<String>> note) {
        this.note = SolrUtils.normalizeStringListMapByAddingPrefix(
                AgentSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note);
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
