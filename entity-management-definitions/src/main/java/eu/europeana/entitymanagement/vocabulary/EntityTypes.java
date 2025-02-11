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
   * Utility method to verify if the provided type is a type or subtype (e.g. agregator) of organization
   * @param entityType type as string
   * @return true if Organization or subtype
   */
  public static boolean isOrganizationType(String entityType) {
    return EntityTypes.Organization.getEntityType().equalsIgnoreCase(entityType) ||
        EntityTypes.Aggregator.getEntityType().equalsIgnoreCase(entityType);
  }

  /**
   * Utility method to verify if the provided type is exactly the organization type
   * @param entityType type as string
   * @return true if Organization
   */
  public static boolean isOrganization(String entityType) {
    return EntityTypes.Organization.getEntityType().equalsIgnoreCase(entityType);
  }
  
  /**
   * Utility method to verify if the provided type is Aggregator
   * @param entityType type as string
   * @return true if Aggregator
   */
  public static boolean isAggregator(String entityType) {
    return EntityTypes.Aggregator.getEntityType().equalsIgnoreCase(entityType);
  }
  
  /**
   * Method to verify if the entity types are compatible for dereferencing and consolidation. 
   * Aggregator extends Organization, therefore these two type are compatible types
   * @param entityType the type of the first (e.g. consolidated) entity
   * @param proxyResponseType the type of the second (e.g. proxy) entity
   * @return true if the provided types are incompatible
   */
  public static boolean isIncompatibleType(String entityType, String proxyResponseType) {
    if(isOrganizationType(entityType) && isOrganizationType(proxyResponseType)) {
      //organizations and aggregators are compatible types 
      return false;
    }
    
    return !proxyResponseType.equals(entityType);
  }

  public String getParentType() {
    return parentType;
  }
}
