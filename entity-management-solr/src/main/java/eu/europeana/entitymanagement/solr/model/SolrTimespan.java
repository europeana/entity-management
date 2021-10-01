package eu.europeana.entitymanagement.solr.model;

import static eu.europeana.entitymanagement.solr.SolrUtils.SOLR_TIMESPAN_SUGGESTER_FILTER;

import com.fasterxml.jackson.annotation.JsonFilter;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.TimespanSolrFields;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;

@JsonFilter(SOLR_TIMESPAN_SUGGESTER_FILTER)
public class SolrTimespan extends SolrEntity<Timespan> {

  @Field(EntitySolrFields.SAME_AS)
  private List<String> sameAs;

  @Field(TimespanSolrFields.IS_NEXT_IN_SEQUENCE)
  private List<String> isNextInSequence;

  @Field(TimespanSolrFields.BEGIN)
  private String begin;

  @Field(TimespanSolrFields.END)
  private String end;

  public SolrTimespan() {
    super();
  }

  public SolrTimespan(Timespan timespan) {
    super(timespan);

    this.isNextInSequence = timespan.getIsNextInSequence();
    this.begin = timespan.getBeginString();
    this.end = timespan.getEndString();
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
