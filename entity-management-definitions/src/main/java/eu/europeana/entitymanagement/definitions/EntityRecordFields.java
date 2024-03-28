package eu.europeana.entitymanagement.definitions;

/**
 *  Collection of field names for entity record class
 */
public class EntityRecordFields {

  public static final String CLASS = "objectClass";
  public static final String ENTITY = "entity";
  public static final String ENTITY_ID = "entityId";
  public static final String ENTITY_MODIFIED = "modified";
  public static final String DISABLED = "disabled";

  public static final String ENTITY_SAME_AS = "entity.sameAs";
  public static final String ENTITY_AGGREGATED_VIA = "entity.aggregatedVia";
  public static final String ENTITY_EXACT_MATCH = "entity.exactMatch";
  public static final String ENTITY_TYPE = "entity.type";
  
  private EntityRecordFields() {
    // private constructor to prevent instantiation
  }

}
