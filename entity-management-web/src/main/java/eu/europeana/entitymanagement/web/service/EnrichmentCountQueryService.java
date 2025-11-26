package eu.europeana.entitymanagement.web.service;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPathWithBase;
import static eu.europeana.entitymanagement.vocabulary.WebEntityFields.BASE_DATA_EUROPEANA_URI;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons.http.HttpConnection;
import eu.europeana.api.commons.http.HttpResponseHandler;
import eu.europeana.entitymanagement.exception.ParamValidationException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.exception.ScoringComputationException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import javax.annotation.Resource;

public class EnrichmentCountQueryService {

  /** Query fields for entity types */
  private static final Map<String, String> ENRICHMENT_QUERY_FIELD_MAP =
      Map.of(
          EntityTypes.Agent.getEntityType(), "edm_agent",
          EntityTypes.Concept.getEntityType(), "skos_concept",
          EntityTypes.Place.getEntityType(), "edm_place",
          EntityTypes.TimeSpan.getEntityType(), "edm_timespan",
          EntityTypes.Organization.getEntityType(), "foaf_organization",
          EntityTypes.Aggregator.getEntityType(), "foaf_organization"
          );

  private static final Logger logger = LogManager.getLogger(EnrichmentCountQueryService.class);
  private static final String ERROR_MSG = "Error retrieving enrichmentCount for entityId=";
  private static final String contentTierPrefix = " AND contentTier:";

  @Resource
  private EntityManagementConfiguration configuration;

  private final HttpConnection httpConnection;
  AuthenticationHandler auth;

  /**
   * Constructor
   * @param auth authentication for accessing SR api
   */
  public EnrichmentCountQueryService(AuthenticationHandler auth) {
    this.auth = auth;
    httpConnection = new HttpConnection(true);
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
    String response = null;
    Instant start = Instant.now();

    try {
      String uri = buildSearchRequestUrl(entity);
      if (logger.isDebugEnabled()) {
        logger.debug("Getting enrichment count for entityId={}; queryUri={}", entity.getEntityId(), uri);
      }

      HttpResponseHandler httpResponse = httpConnection.get(uri, "application/json", auth);
      if (httpResponse.getStatus() == HttpStatus.SC_OK) {
        response = httpResponse.getResponse();
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


  /**
   * Build the search api retrieval url with entity id
   * @param entity entity
   * @return URL
   * @throws ParamValidationException
   */
  private String buildSearchRequestUrl(Entity entity) throws ParamValidationException {
    try {
      return new URIBuilder(configuration.getSearchApiUrlPrefix())
              .addParameter("query", buildSearchQuery(entity))
              .build().toString();
    } catch (URISyntaxException e) {
      throw new ParamValidationException("Error building the search request url - " + e.getMessage(), e);
    }
  }

  private String buildSearchQuery(Entity entity) {
    StringBuilder searchQuery = new StringBuilder(50); // resized as atleast 35 characters are appended
    searchQuery.append(String.format(
            "%s:%s ", ENRICHMENT_QUERY_FIELD_MAP.get(entity.getType()), getEntityIdsForQuery(entity)));

    if (!EntityTypes.isOrganizationType(entity.getType())) {
      searchQuery.append(contentTierPrefix);
      searchQuery.append(configuration.getEnrichmentsQueryContentTier());
    }
    // no rows needed, only the count
    searchQuery.append("&profile=minimal&rows=0");
    return searchQuery.toString();
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
    if(EntityTypes.isOrganizationType(entity.getType())) {
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
