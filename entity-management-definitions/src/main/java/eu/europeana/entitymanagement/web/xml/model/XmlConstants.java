package eu.europeana.entitymanagement.web.xml.model;

import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.vocabulary.XmlFields;

/**
 * Constants for XML serialization
 * After adding the namespaces, the most of the XML fieldnames are redundant. 
 * These fields needs to be sorted out and reuse WebEntityFields
 */
public final class XmlConstants extends XmlFields {

  public static final String NAMESPACE_SKOS = "http://www.w3.org/2004/02/skos/core#";
  public static final String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String NAMESPACE_FOAF = "http://xmlns.com/foaf/0.1/";
  public static final String NAMESPACE_EDM = "http://www.europeana.eu/schemas/edm/";
  public static final String NAMESPACE_RDAGR2 = "http://rdvocab.info/ElementsGr2/";
  public static final String NAMESPACE_OWL = "http://www.w3.org/2002/07/owl#";
  public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";
  public static final String NAMESPACE_ORE = "http://www.openarchives.org/ore/terms/";
  public static final String NAMESPACE_DC_TERMS = "http://purl.org/dc/terms/";
  public static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
  public static final String NAMESPACE_WGS84_POS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
  public static final String NAMESPACE_VCARD = "http://www.w3.org/2006/vcard/ns#";

  public static final String RDF = "RDF";
  public static final String XML = "xml";
  public static final String ABOUT = "about";
  public static final String TYPE = WebEntityFields.TYPE;
  public static final String RESOURCE = "resource";
  public static final String LANG = "lang";
  public static final String XML_EDM_WEB_RESOURCE = WebEntityFields.WEB_RESOURCE;
  public static final String IS_SHOWN_BY = WebEntityFields.IS_SHOWN_BY;
  public static final String XML_DC_SOURCE = WebEntityFields.SOURCE;

  public static final String XML_CONCEPT = "Concept";
  public static final String DEPICTION = WebEntityFields.DEPICTION;
  public static final String PREF_LABEL = WebEntityFields.PREF_LABEL;
  public static final String ALT_LABEL = WebEntityFields.ALT_LABEL;
  public static final String HIDDEN_LABEL = WebEntityFields.HIDDEN_LABEL;
  public static final String BROADER = WebEntityFields.BROADER;
  public static final String NARROWER = WebEntityFields.NARROWER;
  public static final String RELATED = WebEntityFields.RELATED;
  public static final String BROAD_MATCH = WebEntityFields.BROAD_MATCH;
  public static final String NARROW_MATCH = WebEntityFields.NARROW_MATCH;
  public static final String RELATED_MATCH = WebEntityFields.RELATED_MATCH;
  public static final String EXACT_MATCH = WebEntityFields.EXACT_MATCH;
  public static final String CLOSE_MATCH = WebEntityFields.CLOSE_MATCH;
  public static final String NOTE = WebEntityFields.NOTE;
  public static final String NOTATION = WebEntityFields.NOTATION;
  public static final String IN_SCHEME = WebEntityFields.IN_SCHEME;

  public static final String XML_TIMESPAN = "TimeSpan";

  public static final String XML_AGENT = "Agent";
  public static final String XML_DATE = WebEntityFields.DATE;
  public static final String XML_IDENTIFIER = WebEntityFields.IDENTIFIER;
  public static final String XML_HAS_PART = WebEntityFields.HAS_PART;
  public static final String XML_IS_PART_OF = WebEntityFields.IS_PART_OF;
  public static final String XML_WAS_PRESENT_AT = WebEntityFields.WAS_PRESENT_AT;
  public static final String XML_BEGIN = WebEntityFields.BEGIN;
  public static final String XML_END = WebEntityFields.END;
  public static final String XML_HASMET = WebEntityFields.HAS_MET;
  public static final String XML_IS_RELATED_TO = WebEntityFields.IS_RELATED_TO;
  public static final String XML_NAME = WebEntityFields.NAME;
  public static final String XML_THUMBNAIL = WebEntityFields.THUMBNAIL;
  public static final String XML_BIOGRAPHICAL_INFORMATION = WebEntityFields.BIOGRAPHICAL_INFORMATION;
  public static final String XML_DATE_OF_BIRTH = WebEntityFields.DATE_OF_BIRTH;
  public static final String XML_DATE_OF_DEATH = WebEntityFields.DATE_OF_DEATH;
  public static final String XML_DATE_OF_ESTABLISHMENT = WebEntityFields.DATE_OF_ESTABLISHMENT;
  public static final String XML_DATE_OF_TERMINATION = WebEntityFields.DATE_OF_TERMINATION;
  public static final String XML_GENDER = WebEntityFields.GENDER;
  public static final String XML_PLACE_OF_BIRTH = WebEntityFields.PLACE_OF_BIRTH;
  public static final String XML_PLACE_OF_DEATH = WebEntityFields.PLACE_OF_DEATH;
  public static final String XML_PROFESSION_OR_OCCUPATION = WebEntityFields.PROFESSION_OR_OCCUPATION;
  public static final String XML_SAME_AS = WebEntityFields.SAME_AS;

  public static final String XML_PLACE = "Place";
  public static final String XML_WGS84_POS_LAT = WebEntityFields.LATITUDE;
  public static final String XML_WGS84_POS_LONG = WebEntityFields.LONGITUDE;
  public static final String XML_WGS84_POS_ALT = WebEntityFields.ALTITUDE;
  public static final String XML_IS_NEXT_IN_SEQUENCE = WebEntityFields.IS_NEXT_IN_SEQUENCE;

  public static final String XML_ORGANIZATION = "Organization";
  public static final String XML_AGGREGATOR = "Aggregator";
  public static final String XML_ACRONYM = WebEntityFields.ACRONYM;
  public static final String XML_DESCRIPTION = WebEntityFields.DESCRIPTION;
  public static final String XML_LOGO = WebEntityFields.FOAF_LOGO;
  public static final String XML_EUROPEANA_ROLE = WebEntityFields.EUROPEANA_ROLE;
  public static final String XML_COUNTRY = WebEntityFields.COUNTRY;
  public static final String XML_HOMEPAGE = WebEntityFields.FOAF_HOMEPAGE;
  public static final String XML_PHONE = WebEntityFields.FOAF_PHONE;
  public static final String XML_MBOX = WebEntityFields.FOAF_MBOX;
  public static final String XML_HAS_ADDRESS = WebEntityFields.HAS_ADDRESS;
  public static final String XML_LANGUAGE = WebEntityFields.LANGUAGE;
  public static final String XML_AGGREGATES_FROM = WebEntityFields.AGGREGATES_FROM;
  public static final String XML_AGGREGATED_VIA = WebEntityFields.AGGREGATED_VIA;
  public static final String XML_GEOGRAPHIC_SCOPE = WebEntityFields.GEOGRAPHIC_SCOPE;
  public static final String XML_HERITAGE_DOMAIN = WebEntityFields.HERITAGE_DOMAIN;
  public static final String XML_PROVIDES_SUPPORT_FOR_MEDIA_TYPE = WebEntityFields.PROVIDES_SUPPORT_FOR_MEDIA_TYPE;
  public static final String XML_PROVIDES_SUPPORT_FOR_DATA_ACTIVITY = WebEntityFields.PROVIDES_SUPPORT_FOR_DATA_ACTIVITY;
  public static final String XML_PROVIDES_CAPACITY_BUILDING_ACTIVITY = WebEntityFields.PROVIDES_SUPPORT_FOR_CAPACITY_BUILDING_ACTIVITY;
  public static final String XML_PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY = WebEntityFields.PROVIDES_AUDIENCE_ENGAGEMENT_ACTIVITY;

  public static final String XML_ADDRESS = WebEntityFields.ADDRESS_TYPE;
  public static final String XML_STREET_ADDRESS = "street-address";
  public static final String XML_POSTAL_CODE = "postal-code";
  public static final String XML_POST_OFFICE_BOX = "post-office-box";
  public static final String XML_LOCALITY = WebEntityFields.LOCALITY;
  public static final String XML_REGION = WebEntityFields.REGION;
  public static final String XML_COUNTRY_NAME = "country-name";
  public static final String XML_HAS_GEO = WebEntityFields.HAS_GEO;
  public static final String XML_LOCATION = WebEntityFields.GEO_LOCATION_TYPE;

  public static final String IS_AGGREGATED_BY = WebEntityFields.IS_AGGREGATED_BY;
  public static final String AGGREGATION = WebEntityFields.AGGREGATION;
  public static final String XML_CREATED = WebEntityFields.CREATED;
  public static final String XML_MODIFIED = WebEntityFields.MODIFIED;
  public static final String XML_AGGREGATES = WebEntityFields.AGGREGATES;
  public static final String XML_DATATYPE = "datatype";
}
