package eu.europeana.entitymanagement.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Aggregation;
import eu.europeana.entitymanagement.definitions.model.Aggregator;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.definitions.model.Place;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.solr.model.SolrAgent;
import eu.europeana.entitymanagement.solr.model.SolrAggregator;
import eu.europeana.entitymanagement.solr.model.SolrConcept;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.model.SolrOrganization;
import eu.europeana.entitymanagement.solr.model.SolrPlace;
import eu.europeana.entitymanagement.solr.model.SolrTimeSpan;
import eu.europeana.entitymanagement.vocabulary.EntitySolrFields;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

/**
 * This class implements supporting methods for Solr*Impl classes e.g. normalization of the content
 * to match to the required output format.
 */
public class SolrEntityUtils {

  public static final String SOLR_AGENT_SUGGESTER_FILTER = "solrAgentFilter";
  public static final String SOLR_ORGANIZATION_SUGGESTER_FILTER = "solrOrganizationFilter";
  public static final String SOLR_TIMESPAN_SUGGESTER_FILTER = "solrTimeSpanFilter";
  public static final String SOLR_PLACE_SUGGESTER_FILTER = "solrPlaceFilter";
  public static final String SOLR_CONCEPT_SUGGESTER_FILTER = "solrConceptFilter";
  public static final int MAX_FILTERS = 3;
  
  /**
   * Hide default constructor
   */
  private SolrEntityUtils(){}
  
  /**
   * Gets the {@link SolrEntity} class for an entity type.
   *
   * @param solrType entity type in Solr
   * @return SolrEntity class type
   */
  @SuppressWarnings("rawtypes")
  public static Class<? extends SolrEntity> getSolrEntityClass(
      String solrType) {
    Class<? extends SolrEntity> solrEntityClass = null;
    switch(EntityTypes.valueOf(solrType)) {
      case Agent:
        solrEntityClass = SolrAgent.class;
        break;
      case Concept:
        solrEntityClass = SolrConcept.class;
        break;
      case Organization:
        solrEntityClass = SolrOrganization.class;
        break;
      case Aggregator:
        solrEntityClass = SolrAggregator.class;
        break;
      case Place:
        solrEntityClass = SolrPlace.class;
        break; 
      case TimeSpan:
        solrEntityClass = SolrTimeSpan.class;
        break;
      case ConceptScheme:
        break;
      default:
        break;  
    }

    if(solrEntityClass == null) {
      throw new IllegalArgumentException(
          String.format(
              "Unrecognized entity type while determining Solr entity class: %s ", solrType));
    }
    
    return solrEntityClass;
  }

  /**
   * Factory method to instantiate solr objects
   * @param record the entity record
   * @return the solr entity
   */
  public static SolrEntity<? extends Entity> createSolrEntity(EntityRecord record) {
    final Entity entity = record.getEntity();
    SolrEntity<? extends Entity> solrEntity = null;
    switch(EntityTypes.valueOf(entity.getType())) {
      case Agent:
        solrEntity = new SolrAgent((Agent) entity);
        break;
      case Concept:
        solrEntity = new SolrConcept((Concept) entity);
        break;
      case Organization:
        solrEntity = new SolrOrganization((Organization) entity);
        break;
      case Aggregator:
        solrEntity = new SolrAggregator((Aggregator) entity);
        break;
      case Place:
        solrEntity = new SolrPlace((Place) entity);
        break; 
      case TimeSpan:
        solrEntity = new SolrTimeSpan((TimeSpan) entity);
        break;
      case ConceptScheme:
        break;
      default:
        break;  
    }
    // All possible types have been checked
    if (solrEntity == null) {
      throw new IllegalArgumentException(
          String.format(
              "Unrecognized entity type while creating SolrEntity: %s ",
              entity.getClass().getName()));
    }

    setMetricsAndFilters(solrEntity, record);
    setLabelEnrich(solrEntity, record);

    return solrEntity;
  }

  private static void setLabelEnrich(SolrEntity<? extends Entity> solrEntity, EntityRecord record) {
    if (isEnrichmentDisabled(record)) {
      return;
    }

    solrEntity.setLabelEnrichGeneral(collectLabelEnrichGeneral(record));
    solrEntity.setLabelEnrich(collectLabelEnrich(record));
  }

  private static List<String> collectLabelEnrichGeneral(EntityRecord record) {
    // collect values from prefLabel, altLabel, hiddenLabel, acronym
    List<String> values = new ArrayList<>();
    Entity entity = record.getEntity();
    if (entity == null) {
      return values;
    }

    // prefLabel
    values.addAll(entity.getPrefLabel().values());

    // altLabel
    Map<String, List<String>> altLabels = entity.getAltLabel();
    if (altLabels != null) {
      for (Map.Entry<String, List<String>> entry : altLabels.entrySet()) {
        values.addAll(entry.getValue());
      }
    }

    // hiddenLabel, only available for organizations
    if (entity.getHiddenLabel() != null) {
      values.addAll(entity.getHiddenLabel());
    }

    // acronym
    if (entity instanceof Organization) {
      Map<String, List<String>> acronyms = ((Organization) entity).getAcronym();
      if (acronyms != null) {
        for (Map.Entry<String, List<String>> entry : acronyms.entrySet()) {
          values.addAll(entry.getValue());
        }
      }
    }

    return values;
  }

  private static Map<String, List<String>> collectLabelEnrich(EntityRecord record) {
    // collect values from prefLabel, altLabel, hiddenLabel acronym
    Entity entity = record.getEntity();
    Map<String, List<String>> values = new ConcurrentHashMap<>();
    if (entity == null) {
      return values;
    }

    // prefLabel
    for (Map.Entry<String, String> entry : entity.getPrefLabel().entrySet()) {
      addLabel(values, entry.getKey(), entry.getValue());
    }

    // altLabel
    Map<String, List<String>> altLabels = entity.getAltLabel();
    if (altLabels != null) {
      for (Map.Entry<String, List<String>> entry : altLabels.entrySet()) {
        addLabels(values, entry.getKey(), entry.getValue());
      }
    }

    // acronym
    if (entity instanceof Organization) {
      Map<String, List<String>> acronyms = ((Organization) entity).getAcronym();
      if (acronyms != null) {
        for (Map.Entry<String, List<String>> entry : acronyms.entrySet()) {
          addLabels(values, entry.getKey(), entry.getValue());
        }
      }
    }

    // hiddenLabel, only available for organizations
    if (entity.getHiddenLabel() != null) {
      // add hidden labels to all languages
      for (Map.Entry<String, List<String>> entry : values.entrySet()) {
        entry.getValue().addAll(entity.getHiddenLabel());
      }
    }

    return values;
  }

  private static void addLabel(Map<String, List<String>> labelMap, String language, String label) {
    if (labelMap.containsKey(language)) {
      labelMap.get(language).add(label);
    } else {
      List<String> values = new ArrayList<>();
      values.add(label);
      labelMap.put(language, values);
    }
  }

  private static void addLabels(
      Map<String, List<String>> labelMap, String language, List<String> labels) {
    if (labelMap.containsKey(language)) {
      labelMap.get(language).addAll(labels);
    } else {
      List<String> values = new ArrayList<>(labels);
      labelMap.put(language, values);
    }
  }

  private static boolean isEnrichmentDisabled(EntityRecord record) {
    // entity is not consolidated, should not be index at this stage
    boolean notConsolidated = record.getEntity() == null || record.getEntity().getIsAggregatedBy() == null;
    // check if flag is set to false
    boolean noEnrichment = Boolean.FALSE.equals(record.getEntity().getIsAggregatedBy().getEnrich());
    if (notConsolidated || noEnrichment) {
      return true;
    }

    return false;
  }

  private static void setMetricsAndFilters(
      SolrEntity<? extends Entity> solrEntity, EntityRecord record) {
    // metrics only set in entity isAggregatedBy
    Aggregation aggregation = record.getEntity().getIsAggregatedBy();

    if (aggregation != null) {
      solrEntity.setDocCount(aggregation.getRecordCount());
      if (aggregation.getPageRank() != null) {
        solrEntity.setPageRank(aggregation.getPageRank().floatValue());
      }
      if (aggregation.getScore() != null) {
        solrEntity.setDerivedScore(aggregation.getScore().floatValue());
      }
    }

    EntityTypes entityType = EntityTypes.valueOf(solrEntity.getType());
    List<String> filters = new ArrayList<>(MAX_FILTERS); 
    filters.add(entityType.getEntityType());
    if(entityType.getParentType() != null) {
      //add organization as type for Aggregators
      filters.add(entityType.getParentType());
    }
    
    if (solrEntity.getDocCount() != null && solrEntity.getDocCount() > 0) {
      //in_europeana filter
      filters.add(EntitySolrFields.SUGGEST_FILTER_EUROPEANA);
    } 
    
    solrEntity.setSuggestFilters(filters);
  }
}
