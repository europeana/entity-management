package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPathWithBase;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.ScoringComputationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

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
  private static final String contentTierPrefix = " AND contentTier:";

  private final WebClient webClient;
  private final EntityManagementConfiguration configuration;

  public EnrichmentCountQueryService(EntityManagementConfiguration configuration) {
    this.configuration = configuration;
    webClient = WebClient.builder().build();
  }

  /**
   * Queries the Search API to retrieve enrichment counts for the given Entity. Organizations have
   * a different query string.
   *
   * @param entity Entity
   * @return the number of enrichments of Europeana Records using the given entity
   * @throws ScoringComputationException if the European search API cannot be called successfully 
   */
  public int getEnrichmentCount(Entity entity) throws ScoringComputationException {
    String uri = buildSearchRequestUrl(entity);

    if (logger.isDebugEnabled()) {
      logger.debug("Getting enrichment count for entityId={}; queryUri={}", entity.getEntityId(), uri);
    }

    String response = null;
    Instant start = Instant.now();

    try {
      response =
          webClient
              .get()
              .uri(uri)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(String.class)
              .block();
    } catch (Exception e) {
      throw new ScoringComputationException(ERROR_MSG + entity.getEntityId(), e);
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved enrichmentCount for entityId={} in {}ms; response={}",
          entity.getEntityId(),
          Duration.between(start, Instant.now()).toMillis(),
          response);
    }

    if (response == null) {
      throw new ScoringComputationException(ERROR_MSG + entity.getEntityId());
    }

    int result = 0;
    JSONObject responseJson = new JSONObject(response);
    if (responseJson.has("totalResults")) {
      result = responseJson.getInt("totalResults");
    }
    return result;
  }

  String buildSearchRequestUrl(Entity entity) {
    StringBuilder url = new StringBuilder(configuration.getSearchApiUrlPrefix());
    String searchQuery =
        String.format(
            "%s:%s ", ENRICHMENT_QUERY_FIELD_MAP.get(entity.getType()), getEntityIdsForQuery(entity));

    url.append("&query=" + searchQuery);
    if (!EntityTypes.isOrganization(entity.getType())) {
      url.append(contentTierPrefix);
      url.append(configuration.getEnrichmentsQueryContentTier());
    }
    url.append("&profile=minimal");
    // no rows needed, only the count
    url.append("&rows=0");
    return url.toString();
  }

  /**
   * EntityID format is different in Search API. So we need to add the /base/ namespace when
   * querying for enrichment counts.
   *
   * <p>TODO: This should be changed when entities are re-indexed in Search API with the "correct"
   * ids (EA-2944 suport both URIs with and without /base/ in the path)
   */
  private String getEntityIdsForQuery(Entity entity) {
    // not applicable for timespans
    if (EntityTypes.isTimeSpan(entity.getType())) {
      return "\"" + entity.getEntityId() + "\"";
    }
    
    //for the organizations search also for all data.europeana.eu uris from the sameAs
    if(EntityTypes.isOrganization(entity.getType())) {
      return buildSearchedIdsForOrganizations(entity);
    }

    // EA-2944 suport both URIs with and without /base/ in the path
    StringBuilder entityIdsBuilder = new StringBuilder("(\"");
    entityIdsBuilder
        .append(BASE_DATA_EUROPEANA_URI)
        .append(getEntityRequestPathWithBase(entity.getEntityId()))
        .append("\" OR \"")
        .append(BASE_DATA_EUROPEANA_URI)
        .append(getEntityRequestPath(entity.getEntityId()))
        .append("\")");

    return entityIdsBuilder.toString();
  }

  String buildSearchedIdsForOrganizations(Entity entity) {
    StringBuilder orgIdsBuilder = new StringBuilder("(\"");
    orgIdsBuilder.append(entity.getEntityId());
    
    if(entity.getSameReferenceLinks()!=null) {
      for(String sameAsUri : entity.getSameReferenceLinks()) {
        if(sameAsUri.startsWith(BASE_DATA_EUROPEANA_URI)) {
          orgIdsBuilder.append("\" OR \"");
          orgIdsBuilder.append(sameAsUri);
        }
      }
    }
    orgIdsBuilder.append("\")");
    return orgIdsBuilder.toString();
  }



}
