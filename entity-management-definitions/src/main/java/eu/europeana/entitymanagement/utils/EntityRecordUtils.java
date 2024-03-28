package eu.europeana.entitymanagement.utils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;

public class EntityRecordUtils {

  public static final String ENTITY_ID_REMOVED_MSG = "Entity '%s' has been removed";
  public static final String MULTIPLE_CHOICES_FOR_REDIRECTION_MSG =
      "There are multiple choices for redirecting the entity id: '%s'. They include: '%s'.";

  private EntityRecordUtils() {
    // private constructor to prevent instantiation
  }

  /**
   * Utility method to build Entity Id URLs
   * 
   * @param type of the entity
   * @param identifier of the entity
   * @return EntityId url
   */
  public static String buildEntityIdUri(EntityTypes type, String identifier) {
    return buildEntityIdUri(type.getUrlPath(), identifier);
  }

  private static String buildEntityIdUri(@NotNull String type, @NotNull String identifier) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(WebEntityFields.BASE_DATA_EUROPEANA_URI)
        .append(type.toLowerCase(Locale.ENGLISH)).append('/').append(identifier);
    return stringBuilder.toString();
  }

  /**
   * Extract entity part from the EntityId Url
   * 
   * @param entityId as URL
   * @return the entity path including {type}/{identifier}
   */
  public static String extractEntityPathFromEntityId(String entityId) {
    return entityId.replace(WebEntityFields.BASE_DATA_EUROPEANA_URI, "");
  }

  public static String getEuropeanaAggregationId(String entityId) {
    return entityId + "#aggr_europeana";
  }

  public static String getDatasourceAggregationId(String entityId, int aggregationId) {
    return entityId + "#aggr_source_" + aggregationId;
  }

  public static String getIsAggregatedById(String entityId) {
    return entityId + "#aggregation";
  }

  public static String getEuropeanaProxyId(String entityId) {
    return entityId + "#proxy_europeana";
  }

  /**
   * extract the identifier part from the URL
   * 
   * @param url
   * @return
   */
  public static String getIdentifierFromUrl(String url) {
    if (!url.contains("/")) {
      return url;
    } else {
      return StringUtils.substringAfterLast(url, "/");
    }
  }

  /**
   * Gets the "{type}/{identifier}" from an EntityId string
   * 
   * @param entityId as url
   * @return the request path "{type}/{identifier}"
   */
  public static String getEntityRequestPath(String entityId) {
    // entity id is "http://data.europeana.eu/{type}/{identifier}"
    String[] parts = entityId.split("/");

    // namespace is always base
    return parts[parts.length - 2] + "/" + parts[parts.length - 1];
  }

  /**
   * Gets the "{type}/base/{identifier}" from an EntityId string
   * 
   * @param entityId as url
   * @return the request path "{type}/base/{identifier}"
   */
  public static String getEntityRequestPathWithBase(String entityId) {
    // entity id is "http://data.europeana.eu/{type}/{identifier}"
    String[] parts = entityId.split("/");

    return parts[parts.length - 2] + "/base/" + parts[parts.length - 1];
  }

  /**
   * Utility method to extract the entity ids from the list of entity records
   * 
   * @param entities the list of entity records
   * @return the list of extracted entity ids
   */
  public static List<String> getEntityIds(List<EntityRecord> entities) {
    if (entities == null || entities.isEmpty()) {
      return Collections.emptyList();
    }
    return entities.stream().map(e -> e.getEntityId()).toList();
  }


  /**
   * Checks if the entity with the given id is a Europeana entity (data.europeana.eu)
   *
   * @param id entity id
   * @return true if given entity is a Europeana entity, false otherwise
   */
  public static boolean isEuropeanaEntity(String id) {

    return id != null && id.startsWith(WebEntityFields.BASE_DATA_EUROPEANA_URI);
  }

  /**
   * Build redirection relative Location by replacing the identifier of requested entity with the
   * identifier extracted from redirectionEntityId, the query string is appended if non null
   * 
   * @param identifier the identifier of the URL from the original request
   * @param redirectionEntityId the full entityId of the entity to redirect to
   * @param requestUri the URI of the original request
   * @param queryString the queryString from the original request
   * @return the constructed Location for redirection
   */
  public static String buildRedirectionLocation(String identifier, String redirectionEntityId,
      String requestUri, String queryString) {
    // get entity identifier
    String redirectionIdentifier = getIdentifierFromUrl(redirectionEntityId);
    // replace identifier in original request URI
    String redirectLocation = requestUri.replaceFirst(identifier, redirectionIdentifier);
    // append queryString if exists
    if (queryString != null) {
      redirectLocation += ('?' + queryString);
    }
    return redirectLocation;
  }
}
