package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.solr.SolrUtils.SOLR_TIMESPAN_SUGGESTER_FILTER;

import com.fasterxml.jackson.annotation.JsonFilter;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.TimeSpanSolrFields;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;

@JsonFilter(SOLR_TIMESPAN_SUGGESTER_FILTER)
public class SolrTimeSpan extends SolrEntity<TimeSpan> {

  @Field(EntitySolrFields.SAME_AS)
  private List<String> sameAs;

  @Field(TimeSpanSolrFields.IS_NEXT_IN_SEQUENCE)
  private List<String> isNextInSequence;

  @Field(TimeSpanSolrFields.BEGIN)
  private String begin;

  @Field(TimeSpanSolrFields.END)
  private String end;

  public SolrTimeSpan() {
    super();
  }

  public SolrTimeSpan(TimeSpan timespan) {
    super(timespan);

    this.isNextInSequence = timespan.getIsNextInSequence();
    this.begin = timespan.getBeginString();
    this.end = timespan.getEndString();
    if (timespan.getSameReferenceLinks() != null) {
      this.sameAs = new ArrayList<>(timespan.getSameReferenceLinks());
    }
  }

  public List<String> getIsNextInSequence() {
    return isNextInSequence;
  }

  public String getBegin() {
    return begin;
  }

  public String getEnd() {
    return end;
  }

  @Override
  protected void setSameReferenceLinks(ArrayList<String> uris) {
    this.sameAs = uris;
  }
}
