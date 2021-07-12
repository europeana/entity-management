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

    /**
     * Creates a new Aggregation instance
     * @param initializeNumericFields if true, pageRank, recordCount and score are initialized to zero, meaning
     *                                 they are included during serialization.
     */
	public Aggregation(boolean initializeNumericFields){
        if(initializeNumericFields){
            this.pageRank = 0;
            this.recordCount = 0;
            this.score = 0;
        }
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

    private String id;
	private String type;
	private String rights;
	private String source;
    private Date created;
    private Date modified;

    /**
     * Initialize numeric fields with negative values, so they are not serialized unless a value is
     * explicitly set
     */
    private int score= -1;
    private int recordCount= -1;
    private double pageRank= -1d;

    private List<String> aggregates;

    
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
    
    @JsonGetter(PAGE_RANK)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public double getPageRank() {
        return pageRank;
    }

    
    @JsonSetter(PAGE_RANK)
    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    
    @JsonGetter(RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public int getRecordCount() {
        return recordCount;
    }
    
    @JsonSetter(RECORD_COUNT)
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    
    @JsonGetter(SCORE)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = PositiveNumberFilter.class)
    public int getScore() {
        return score;
    }
    
    @JsonSetter(SCORE)
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
