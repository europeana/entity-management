package eu.europeana.entitymanagement.vocabulary;

import java.util.Date;

public interface GeneralConstants {
  /*
   * the value that differentiates the enabled records from the disabled once
   * (any other value different from the Date(0) represents the date when the record was disabled)
   */
  public static final Date ENABLED_RECORD_DATE = new Date(0);
}
