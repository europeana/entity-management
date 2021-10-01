package eu.europeana.entitymanagement.vocabulary;

/*
 * TODO: add a constant for the field wasPresentAt
 */
public interface AgentSolrFields extends EntitySolrFields {

  public static final String DATE = "dc_date";
  public static final String HAS_MET = "edm_hasMet";
  public static final String WAS_PRESENT_AT = "edm_wasPresentAt";
  public static final String NAME = "foaf_name";
  public static final String BIOGRAPHICAL_INFORMATION = "rdagr2_biographicalInformation";
  public static final String BIOGRAPHICAL_INFORMATION_ALL =
      "rdagr2_biographicalInformation" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_BIRTH_ALL =
      "rdagr2_dateOfBirth" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_DEATH_ALL =
      "rdagr2_dateOfDeath" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String PLACE_OF_BIRTH = "rdagr2_placeOfBirth";
  public static final String PLACE_OF_DEATH = "rdagr2_placeOfDeath";
  public static final String PLACE_OF_BIRTH_ALL =
      "rdagr2_placeOfBirth" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String PLACE_OF_DEATH_ALL =
      "rdagr2_placeOfDeath" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_ESTABLISHMENT = "rdagr2_dateOfEstablishment";
  public static final String DATE_OF_TERMINATION = "rdagr2_dateOfTermination";
  public static final String GENDER = "rdagr2_gender";
  public static final String PROFESSION_OR_OCCUPATION = "rdagr2_professionOrOccupation";
  public static final String PROFESSION_OR_OCCUPATION_ALL =
      "rdagr2_professionOrOccupation" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String BEGIN = "edm_begin";
  public static final String END = "edm_end";
  public static final String EXACT_MATCH = "skos_exactMatch";
}
