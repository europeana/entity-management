package eu.europeana.entitymanagement.vocabulary;

import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;

public enum EntityTypes implements EntityKeyword {
  Organization("Organization", "organization", "http://www.europeana.eu/schemas/edm/Organization", null),
  //must keep in sync the parent type of Aggregators to be Organization
  Aggregator("Aggregator", "organization", "http://www.europeana.eu/schemas/edm/Aggregator", "Organization"),
  Concept("Concept", "concept", "https://www.w3.org/2009/08/skos-reference/skos.html#Concept", null),
  ConceptScheme(
      "ConceptScheme",
      "scheme",
      "https://www.w3.org/2009/08/skos-reference/skos.html#ConceptScheme", null),
  Agent("Agent", "agent", "http://www.europeana.eu/schemas/edm/Agent", null),
  Place("Place", "place", "http://www.europeana.eu/schemas/edm/Place", null),
  TimeSpan("TimeSpan", "timespan", "http://www.europeana.eu/schemas/edm/TimeSpan", null);

  private String entityType;
  private String urlPath;
  private String httpUri;
  private String parentType;

  EntityTypes(String entityType, String urlPath, String uri, String parentType) {
    this.entityType = entityType;
    this.urlPath = urlPath;
    this.httpUri = uri;
    this.parentType = parentType;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getUrlPath() {
    return urlPath;
  }
  /**
   * Check if an array of EntityTypes contains an Entity type
   *
   * @param entityTypes Array of EntityTypes
   * @param entityType Single EntityTypes for which it is verified if it is contained in the
   *     EntityTypes array
   * @return True if the EntityTypes object is contained in the EntityTypes array
   */
  public static boolean arrayHasValue(EntityTypes[] entityTypes, EntityTypes entityType) {
    for (EntityTypes entType : entityTypes) {
      if (entType.equals(entityType)) {
        return true;
      }
    }
    return false;
  }

  public static EntityTypes getByEntityType(String entityType)
      throws UnsupportedEntityTypeException {

    for (EntityTypes entityTypeEnum : EntityTypes.values()) {
      if (entityTypeEnum.getEntityType().equalsIgnoreCase(entityType)) return entityTypeEnum;
    }
    throw new UnsupportedEntityTypeException(entityType);
  }

  public String getHttpUri() {
    return httpUri;
  }

  @Override
  public String getJsonValue() {
    return getEntityType();
  }
  
  /**
   * Utility method to verify if the provided type is TimeSpan
   * @param entityType type as string
   * @return true if TimeSpan
   */
  public static boolean isTimeSpan(String entityType) {
    return EntityTypes.TimeSpan.getEntityType().equals(entityType);
  }
  
  /**
   * Utility method to verify if the provided type is Organization
   * @param entityType type as string
   * @return true if Organization
   */
  public static boolean isOrganization(String entityType) {
    return EntityTypes.Organization.getEntityType().equalsIgnoreCase(entityType) ||
        EntityTypes.Aggregator.getEntityType().equalsIgnoreCase(entityType);
  }

  public String getParentType() {
    return parentType;
  }
}
