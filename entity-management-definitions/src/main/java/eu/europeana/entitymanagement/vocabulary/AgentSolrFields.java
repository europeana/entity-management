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
      BIOGRAPHICAL_INFORMATION + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_BIRTH = "rdagr2_dateOfBirth";
  public static final String DATE_OF_BIRTH_ALL = DATE_OF_BIRTH + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_DEATH = "rdagr2_dateOfDeath";
  public static final String DATE_OF_DEATH_ALL = DATE_OF_DEATH + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String PLACE_OF_BIRTH = "rdagr2_placeOfBirth";
  public static final String PLACE_OF_DEATH = "rdagr2_placeOfDeath";
  public static final String PLACE_OF_BIRTH_ALL = PLACE_OF_BIRTH + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String PLACE_OF_DEATH_ALL = PLACE_OF_DEATH + DYNAMIC_FIELD_SEPARATOR + "*";

  public static final String DATE_OF_ESTABLISHMENT = "rdagr2_dateOfEstablishment";
  public static final String DATE_OF_TERMINATION = "rdagr2_dateOfTermination";
  public static final String DATE_OF_ESTABLISHMENT_ALL =
      "rdagr2_dateOfEstablishment" + DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DATE_OF_TERMINATION_ALL =
      "rdagr2_dateOfTermination" + DYNAMIC_FIELD_SEPARATOR + "*";

  public static final String GENDER = "rdagr2_gender";
  public static final String PROFESSION_OR_OCCUPATION = "rdagr2_professionOrOccupation";
  public static final String PROFESSION_OR_OCCUPATION_ALL =
      PROFESSION_OR_OCCUPATION + DYNAMIC_FIELD_SEPARATOR + "*";
}
