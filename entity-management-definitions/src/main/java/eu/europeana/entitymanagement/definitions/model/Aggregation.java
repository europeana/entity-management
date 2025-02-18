package eu.europeana.entitymanagement.definitions.model;

import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import dev.morphia.annotations.Embedded;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embedded
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
  ID,
  TYPE,
  CREATED,
  MODIFIED,
  PAGE_RANK,
  RECORD_COUNT,
  SCORE,
  AGGREGATES,
  ENRICH
})
public class Aggregation {

  private String id;
  private String type;
  private String rights;
  private String source;
  private Date created;
  private Date modified;
  private Boolean enrich;

  private Integer score;

  private Integer recordCount;
  private Double pageRank;

  private List<String> aggregates;

  public Aggregation() {}

  public Aggregation(Aggregation copy) {
    this.id = copy.getId();
    this.type = copy.getAggregationType();
    this.rights = copy.getRights();
    this.source = copy.getSource();
    this.created = copy.getCreated();
    this.modified = copy.getModified();
    this.score = copy.getScore();
    this.recordCount = copy.getRecordCount();
    this.pageRank = copy.getPageRank();
    if (copy.getAggregates() != null) this.aggregates = new ArrayList<>(copy.getAggregates());
    if (copy.getEnrich() != null) this.enrich = copy.getEnrich();
  }

  @JsonGetter(ID)
  public String getId() {
    return id;
  }

  @JsonSetter(ID)
  public void setId(String id) {
    this.id = id;
  }

  @JsonGetter(TYPE)
  public String getAggregationType() {
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

  @Override
  public String toString() {
    return "Aggregation{"
        + "id='"
        + id
        + '\''
        + ", type='"
        + type
        + '\''
        + ", rights='"
        + rights
        + '\''
        + ", source='"
        + source
        + '\''
        + ", created="
        + created
        + ", modified="
        + modified
        + ", enrich='"
        + enrich
        + '\''
        + '}';
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
  public Double getPageRank() {
    return pageRank;
  }

  @JsonSetter(PAGE_RANK)
  public void setPageRank(Double pageRank) {
    this.pageRank = pageRank;
  }

  @JsonGetter(RECORD_COUNT)
  public Integer getRecordCount() {
    return recordCount;
  }

  @JsonSetter(RECORD_COUNT)
  public void setRecordCount(Integer recordCount) {
    this.recordCount = recordCount;
  }

  @JsonGetter(SCORE)
  public Integer getScore() {
    return score;
  }

  @JsonSetter(SCORE)
  public void setScore(Integer score) {
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

  @JsonGetter(ENRICH)
  public Boolean getEnrich() {
    return enrich;
  }

  @JsonSetter(ENRICH)
  public void setEnrich(Boolean enrich) {
    this.enrich = enrich;
  }
}
