package eu.europeana.entitymanagement.web.service;


import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.exception.ScoringComputationException;
import eu.europeana.entitymanagement.web.model.EnrichmentCountResponse;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EnrichmentCountQueryService {

  private final WebClient webClient;
  private final String searchApiUriPrefix;
  private final String enrichmentQuery;
  private final String enrichmentQueryOrgs;

  private static final Logger logger = LogManager.getLogger(EnrichmentCountQueryService.class);
  private static final String ERROR_MSG = "Error retrieving enrichmentCount for entityId=";


  public EnrichmentCountQueryService(EntityManagementConfiguration configuration) {
    searchApiUriPrefix = configuration.getSearchApiUrlPrefix();
    enrichmentQuery = configuration.getEnrichmentsQuery();
    enrichmentQueryOrgs = configuration.getEnrichmentsQueryOrgs();

    webClient = WebClient.builder().build();
  }


  /**
   * Queries the Search API to retrieve enrichment counts for the given entityId. Organizations have
   * a different query string.
   *
   * @param entityId       entityID string
   * @param isOrganization indicates whether this is an organization.
   */
  public int getEnrichmentCount(String entityId, boolean isOrganization) {
    String searchQuery = isOrganization ? String.format(enrichmentQueryOrgs, entityId)
        : String.format(enrichmentQuery, entityId);

    String uri = searchApiUriPrefix + searchQuery;

    if (logger.isDebugEnabled()) {
      logger.debug("Getting enrichment count for entityId={}; queryUri={}", entityId,
          uri);
    }

    EnrichmentCountResponse response;
    Instant start = Instant.now();

    try {
      response = webClient.get()
          .uri(uri)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(EnrichmentCountResponse.class)
          .block();
    } catch (Exception e) {
      throw new ScoringComputationException(
          ERROR_MSG + entityId, e);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Retrieved enrichmentCount for entityId={} in {}ms; response={}", entityId,
          Duration.between(start, Instant.now()).toMillis(), response);
    }

    if (response == null) {
      // unlikely as exception should be thrown earlier if response cannot be deserialized.
      throw new ScoringComputationException(
          ERROR_MSG + entityId);
    }

    return response.getTotalResults();
  }
}
