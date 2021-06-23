package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import eu.europeana.entitymanagement.serialization.PositiveNumberFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ID, TYPE, CREATED, MODIFIED, PAGE_RANK, RECORD_COUNT, SCORE, AGGREGATES})
public class Aggregation {
	
	public Aggregation() {
		
	}
    
    public Aggregation(Aggregation copy) {
		this.id = copy.getId();
		this.type = copy.getType();
		this.rights = copy.getRights();
		this.source = copy.getSource();
		this.created = copy.getCreated();
		this.modified = copy.getModified();
		this.score = copy.getScore();
		this.recordCount = copy.getRecordCount();
		this.pageRank = copy.getPageRank();
		if(copy.getAggregates()!=null) this.aggregates = new ArrayList<>(copy.getAggregates());
	}

	String id, type, rights, source;
    Date created, modified;
    int score, recordCount;
    double pageRank;
    List<String> aggregates;

    
    @JsonGetter(ID)
    public String getId() {
        return id;
    }
    
    @JsonSetter(ID)
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonGetter(TYPE)
    public String getType() {
        return AGGREGATION;
    }


    
    @JsonGetter(RIGHTS)
    public String getRights() {
        return rights;
    }
    
    @JsonSetter(RIGHTS)
    public void setRights(String rights) {
        this.rights = rights;
    }
    
    @JsonGetter(SOURCE)
    public String getSource() {
        return source;
    }
    
    @JsonSetter(SOURCE)
    public void setSource(String source) {
        this.source = source;
    }
    
    @JsonGetter(CREATED)
    public Date getCreated() {
        return created;
    }
    
    @JsonSetter(CREATED)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @JsonGetter(MODIFIED)
    public Date getModified() {
        return modified;
    }
    
    @JsonSetter(MODIFIED)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    @JsonGetter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public double getPageRank() {
        return pageRank;
    }

    
    @JsonSetter

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    
    @JsonGetter
    public int getRecordCount() {
        return recordCount;
    }
    
    @JsonSetter
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    
    @JsonGetter
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public int getScore() {
        return score;
    }
    
    @JsonSetter
    public void setScore(int score) {
        this.score = score;
    }
    
    @JsonGetter(AGGREGATES)
    public List<String> getAggregates() {
        return aggregates;
    }
    
    @JsonSetter(AGGREGATES)
    public void setAggregates(List<String> aggregates) {
        this.aggregates = aggregates;
    }


}
