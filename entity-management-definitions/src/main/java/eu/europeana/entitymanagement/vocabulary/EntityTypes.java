package eu.europeana.entitymanagement.vocabulary;

import eu.europeana.entitymanagement.definitions.exceptions.UnsupportedEntityTypeException;

public enum EntityTypes implements EntityKeyword {
  Organization("Organization", "http://www.europeana.eu/schemas/edm/Organization"),
  Concept("Concept", "https://www.w3.org/2009/08/skos-reference/skos.html#Concept"),
  Agent("Agent", "http://www.europeana.eu/schemas/edm/Agent"),
  Place("Place", "http://www.europeana.eu/schemas/edm/Place"),
  TimeSpan("TimeSpan", "http://www.europeana.eu/schemas/edm/TimeSpan");

  private String entityType;
  private String httpUri;

  public String getEntityType() {
    return entityType;
  }

  EntityTypes(String entityType, String uri) {
    this.entityType = entityType;
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

    for (EntityTypes field : EntityTypes.values()) {
      if (entityId.contains(String.format("/%s/", field.getEntityType().toLowerCase()))) {
        return field;
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
}
