package eu.europeana.entitymanagement.vocabulary;

import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;

public enum EntityTypes implements EntityKeyword {
  Organization("Organization", "organization", "http://www.europeana.eu/schemas/edm/Organization"),
  Concept("Concept", "concept", "https://www.w3.org/2009/08/skos-reference/skos.html#Concept"),
  ConceptScheme(
      "ConceptScheme",
      "scheme",
      "https://www.w3.org/2009/08/skos-reference/skos.html#ConceptScheme"),
  Agent("Agent", "agent", "http://www.europeana.eu/schemas/edm/Agent"),
  Place("Place", "place", "http://www.europeana.eu/schemas/edm/Place"),
  TimeSpan("TimeSpan", "timespan", "http://www.europeana.eu/schemas/edm/TimeSpan");

  private String entityType;
  private String urlPath;
  private String httpUri;

  public String getEntityType() {
    return entityType;
  }

  public String getUrlPath() {
    return urlPath;
  }

  EntityTypes(String entityType, String stringForUrl, String uri) {
    this.entityType = entityType;
    this.urlPath = stringForUrl;
    this.httpUri = uri;
  }

  @Deprecated
  /**
   * refactor to use value of
   *
   * @param entityType
   * @return
   */
  public static boolean contains(String entityType) {

    for (EntityTypes field : EntityTypes.values()) {
      if (field.getEntityType().equalsIgnoreCase(entityType)) {
        return true;
      }
    }

    return false;
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

  public static EntityTypes getByEntityId(String entityId) throws UnsupportedEntityTypeException {

    for (EntityTypes entityType : EntityTypes.values()) {
      if (entityId.contains(String.format("/%s/", entityType.getUrlPath()))) {
        return entityType;
      }
    }

    throw new UnsupportedEntityTypeException(entityId);
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
    return EntityTypes.Organization.getEntityType().equals(entityType);
  }
}
