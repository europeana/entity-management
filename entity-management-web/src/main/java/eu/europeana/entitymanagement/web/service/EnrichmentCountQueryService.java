package eu.europeana.entitymanagement.web.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.entitymanagement.exception.ParamValidationException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.ScoringComputationException;

public class EnrichmentCountQueryService extends SearchRecordAccess {

  private static final String ERROR_MSG = "Error retrieving enrichmentCount for entityId=";

  /**
   * Constructor
   *
   * @param auth authentication for accessing SR api
   */
  public EnrichmentCountQueryService(AuthenticationHandler auth) {
    super(auth);
  }

  /**
   * Queries the Search API to retrieve enrichment counts for the given Entity. Organizations have
   * a different query string.
   *
   * @param entity Entity
   * @return the number of enrichments of Europeana Records using the given entity
   * @throws ScoringComputationException if the European search API cannot be called successfully 
   */
  public int getEnrichmentCount(Entity entity) throws ScoringComputationException, ParamValidationException {
    String response = null;
    Instant start = Instant.now();
    String uri = buildEnrichmentCountRequestUrl(entity);
    if (logger.isDebugEnabled()) {
      logger.debug("Getting enrichment count for entityId={}; queryUri={}", entity.getEntityId(), uri);
    }
    try (CloseableHttpResponse httpResponse = httpConnection.get(uri, Collections.singletonMap(HttpHeaders.ACCEPT,"application/json") ,auth)){
      if (httpResponse.getCode() == HttpStatus.SC_OK) {
        response = EntityUtils.toString(httpResponse.getEntity());
      } else {
        logger.error("Unable to get the valid response from the Search and Record API");
      }
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
}
