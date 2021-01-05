package eu.europeana.entity.definitions.model.impl;

import java.util.Date;
import java.util.List;

import eu.europeana.entity.definitions.model.Aggregation;

public class BaseAggregation implements Aggregation{

    String id, type, rights, source;
    Date created, modified;
    int pageRank, recordCount, score;
    List<String> aggregates;
    
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    @Override
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @Override
    public String getRights() {
        return rights;
    }
    @Override
    public void setRights(String rights) {
        this.rights = rights;
    }
    @Override
    public String getSource() {
        return source;
    }
    @Override
    public void setSource(String source) {
        this.source = source;
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
    public int getPageRank() {
        return pageRank;
    }
    @Override
    public void setPageRank(int pageRank) {
        this.pageRank = pageRank;
    }
    @Override
    public int getRecordCount() {
        return recordCount;
    }
    @Override
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }
    @Override
    public int getScore() {
        return score;
    }
    @Override
    public void setScore(int score) {
        this.score = score;
    }
    @Override
    public List<String> getAggregates() {
        return aggregates;
    }
    @Override
    public void setAggregates(List<String> aggregates) {
        this.aggregates = aggregates;
    }
    
    
}
