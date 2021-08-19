package eu.europeana.entitymanagement.testutils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

public class BaseMvcTestUtils {

    public static final String BASE_SERVICE_URL = "/entity/";
    public static final String BASE_ADMIN_URL = "/management";

    public static final String BATHTUB_DEREF = "/content/deref_bathtub.xml";
        
    public static final String CONCEPT_JSON = "/content/concept.json";
    public static final String AGENT_JSON = "/content/agent.json";
    public static final String ORGANIZATION_JSON = "/content/organization.json";
    public static final String PLACE_JSON = "/content/place.json";
    public static final String TIMESPAN_JSON = "/content/timespan.json"; 
    
    public static final String CONCEPT_REGISTER_METIS_ERROR_JSON = "/content/concept_register_metis_error.json";
    public static final String CONCEPT_REGISTER_ERROR_CHECK_1_JSON = "/content/concept_register_error_check_1.json";
    public static final String CONCEPT_REGISTER_INVALID_SOURCE = "/content/concept_register_invalid_source.json";
    public static final String CONCEPT_CONSOLIDATED_BATHTUB = "/consolidated/concept-consolidated-bathtub.json";
    public static final String CONCEPT_DATA_RECONCELIATION_XML = "/metis-deref/concept-data-reconceliation.xml";


    public static final String AGENT_REGISTER_JSON = "/content/agent_register.json";
    public static final String CONCEPT_REGISTER_UNKNOWN_ENTITY = "/content/concept_register_unknown.json";
    public static final String CONCEPT_REGISTER_BATHTUB_JSON = "/content/concept_register_bathtub.json";
    public static final String AGENT_REGISTER_DAVINCI_JSON = "/content/agent_register_davinci.json";
    public static final String AGENT_REGISTER_STALIN_JSON = "/content/agent_register_stalin.json";
    public static final String AGENT_REGISTER_JAN_VERMEER = "/content/agent_register_jan_vermeer.json";
    public static final String ORGANIZATION_REGISTER_BNF_JSON = "/content/organization_register_bnf.json";
    public static final String ORGANIZATION_REGISTER_BNF_ZOHO_JSON = "/content/organization_register_zoho_bnf.json";
    public static final String PLACE_REGISTER_PARIS_JSON = "/content/place_register_paris.json";
    public static final String TIMESPAN_REGISTER_1ST_CENTURY_JSON = "/content/timespan_register_1st_century.json";
    
    public static final String CONCEPT_UPDATE_BATHTUB_JSON = "/content/concept_update_bathtub.json";
    public static final String TIMESPAN_UPDATE_JSON = "/content/timespan_update.json";

    public static final String CONCEPT_VALIDATE_FIELDS_JSON = "/content/concept-validate-fields.json";
    public static final String ORGANIZATION_VALIDATE_FIELDS_JSON = "/content/organization-validation.json";
    public static final String AGENT_VALIDATE_FIELDS_JSON = "/content/agent-validation.json";
    public static final String CONCEPT_BATHTUB_EMPTY_UPDATE_JSON = "/content/concept_update_bathtub_empty.json";


    public static final String CONCEPT_MACHINE_XML = "/metis-deref/concept_machine.xml";
    public static final String CONCEPT_BATHTUB_XML = "/metis-deref/concept-metis-bathtub.xml";
    public static final String EMPTY_METIS_RESPONSE = "/metis-deref/empty_metis_response.xml";
    public static final String CONCEPT_ERROR_CHECK_1_XML = "/metis-deref/concept-error-check-1.xml";

    public static final String AGENT_DA_VINCI_XML = "/metis-deref/agent_da_vinci.xml";
    public static final String AGENT_STALIN_XML = "/metis-deref/agent_stalin.xml";
    public static final String AGENT_JAN_VERMEER_XML_VIAF = "/metis-deref/agent_jan_vermeer_viaf.xml";
    public static final String AGENT_JAN_VERMEER_XML_WIKIDATA = "/metis-deref/agent_jan_vermeer_wikidata.xml";
    public static final String ORGANIZATION_BNF_XML = "/metis-deref/organization_bnf.xml";
    public static final String PLACE_PARIS_XML = "/metis-deref/place_paris.xml";
    public static final String TIMESPAN_1ST_CENTURY_XML = "/metis-deref/timespan_1st_century.xml";
    
    public static final String AGENT1_REFERENTIAL_INTEGRITY_JSON = "/content/agent1-referential-integrity.json";
    public static final String AGENT2_REFERENTIAL_INTEGRITY_JSON = "/content/agent2-referential-integrity.json";
    public static final String AGENT_DA_VINCI_REFERENTIAL_INTEGRITY_JSON = "/content/agent-davinci-referential-integrity.json";
    public static final String AGENT_DA_VINCI_REFERENTIAL_INTEGRTITY_PERFORMED_JSON = "/ref-integrity/agent-davinci-integrity-performed.json";  
    public static final String AGENT_FLORENCE_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/agent_florence_school_9623.xml";  
    public static final String AGENT_SALAI_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/agent_salai_58185.xml";  
    public static final String CONCEPT_ENGINEERING_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/concept_engineering_178.xml";  
    public static final String PLACE_AMBOISE_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/place_amboise_42996.xml";  
    public static final String PLACE_FLORENCE_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/place_florence_143905.xml";  
    public static final String PLACE_FRANCE_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/place_france_85.xml";  
    public static final String PLACE_SFORZA_CASTLE_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/place_sforza_castle_143289.xml";  
    public static final String TIMESPAN_15_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/timespan_15.xml";  
    public static final String TIMESPAN_16_REFERENTIAL_INTEGRTITY = "/ref-integrity/references/timespan_16.xml";  
    
    public static final String PLACE_REFERENTIAL_INTEGRITY_JSON = "/content/place-referential-integrity.json";


    public static final String AGENT_DA_VINCI_URI = "http://www.wikidata.org/entity/Q762";
    public static final String AGENT_STALIN_URI = "http://www.wikidata.org/entity/Q855";
    public static final String AGENT_JAN_VERMEER_VIAF_URI = "http://viaf.org/viaf/51961439";
    public static final String AGENT_JAN_VERMEER_WIKIDATA_URI = "http://www.wikidata.org/entity/Q41264";
    public static final String ORGANIZATION_BNF_URI = "http://www.wikidata.org/entity/Q193563";
    public static final String PLACE_PARIS_URI = "https://sws.geonames.org/2988507/";
    public static final String TIMESPAN_1ST_CENTURY_URI = "http://www.wikidata.org/entity/Q8106";

    public static final String CONCEPT_BATHTUB_URI = "http://www.wikidata.org/entity/Q152095";

    public static final String VALID_MIGRATION_ID = "http://www.wikidata.org/entity/testing";
    public static final String INVALID_MIGRATION_ID = "http://www.testing.org/entity/testing";

    /**
     * Maps Metis dereferenciation URIs to mocked XML responses
     */
    public static final Map<String, String> METIS_RESPONSE_MAP = Map.of(
            AGENT_DA_VINCI_URI, AGENT_DA_VINCI_XML,
            AGENT_STALIN_URI, AGENT_STALIN_XML,
            ORGANIZATION_BNF_URI, ORGANIZATION_BNF_XML,
            PLACE_PARIS_URI, PLACE_PARIS_XML,
            TIMESPAN_1ST_CENTURY_URI, TIMESPAN_1ST_CENTURY_XML,
            CONCEPT_BATHTUB_URI, CONCEPT_BATHTUB_XML,
            AGENT_JAN_VERMEER_VIAF_URI, AGENT_JAN_VERMEER_XML_VIAF,
            AGENT_JAN_VERMEER_WIKIDATA_URI, AGENT_JAN_VERMEER_XML_WIKIDATA
    );

    
    public static String loadFile(String resourcePath) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(BaseMvcTestUtils.class.getResourceAsStream(resourcePath)), StandardCharsets.UTF_8).replace("\n", "");
    }



}
