package eu.europeana.entitymanagement.solr.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.TimespanSolrFields;

import static eu.europeana.entitymanagement.solr.SolrUtils.SOLR_TIMESPAN_SUGGESTER_FILTER;

@JsonFilter(SOLR_TIMESPAN_SUGGESTER_FILTER)
public class SolrTimespan extends SolrEntity<Timespan> {
	
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
		
		this.isNextInSequence=timespan.getIsNextInSequence();
		this.begin=timespan.getBeginString();
		this.end=timespan.getEndString();
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
}
