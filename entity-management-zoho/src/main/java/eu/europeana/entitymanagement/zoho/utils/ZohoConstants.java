package eu.europeana.entitymanagement.zoho.utils;

public final class ZohoConstants {

  // Modules
  public static final String CONTACTS_MODULE_NAME = "Contacts";
  public static final String ACCOUNTS_MODULE_NAME = "Accounts";

  // Fields
  // Accounts is the equivalent to Organizations
  public static final String ID_FIELD = "id";
  public static final String ACCOUNT_NAME_FIELD =
      "Account_Name"; // This is the organization field name in Zoho
  public static final String NAME_FIELD =
      "name"; // This is the name of the organization under the Account_Name
  public static final String FIRST_NAME_FIELD = "First_Name";
  public static final String LAST_NAME_FIELD = "Last_Name";
  public static final String EMAIL_FIELD = "Email";
  public static final String USER_COUNTRY_FIELD = "Country";
  public static final String PARTICIPATION_LEVEL_FIELD = "Participation_level";
  public static final String METIS_USER_FIELD = "Metis_user";
  public static final String ACCOUNT_ROLE_FIELD =
      "Pick_List_3"; // This is the Account/Organization Role field
  public static final String ORGANIZATION_ROLE_FIELD = "Organisation_Role";
  public static final String OFFICIAL_LANGUAGE_FIELD = "Official_Language";
  public static final String LANG_ORGANIZATION_NAME_FIELD = "Lang_Organisation_Name";
  public static final String LANG_ALTERNATIVE_FIELD = "Lang_Alternative";
  public static final String ALTERNATIVE_FIELD = "Alternative";
  public static final String LANG_ACRONYM_FIELD = "Lang_Acronym";
  public static final String ACRONYM_FIELD = "Acronym";
  public static final String LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD = "Logo_link_to_WikimediaCommons";
  public static final String WEBSITE_FIELD = "Website";
  public static final String SECTOR_FIELD = "Sector";
  public static final String SCOPE_FIELD = "Scope";
  public static final String ORGANIZATION_COUNTRY_FIELD = "Country1";
  public static final String SAME_AS_FIELD = "SameAs";
  public static final String STREET_FIELD = "Street";
  public static final String CITY_FIELD = "City";
  public static final String COUNTRY_FIELD = "Organisation_Country";
  public static final String ZIP_CODE_FIELD = "ZIP_code";
  public static final String PO_BOX_FIELD = "PO_box";
  public static final String LATITUDE_FIELD = "Latitude";
  public static final String LONGITUDE_FIELD = "Longitude";
  public static final String LAST_ACTIVITY_TIME_FIELD = "Last_Activity_Time";
  public static final String ADDRESS_ABOUT = "#address";
  public static final String HIDDEN_LABEL1_FIELD = "Hidden_1";
  public static final String HIDDEN_LABEL2_FIELD = "Hidden_2";
  public static final String HIDDEN_LABEL3_FIELD = "Hidden_3";
  public static final String HIDDEN_LABEL4_FIELD = "Hidden_4";
  public static final String HIDDEN_LABEL_FIELD = "Hidden";

  public static final String INDUSTRY_FIELD = "Industry_2";
  public static final String EUROPEANA_ID_FIELD = "Europeana_ID";

  // Operations
  public static final String EQUALS_OPERATION = "equals";
  public static final String STARTS_WITH_OPERATION = "starts_with";

  // General constants
  public static final String ZOHO_OPERATION_FORMAT_STRING = "(%s:%s:%s)";
  public static final String DELIMITER_COMMA = ",";
  public static final String OR = "OR";
  public static final String UNDEFINED_LANGUAGE_KEY = "def";

  public static final int LANGUAGE_CODE_LENGTH = 5;
  public static final int SAME_AS_CODE_LENGTH = 3;

  // constants for filtering by Owner
  public static final String ZOHO_OWNER_FIELD = "Owner";
  public static final String ZOHO_OWNER_CRITERIA = "Owner.name";

  // constants for modified
  public static final String ZOHO_MODIFIED_BY_FIELD = "Modified_By";
  public static final String ZOHO_USER_EUROPEANA_APIS = "Europeana APIs";
  
  
  // scheduled for deleteion
  public static final String ZOHO_SCHEDULED_DELETION = "Scheduled_for_deletion";

  private ZohoConstants() {}
}
