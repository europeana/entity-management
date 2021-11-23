package eu.europeana.entitymanagement.solr;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_SOLR_ENTITY_SUGGESTER_FILTER;
import static eu.europeana.entitymanagement.solr.SolrUtils.*;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrEntitySuggesterFilterConfig {

  /** Configures JSON payload fields for Solr suggester */
  @Bean(BEAN_SOLR_ENTITY_SUGGESTER_FILTER)
  public FilterProvider solrEntitySuggesterFilter() {
    SimpleFilterProvider filterProvider = new SimpleFilterProvider();
    filterProvider.addFilter(
        SOLR_AGENT_SUGGESTER_FILTER,
        SimpleBeanPropertyFilter.filterOutAllExcept(
            "id",
            "type",
            "isShownBy",
            "prefLabel",
            "altLabel",
            "hiddenLabel",
            "dateOfBirth",
            "dateOfDeath",
            "dateOfEstablishment",
            "dateOfTermination"));
    filterProvider.addFilter(
        SOLR_ORGANIZATION_SUGGESTER_FILTER,
        SimpleBeanPropertyFilter.filterOutAllExcept(
            "id",
            "type",
            "isShownBy",
            "prefLabel",
            "altLabel",
            "hiddenLabel",
            "acronym",
            "organizationDomain",
            "country"));
    filterProvider.addFilter(
        SOLR_TIMESPAN_SUGGESTER_FILTER,
        SimpleBeanPropertyFilter.filterOutAllExcept(
            "id", "type", "isShownBy", "prefLabel", "altLabel", "hiddenLabel", "begin", "end"));
    filterProvider.addFilter(
        SOLR_PLACE_SUGGESTER_FILTER,
        SimpleBeanPropertyFilter.filterOutAllExcept(
            "id", "type", "isShownBy", "prefLabel", "altLabel", "hiddenLabel"));
    filterProvider.addFilter(
        SOLR_CONCEPT_SUGGESTER_FILTER,
        SimpleBeanPropertyFilter.filterOutAllExcept(
            "id", "type", "isShownBy", "prefLabel", "altLabel", "hiddenLabel"));

    return filterProvider;
  }
}
