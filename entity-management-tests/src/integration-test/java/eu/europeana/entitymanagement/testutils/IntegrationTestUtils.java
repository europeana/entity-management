package eu.europeana.entitymanagement.testutils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zoho.crm.api.record.Record;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

public class IntegrationTestUtils {

  private static final String WIKIDATA_BASE_URI = "http://www.wikidata.org";
  public static final String BASE_SERVICE_URL = "/entity/";
  public static final String BASE_SCHEME_URL = "/scheme/";
  public static final String BASE_ADMIN_URL = "/management";
  public static final String BASE_FAILED_UPDATES = "management/failed";

  public static final String BATHTUB_DEREF = "/content/deref_bathtub.xml";

  public static final String CONCEPT_JSON = "/content/concept.json";
  public static final String AGENT_JSON = "/content/agent.json";
  public static final String ORGANIZATION_JSON = "/content/organization.json";
  public static final String PLACE_JSON = "/content/place.json";

  public static final String TIMESPAN_JSON = "/content/timespan.json";

  public static final String TIMESPAN_REGISTER_1ST_CENTURY_JSON =
      "/content/timespan_register_1st_century.json";

  public static final String TIMESPAN_UPDATE_1ST_CENTURY_JSON =
      "/content/timespan_update_1st_century.json";

  public static final String CONCEPT_REGISTER_METIS_ERROR_JSON =
      "/content/concept_register_metis_error.json";
  public static final String CONCEPT_REGISTER_ERROR_CHECK_1_JSON =
      "/content/concept_register_error_check_1.json";
  public static final String CONCEPT_REGISTER_INVALID_SOURCE =
      "/content/concept_register_invalid_source.json";
  public static final String CONCEPT_CONSOLIDATED_BATHTUB =
      "/consolidated/concept-consolidated-bathtub.json";
  public static final String CONCEPT_DATA_RECONCELIATION_XML =
      "/metis-deref/concept-data-reconceliation.xml";

  public static final String AGENT_REGISTER_JSON = "/content/agent_register.json";
  public static final String CONCEPT_REGISTER_UNKNOWN_ENTITY =
      "/content/concept_register_unknown.json";
  public static final String CONCEPT_REGISTER_BATHTUB_JSON =
      "/content/concept_register_bathtub.json";
  public static final String CONCEPT_REGISTER_BATHTUB_JSON_INVALID =
      "/content/concept_register_bathtub_invalid.json";
  public static final String AGENT_REGISTER_DAVINCI_JSON = "/content/agent_register_davinci.json";
  public static final String AGENT_REGISTER_RAPHAEL_JSON = "/content/agent_register_raphael.json";
  public static final String AGENT_REGISTER_BIRCH_REDIRECTION_JSON =
      "/content/agent_register_birch_redirection.json";
  public static final String AGENT_REGISTER_STALIN_JSON = "/content/agent_register_stalin.json";
  public static final String AGENT_REGISTER_SCHEGK_JSON = "/content/agent_register_schegk.json";

  public static final String AGENT_REGISTER_JAN_VERMEER =
      "/content/agent_register_jan_vermeer.json";
  public static final String ORGANIZATION_REGISTER_BNF_ZOHO_JSON =
      "/content/organization_register_zoho_bnf.json";
  public static final String ORGANIZATION_REGISTER_NATURALIS_ZOHO_JSON =
      "/content/organization_register_zoho_naturalis.json";
  public static final String ORGANIZATION_REGISTER_GFM_ZOHO_JSON =
      "/content/organization_register_zoho_gfm.json";
  public static final String ORGANIZATION_REGISTER_BERGER_MUSEUM_ZOHO_JSON =
      "/content/organization_register_zoho_berger_museum.json";
  public static final String ORGANIZATION_REGISTER_PCCE_ZOHO_JSON =
      "/content/organization_register_zoho_pcce.json";
  public static final String PLACE_REGISTER_PARIS_JSON = "/content/place_register_paris.json";
  public static final String PLACE_REGISTER_HAGENBACH_JSON =
      "/content/place_register_hagenbach.json";

  public static final String CONCEPT_UPDATE_BATHTUB_JSON = "/content/concept_update_bathtub.json";
  public static final String CONCEPT_UPDATE_FAILED_BATHTUB_JSON =
      "/content/concept_update_failed_bathtub.json";
  public static final String CONCEPT_BATHTUB_EMPTY_UPDATE_JSON =
      "/content/concept_update_bathtub_empty.json";

  public static final String CONCEPT_BATHTUB_INVALID_ISSHOWNBY_UPDATE_JSON =
      "/content/concept_update_bathtub_invalid_isshownby.json";
  public static final String CONCEPT_SCHEME_PHOTO_GENRE_JSON = "/content/concept_scheme_photo_genre.json";

  public static final String CONCEPT_MACHINE_XML = "/metis-deref/concept_machine.xml";
  public static final String CONCEPT_BATHTUB_XML = "/metis-deref/concept-metis-bathtub.xml";
  public static final String EMPTY_METIS_RESPONSE = "/metis-deref/empty_metis_response.xml";
  public static final String CONCEPT_ERROR_CHECK_1_XML = "/metis-deref/concept-error-check-1.xml";

  public static final String AGENT_DA_VINCI_XML = "/metis-deref/agent_da_vinci.xml";
  public static final String AGENT_RAPHAEL_XML = "/metis-deref/agent_raphael.xml";
  public static final String AGENT_BIRCH_XML = "/metis-deref/agent_birch.xml";
  public static final String AGENT_STALIN_XML = "/metis-deref/agent_stalin.xml";
  public static final String AGENT_SCHEGK_XML = "/metis-deref/agent_schegk.xml";
  public static final String AGENT_JAN_VERMEER_XML_VIAF = "/metis-deref/agent_jan_vermeer_viaf.xml";
  public static final String AGENT_JAN_VERMEER_XML_WIKIDATA =
      "/metis-deref/agent_jan_vermeer_wikidata.xml";
  public static final String ORGANIZATION_BNF_XML = "/metis-deref/organization_bnf.xml";
  public static final String PLACE_PARIS_XML = "/metis-deref/place_paris.xml";
  public static final String PLACE_HAGENBACH_XML = "/metis-deref/place_hagenbach_redirect.xml";
  public static final String TIMESPAN_1ST_CENTURY_XML = "/metis-deref/timespan_1st_century.xml";

  public static final String TIMESPAN_1_CENTURY_SEARCH_AND_RECORD_JSON =
      "/search_and_record-deref/timespan_1_century_search_and_record.json";
  public static final String SEARCH_AND_RECORD_EMPTY_JSON =
      "/search_and_record-deref/search_and_record_empty.json";

  public static final String AGENT1_REFERENTIAL_INTEGRITY_JSON =
      "/content/agent1-referential-integrity.json";
  public static final String AGENT2_REFERENTIAL_INTEGRITY_JSON =
      "/content/agent2-referential-integrity.json";
  public static final String AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON =
      "/content/agent-davinci-referential-integrity.json";
  public static final String AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON =
      "/ref-integrity/agent-davinci-integrity-performed.json";
  public static final String AGENT_FLORENCE_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/agent_florence_school_9623.xml";
  public static final String AGENT_SALAI_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/agent_salai_58185.xml";
  public static final String CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/concept_engineering_178.xml";
  public static final String PLACE_AMBOISE_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/place_amboise_42996.xml";
  public static final String PLACE_FLORENCE_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/place_florence_143905.xml";
  public static final String PLACE_FRANCE_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/place_france_85.xml";

  public static final String PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/place_sforza_castle_143289.xml";
  public static final String TIMESPAN_15_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/timespan_15.xml";
  public static final String TIMESPAN_16_REFERENTIAL_INTEGRTITY =
      "/ref-integrity/references/timespan_16.xml";

  public static final String PLACE_REFERENTIAL_INTEGRITY_JSON =
      "/content/place-referential-integrity.json";

  public static final String AGENT_DA_VINCI_URI = "http://www.wikidata.org/entity/Q762";
  public static final String AGENT_RAPHAEL_URI = "http://www.wikidata.org/entity/Q5597";
  public static final String AGENT_BIRCH_URI = "http://www.wikidata.org/entity/Q5485322";
  public static final String AGENT_BIRCH_UPDATED_URI = "http://www.wikidata.org/entity/Q14819199";

  public static final String AGENT_STALIN_URI = "http://www.wikidata.org/entity/Q855";
  public static final String AGENT_SCHEGK_URI = "http://www.wikidata.org/entity/Q110854";

  public static final String AGENT_JAN_VERMEER_VIAF_URI = "http://viaf.org/viaf/51961439";
  public static final String AGENT_JAN_VERMEER_WIKIDATA_URI =
      "http://www.wikidata.org/entity/Q41264";
  public static final String PLACE_PARIS_URI = "https://sws.geonames.org/2988507/";
  public static final String PLACE_HAGENBACH_URI = "http://www.wikidata.org/entity/Q32050320";
  public static final String PLACE_HAGENBACH_UPDATED_URI = "http://www.wikidata.org/entity/Q541669";

  public static final String TIMESPAN_1ST_CENTURY_URI = "http://www.wikidata.org/entity/Q8106";

  public static final String CONCEPT_BATHTUB_URI = "http://www.wikidata.org/entity/Q152095";

  public static final String VALID_MIGRATION_ID = "http://www.wikidata.org/entity/testing";
  public static final String INVALID_MIGRATION_ID = "http://www.testing.org/entity/testing";

  public static final String ORGANIZATION_BNF_URI_ZOHO =
      "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000002112001";
  public static final String ORGANIZATION_NATURALIS_URI_ZOHO =
      "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000000370517";
  public static final String ORGANIZATION_PCCE_URI_ZOHO =
      "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000000338555";
  public static final String ORGANIZATION_GFM_URI_ZOHO =
      "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000004503618";
  public static final String ORGANIZATION_BERGER_MUSEUM_URI_ZOHO =
      "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000004477407";
  public static final String ORGANIZATION_NATURALIS_URI_WIKIDATA_PATH_SUFFIX = "/entity/Q641676";
  public static final String ORGANIZATION_NATURALIS_URI_WIKIDATA_URI =
      WIKIDATA_BASE_URI + ORGANIZATION_NATURALIS_URI_WIKIDATA_PATH_SUFFIX;

  public static final String ORGANIZATION_GFM_OLD_WIKIDATA_URI_PATH_SUFFIX = "/entity/Q28933300";
  public static final String ORGANIZATION_GFM_WIKIDATA_URI_PATH_SUFFIX = "/entity/Q18290171";
  public static final String ORGANIZATION_GFM_URI_WIKIDATA_URI =
      WIKIDATA_BASE_URI + ORGANIZATION_GFM_WIKIDATA_URI_PATH_SUFFIX;
  public static final String ORGANIZATION_GFM_OLD_URI_WIKIDATA_URI =
      WIKIDATA_BASE_URI + ORGANIZATION_GFM_OLD_WIKIDATA_URI_PATH_SUFFIX;

  public static final String ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI_PATH_SUFFIX =
      "/entity/Q23014463";
  public static final String ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI =
      WIKIDATA_BASE_URI + ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI_PATH_SUFFIX;

  public static final String ORGANIZATION_BNF_WIKIDATA_URI_PATH_SUFFIX = "/entity/Q193563";
  public static final String ORGANIZATION_BNF_URI_WIKIDATA_URI =
      WIKIDATA_BASE_URI + ORGANIZATION_BNF_WIKIDATA_URI_PATH_SUFFIX;

  public static final String ORGANIZATION_NATURALIS_ZOHO_RESPONSE =
      "/zoho-deref/organization_zoho_naturalis_response.json";
  public static final String ORGANIZATION_GFM_ZOHO_RESPONSE =
      "/zoho-deref/organization_zoho_gfm_response.json";
  public static final String ORGANIZATION_BERGER_MUSEUM_ZOHO_RESPONSE =
      "/zoho-deref/organization_zoho_berger_museum_response.json";
  public static final String ORGANIZATION_PCCE_ZOHO_RESPONSE =
      "/zoho-deref/organization_zoho_pcce_response.json";
  public static final String ORGANIZATION_BNF_ZOHO_RESPONSE =
      "/zoho-deref/organization_zoho_bnf_response.json";
  public static final String ORGANIZATION_NATURALIS_WIKIDATA_RESPONSE_XML =
      "/wikidata-deref/organization_wikidata_naturalis_response.xml";
  public static final String ORGANIZATION_GFM_WIKIDATA_RESPONSE_XML =
      "/wikidata-deref/organization_wikidata_gfm_response.xml";

  public static final String ORGANIZATION_BERGER_MUSEUM_WIKIDATA_RESPONSE_XML =
      "/wikidata-deref/organization_wikidata_berger_museum_response.xml";
  public static final String ORGANIZATION_BNF_WIKIDATA_RESPONSE_XML =
      "/wikidata-deref/organization_wikidata_bnf_response.xml";

  /** static data source * */
  public static final String STATIC_ENTITY_EXTERNAL_ID =
      "http://bib.arts.kuleuven.be/photoVocabulary/-photoVocabulary-11008";

  public static final String STATIC_ENTITY_IDENTIFIER = "1689";
  public static final String STATIC_ENTITY_FILE = "/content/static_concept_1689.json";
  public static final String STATIC_ENTITY_NOT_MINTED_FILE =
      "/content/static_concept_not_minted.json";

  /** Creates an ObjectMapper specifically for handling Mock zoho responses */
  private static final ObjectMapper zohoResponseObjectMapper =
      new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .registerModule(
              new SimpleModule(
                  "SimpleModule",
                  Version.unknownVersion(),
                  Map.of(Record.class, new ZohoRecordTestDeserializer())));

  /** Maps ZOHO organization URIs to mocked JSON responses */
  public static Map<String, String> ZOHO_RESPONSE_MAP =
      Map.of(
          ORGANIZATION_NATURALIS_URI_ZOHO,
          ORGANIZATION_NATURALIS_ZOHO_RESPONSE,
          ORGANIZATION_BNF_URI_ZOHO,
          ORGANIZATION_BNF_ZOHO_RESPONSE,
          ORGANIZATION_GFM_URI_ZOHO,
          ORGANIZATION_GFM_ZOHO_RESPONSE,
          ORGANIZATION_PCCE_URI_ZOHO,
          ORGANIZATION_PCCE_ZOHO_RESPONSE,
          ORGANIZATION_BERGER_MUSEUM_URI_ZOHO,
          ORGANIZATION_BERGER_MUSEUM_ZOHO_RESPONSE);

  /** Maps Metis dereferenciation URIs to mocked XML responses */
  public static final Map<String, String> METIS_RESPONSE_MAP =
      Map.of(
          AGENT_DA_VINCI_URI, AGENT_DA_VINCI_XML,
          AGENT_STALIN_URI, AGENT_STALIN_XML,
          PLACE_PARIS_URI, PLACE_PARIS_XML,
          PLACE_HAGENBACH_URI, PLACE_HAGENBACH_XML,
          TIMESPAN_1ST_CENTURY_URI, TIMESPAN_1ST_CENTURY_XML,
          CONCEPT_BATHTUB_URI, CONCEPT_BATHTUB_XML,
          AGENT_JAN_VERMEER_VIAF_URI, AGENT_JAN_VERMEER_XML_VIAF,
          AGENT_JAN_VERMEER_WIKIDATA_URI, AGENT_JAN_VERMEER_XML_WIKIDATA,
          AGENT_SCHEGK_URI, AGENT_SCHEGK_XML,
          AGENT_BIRCH_URI, AGENT_BIRCH_XML);

  public static String loadFile(String resourcePath) throws IOException {
    return IOUtils.toString(
            Objects.requireNonNull(IntegrationTestUtils.class.getResourceAsStream(resourcePath)),
            StandardCharsets.UTF_8)
        .replace("\n", "");
  }

  public static Optional<Record> getZohoOrganizationRecord(String zohoId) throws Exception {
    String zohoResponseData = loadFile(ZOHO_RESPONSE_MAP.get(zohoId));
    return Optional.ofNullable(zohoResponseObjectMapper.readValue(zohoResponseData, Record.class));
  }
}
