package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPathWithBase;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.exception.ScoringComputationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.model.EnrichmentCountResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EnrichmentCountQueryService {

  /** Query fields for entity types */
  private static final Map<String, String> ENRICHMENT_QUERY_FIELD_MAP =
      Map.of(
          EntityTypes.Agent.getEntityType(), "edm_agent",
          EntityTypes.Concept.getEntityType(), "skos_concept",
          EntityTypes.Place.getEntityType(), "edm_place",
          EntityTypes.TimeSpan.getEntityType(), "edm_timespan",
          EntityTypes.Organization.getEntityType(), "foaf_organization");

  private static final Logger logger = LogManager.getLogger(EnrichmentCountQueryService.class);
  private static final String ERROR_MSG = "Error retrieving enrichmentCount for entityId=";
  private static final String contentTierPrefix = "AND contentTier:";

  private final WebClient webClient;
  private final String searchApiUriPrefix;
  private final String contentTier;

  public EnrichmentCountQueryService(EntityManagementConfiguration configuration) {
    searchApiUriPrefix = configuration.getSearchApiUrlPrefix();
    contentTier = contentTierPrefix + configuration.getEnrichmentsQueryContentTier();

    webClient = WebClient.builder().build();
  }

  /**
   * Queries the Search API to retrieve enrichment counts for the given entityId. Organizations have
   * a different query string.
   *
   * @param entityId entityID string
   * @param type entity type
   */
  public int getEnrichmentCount(String entityId, String type) {
    String searchQuery =
        String.format(
            "%s:%s ",
            ENRICHMENT_QUERY_FIELD_MAP.get(type), getEntityIdForQuery(entityId, type));

    if (!EntityTypes.Organization.getEntityType().equals(type)) {
      searchQuery = searchQuery + contentTier;
    }

    String uri = searchApiUriPrefix + searchQuery;

    if (logger.isDebugEnabled()) {
      logger.debug("Getting enrichment count for entityId={}; queryUri={}", entityId, uri);
    }

    EnrichmentCountResponse response;
    Instant start = Instant.now();

    try {
      response =
          webClient
              .get()
              .uri(uri)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(EnrichmentCountResponse.class)
              .block();
    } catch (Exception e) {
      throw new ScoringComputationException(ERROR_MSG + entityId, e);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved enrichmentCount for entityId={} in {}ms; response={}",
          entityId,
          Duration.between(start, Instant.now()).toMillis(),
          response);
    }

    if (response == null) {
      throw new ScoringComputationException(ERROR_MSG + entityId);
    }

    return response.getTotalResults();
  }

  /**
   * EntityID format is different in Search API. So we need to add the /base/ namespace when
   * querying for enrichment counts.
   *
   * <p>TODO: This should be changed when entities are re-indexed in Search API with the "correct"
   * ids (EA-2944 suport both URIs with and without /base/ in the path)
   */
  private String getEntityIdForQuery(String entityId, String type) {
    // not applicable for organizations and timespans
    if (EntityTypes.TimeSpan.getEntityType().equals(type)
        || EntityTypes.Organization.getEntityType().equals(type)) {
      return "\""+entityId+"\"";
    }
    
    //EA-2944 suport both URIs with and without /base/ in the path
    StringBuilder entityIdsBuilder = new StringBuilder("(\"");
    entityIdsBuilder.append(BASE_DATA_EUROPEANA_URI).append(getEntityRequestPathWithBase(entityId)).append("\" OR \"");
    entityIdsBuilder.append(BASE_DATA_EUROPEANA_URI).append(getEntityRequestPath(entityId)).append("\")");
    
    return entityIdsBuilder.toString();
  }
}
