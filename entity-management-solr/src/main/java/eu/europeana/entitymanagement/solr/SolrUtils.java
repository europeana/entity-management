package eu.europeana.entitymanagement.solr;

import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimeSpan;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;

/**
 * This class implements supporting methods for Solr*Impl classes e.g. normalization of the content
 * to match to the required output format.
 */
public class SolrUtils {

  public static final String SOLR_AGENT_SUGGESTER_FILTER = "solrAgentFilter";
  public static final String SOLR_ORGANIZATION_SUGGESTER_FILTER = "solrOrganizationFilter";
  public static final String SOLR_TIMESPAN_SUGGESTER_FILTER = "solrTimeSpanFilter";

  /**
   * This method adds prefixes to the fields in format Map<String, List<String>> languageMap e.g.
   * "skos_prefLabel"
   *
   * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
   * @param languageMap e.g. prefLabel
   * @return normalized content in format Map<String, List<String>>
   */
  public static Map<String, List<String>> normalizeStringListMapByAddingPrefix(
      String fieldNamePrefix, Map<String, List<String>> languageMap) {
    if (MapUtils.isEmpty(languageMap)) {
      return new HashMap<>();
    }
    Map<String, List<String>> res;
    if (!languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
      res =
          languageMap.entrySet().stream()
              .collect(
                  Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
    } else {
      res = languageMap;
    }
    return res;
  }

  /**
   * This method adds prefixes to the fields in format Map<String, String> languageMap e.g.
   * "skos_prefLabel"
   *
   * @param fieldNamePrefix e.g. ConceptSolrFields.PREF_LABEL
   * @param languageMap e.g. prefLabel
   * @return normalized content in format Map<String, String>
   */
  public static Map<String, String> normalizeStringMapByAddingPrefix(
      String fieldNamePrefix, Map<String, String> languageMap) {

    if (MapUtils.isEmpty(languageMap)) {
      return new HashMap<>();
    }

    Map<String, String> res;
    if (!languageMap.keySet().iterator().next().contains(fieldNamePrefix)) {
      res =
          languageMap.entrySet().stream()
              .collect(
                  Collectors.toMap(entry -> fieldNamePrefix + entry.getKey(), Map.Entry::getValue));
    } else {
      res = languageMap;
    }
    return res;
  }

  /**
   * Gets the {@link SolrEntity} class for an entity type.
   *
   * @param solrType entity type in Solr
   * @return SolrEntity class type
   */
  @SuppressWarnings("unchecked")
  public static <T extends Entity, U extends SolrEntity<T>> Class<U> getSolrEntityClass(
      String solrType) {
    if (solrType.equals(EntityTypes.Agent.getEntityType())) {
      return (Class<U>) SolrAgent.class;
    } else if (solrType.equals(EntityTypes.Concept.getEntityType())) {
      return (Class<U>) SolrConcept.class;
    } else if (solrType.equals(EntityTypes.Organization.getEntityType())) {
      return (Class<U>) SolrOrganization.class;
    } else if (solrType.equals(EntityTypes.Place.getEntityType())) {
      return (Class<U>) SolrPlace.class;
    } else if (solrType.equals(EntityTypes.TimeSpan.getEntityType())) {
      return (Class<U>) SolrTimeSpan.class;
    }

    throw new IllegalArgumentException(
        String.format(
            "Unrecognized entity type while determining Solr entity class: %s ", solrType));
  }

  public static SolrEntity<? extends Entity> createSolrEntity(EntityRecord record) {
    final Entity entity = record.getEntity();
    SolrEntity<? extends Entity> solrEntity = null;
    if (entity instanceof Agent) {
      solrEntity = new SolrAgent((Agent) entity);
    } else if (entity instanceof Concept) {
      solrEntity = new SolrConcept((Concept) entity);
    } else if (entity instanceof Organization) {
      solrEntity = new SolrOrganization((Organization) entity);
    } else if (entity instanceof Place) {
      solrEntity = new SolrPlace((Place) entity);
    } else if (entity instanceof TimeSpan) {
      solrEntity = new SolrTimeSpan((TimeSpan) entity);
    }

    // All possible types have been checked
    if (solrEntity == null) {
      throw new IllegalArgumentException(
          String.format(
              "Unrecognized entity type while creating SolrEntity: %s ",
              entity.getClass().getName()));
    }

    setMetricsAndFilters(solrEntity, record);

    return solrEntity;
  }

  private static void setMetricsAndFilters(
      SolrEntity<? extends Entity> solrEntity, EntityRecord record) {
    // metrics only set in entity isAggregatedBy
    Aggregation aggregation = record.getEntity().getIsAggregatedBy();

    if (aggregation != null) {
      solrEntity.setDocCount(
          aggregation.getRecordCount());
      // TODO: change data types when solr schema will be updated
      if (aggregation.getPageRank() != null) {
          solrEntity.setPageRank(aggregation.getPageRank().floatValue());
      }
      if (aggregation.getScore() != null) {
          solrEntity.setDerivedScore(aggregation.getScore().floatValue());
      }
    }

    EntityProxy europeanaProxy = record.getEuropeanaProxy();
    if (europeanaProxy != null) {
      // rights only set in Europeana proxy
      solrEntity.setRights(List.of(europeanaProxy.getProxyIn().getRights()));
    }

    if (solrEntity.getDocCount() != null && solrEntity.getDocCount() > 0) {
      // set type & in_europeana filter
      solrEntity.setSuggestFilters(
          Arrays.asList(solrEntity.getType(), EntitySolrFields.SUGGEST_FILTER_EUROPEANA));
    } else {
      // set type only
      solrEntity.setSuggestFilters(List.of(solrEntity.getType()));
    }
  }
}
