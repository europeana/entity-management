package eu.europeana.entitymanagement.vocabulary;

public interface PlaceSolrFields extends EntitySolrFields {

  public static final String IS_NEXT_IN_SEQUENCE = "edm_isNextInSequence";
  public static final String LATITUDE = "wgs84_pos_lat";
  public static final String LONGITUDE = "wgs84_pos_long";
  public static final String ALTITUDE = "wgs84_pos_alt";
  public static final String EXACT_MATCH = "skos_exactMatch";
}
