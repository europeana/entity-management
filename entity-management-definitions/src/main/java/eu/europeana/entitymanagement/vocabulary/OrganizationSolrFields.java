package eu.europeana.entitymanagement.vocabulary;

/**
 * Constants for solr field names
 */
public interface OrganizationSolrFields extends EntitySolrFields {

  String EXTENSION_ALL = DYNAMIC_FIELD_SEPARATOR + "*";
  String DC_DESCRIPTION = "dc_description";
  String DC_DESCRIPTION_ALL = "dc_description" + EXTENSION_ALL;
  String EDM_ACRONYM = "edm_acronym";
  String EDM_ACRONYM_ALL = EDM_ACRONYM + EXTENSION_ALL;
  String FOAF_LOGO = "foaf_logo";
  String FOAF_HOMEPAGE = "foaf_homepage";
  String FOAF_PHONE = "foaf_phone";
  String FOAF_MBOX = "foaf_mbox";
  String EUROPEANA_ROLE = "europeanaRole";
  String COUNTRY = "country";
  String COUNTRY_LABEL = "countryLabel";
  String COUNTRY_LABEL_ALL = "countryLabel" + EXTENSION_ALL;
  String VCARD_HAS_ADDRESS = "vcard_hasAddress.1";
  String VCARD_STREET_ADDRESS = "vcard_streetAddress.1";
  String VCARD_LOCALITY = "vcard_locality.1";
  String VCARD_REGION = "vcard_region.1";
  String VCARD_POSTAL_CODE = "vcard_postalCode.1";
  String VCARD_COUNTRYNAME = "vcard_countryName.1";
  String VCARD_POST_OFFICE_BOX = "vcard_postOfficeBox.1";
  String VCARD_HAS_GEO = "hasGeo";
}
