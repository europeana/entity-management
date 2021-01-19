package eu.europeana.entitymanagement.scoring.model;

public class EntityMetrics {

    Integer enrichmentCount, hitCount, score;
    Double pageRank;
    
    public Integer getEnrichmentCount() {
        return enrichmentCount;
    }
    public void setEnrichmentCount(Integer enrichmentCount) {
        this.enrichmentCount = enrichmentCount;
    }
    public Integer getHitCount() {
        return hitCount;
    }
    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }
    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }
    public Double getPageRank() {
        return pageRank;
    }
    public void setPageRank(Double pageRank) {
        this.pageRank = pageRank;
    }
    
}
