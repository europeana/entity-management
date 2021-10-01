package eu.europeana.entitymanagement.definitions;

// Collection field names
public class EntityRecordFields {

  private EntityRecordFields() {
    // private constructor to prevent instantiation
  }

  public static final String CLASS = "objectClass";
  public static final String ENTITY_ID = "entityId";
  public static final String ENTITY_MODIFIED = "modified";
  public static final String DISABLED = "disabled";

  public static final String ENTITY_SAME_AS = "entity.sameAs";
  public static final String ENTITY_EXACT_MATCH = "entity.exactMatch";
  public static final String ENTITY_TYPE = "entity.type";
}
