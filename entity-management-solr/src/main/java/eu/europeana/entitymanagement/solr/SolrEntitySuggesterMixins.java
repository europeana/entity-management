package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.solr.SolrUtils.*;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Solr-specific @JsonFilter annotations cannot be applied to Entity classes, as that would affect
 * the JSON output returned by the API.
 *
 * <p>Instead these mixins are used by the ObjectMapper (in SolrService) to filter out JSON
 * properties when generating the suggester payload.
 */
public class SolrEntitySuggesterMixins {

  @JsonFilter(SOLR_AGENT_SUGGESTER_FILTER)
  public static class AgentSuggesterMixin {}

  @JsonFilter(SOLR_ORGANIZATION_SUGGESTER_FILTER)
  public static class OrganizationSuggesterMixin {}

  @JsonFilter(SOLR_TIMESPAN_SUGGESTER_FILTER)
  public static class TimespanSuggesterMixin {}
}
