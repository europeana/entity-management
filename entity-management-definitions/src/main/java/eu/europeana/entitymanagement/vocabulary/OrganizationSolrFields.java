package eu.europeana.entitymanagement.vocabulary;

/*
 * TODO: define the constant for the fields countryMap and address
 */
public interface OrganizationSolrFields extends EntitySolrFields {

  public static final String EXTENSION_ALL = DYNAMIC_FIELD_SEPARATOR + "*";
  public static final String DC_DESCRIPTION = "dc_description";
  public static final String DC_DESCRIPTION_ALL = "dc_description" + EXTENSION_ALL;
  public static final String EDM_ACRONYM = "edm_acronym";
  public static final String EDM_ACRONYM_ALL = EDM_ACRONYM + EXTENSION_ALL;
  public static final String FOAF_LOGO = "foaf_logo";
  public static final String FOAF_HOMEPAGE = "foaf_homepage";
  public static final String FOAF_PHONE = "foaf_phone";
  public static final String FOAF_MBOX = "foaf_mbox";
  public static final String EUROPEANA_ROLE = "europeanaRole";
  public static final String EUROPEANA_ROLE_ALL = EUROPEANA_ROLE + EXTENSION_ALL;
  public static final String COUNTRY_ID = "countryId";
  public static final String COUNTRY_PREF_LABEL_ALL = "countryPrefLabel.*";
  public static final String COUNTRY_PREF_LABEL = "countryPrefLabel";
  public static final String VCARD_HAS_ADDRESS = "vcard_hasAddress.1";
  public static final String VCARD_STREET_ADDRESS = "vcard_streetAddress.1";
  public static final String VCARD_LOCALITY = "vcard_locality.1";
  public static final String VCARD_REGION = "vcard_region.1";
  public static final String VCARD_POSTAL_CODE = "vcard_postalCode.1";
  public static final String VCARD_COUNTRYNAME = "vcard_countryName.1";
  public static final String VCARD_POST_OFFICE_BOX = "vcard_postOfficeBox.1";
  public static final String VCARD_HAS_GEO = "hasGeo";
}
