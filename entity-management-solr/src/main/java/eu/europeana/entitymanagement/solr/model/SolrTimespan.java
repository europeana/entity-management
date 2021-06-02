package eu.europeana.entitymanagement.solr.model;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.AgentSolrFields;
import eu.europeana.entitymanagement.vocabulary.ConceptSolrFields;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.TimespanSolrFields;

public class SolrTimespan extends Timespan {
	
	private String payload;

	public SolrTimespan() {
		super();
	}

	public SolrTimespan(Timespan timespan) {
		super();
		this.setType(timespan.getType());
		this.setEntityId(timespan.getEntityId());
		this.setDepiction(timespan.getDepiction());
		this.setNote(timespan.getNote());
		this.setPrefLabelStringMap(timespan.getPrefLabelStringMap());
		this.setAltLabel(timespan.getAltLabel());
		this.setHiddenLabel(timespan.getHiddenLabel());
		this.setIdentifier(timespan.getIdentifier());
		this.setSameAs(timespan.getSameAs());
		this.setIsRelatedTo(timespan.getIsRelatedTo());
		this.setHasPart(timespan.getHasPart());
		this.setIsPartOfArray(timespan.getIsPartOfArray());
		
		this.setIsNextInSequence(timespan.getIsNextInSequence());
		this.setBeginString(timespan.getBeginString());
		this.setEndString(timespan.getEndString());
	}

    @Override
    @Field(TimespanSolrFields.ID)
    public void setEntityId(String entityId) {
	super.setEntityId(entityId);
    }

    @Override
    @Field(TimespanSolrFields.TYPE)
    public void setType(String type) {
	super.setType(type);
    }

    @Override
    @Field(TimespanSolrFields.DEPICTION)
    public void setDepiction(String depiction) {
	super.setDepiction(depiction);
    }

    @Override
    @Field(TimespanSolrFields.PREF_LABEL_ALL)
    public void setPrefLabelStringMap(Map<String, String> prefLabel) {
	Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMapByAddingPrefix(ConceptSolrFields.PREF_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, prefLabel);
	super.setPrefLabelStringMap(normalizedPrefLabel);
    }

    @Override
    @Field(TimespanSolrFields.ALT_LABEL_ALL)
    public void setAltLabel(Map<String, List<String>> altLabel) {
	Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMapByAddingPrefix(ConceptSolrFields.ALT_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
		altLabel);
	super.setAltLabel(normalizedAltLabel);
    }

    @Override
    @Field(TimespanSolrFields.HIDDEN_LABEL_ALL)
    public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
	Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMapByAddingPrefix(ConceptSolrFields.HIDDEN_LABEL + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR,
		hiddenLabel);
	super.setHiddenLabel(normalizedHiddenLabel);
    }

    @Override
    @Field(TimespanSolrFields.NOTE_ALL)
    public void setNote(Map<String, List<String>> note) {
	Map<String, List<String>> normalizedNote = SolrUtils.normalizeStringListMapByAddingPrefix(ConceptSolrFields.NOTE + EntitySolrFields.DYNAMIC_FIELD_SEPARATOR, note);
	super.setNote(normalizedNote);
    }

    @Override
    @Field(TimespanSolrFields.HAS_PART)
    public void setHasPart(List<String> hasPart) {
	super.setHasPart(hasPart);
    }

    @Override
    @Field(TimespanSolrFields.IS_PART_OF)
    public void setIsPartOfArray(List<String> isPartOf) {
	super.setIsPartOfArray(isPartOf);
    }

    @Override
    @Field(TimespanSolrFields.SAME_AS)
    public void setSameAs(List<String> sameAs) {
	super.setSameAs(sameAs);
    }
    
	@Override
	@Field(TimespanSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(List<String> isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}
	
	@Override
	@Field(TimespanSolrFields.IDENTIFIER)
	public void setIdentifier(List<String> identifier) {
		super.setIdentifier(identifier);
	}

    @Override
    @Field(TimespanSolrFields.IS_NEXT_IN_SEQUENCE)
    public void setIsNextInSequence(List<String> isNextInSequence) {
	super.setIsNextInSequence(isNextInSequence);
    }

    @Override
    @Field(TimespanSolrFields.BEGIN)
    public void setBeginString(String begin) {
	super.setBeginString(begin);
    }
    
    @Override
    @Field(TimespanSolrFields.END)
    public void setEndString(String end) {
        super.setEndString(end);
    }

	public String getPayload() {
		return payload;
	}

	@Field(AgentSolrFields.PAYLOAD)
	public void setPayload(String payload) {
		this.payload = payload;
	}
}
