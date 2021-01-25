package eu.europeana.entitymanagement.scoring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "metrics")
public class EntityMetrics {

    Integer enrichmentCount, hitCount, score, pageRank;
    String entityId, entityType;

    public EntityMetrics() {

    }

    public EntityMetrics(String entityId) {
	this.entityId = entityId;
    }

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

    public Integer getPageRank() {
	return pageRank;
    }

    public void setPageRank(Integer pageRank) {
	this.pageRank = pageRank;
    }

    public String getEntityId() {
	return entityId;
    }

    public void setEntityId(String entityId) {
	this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

}
