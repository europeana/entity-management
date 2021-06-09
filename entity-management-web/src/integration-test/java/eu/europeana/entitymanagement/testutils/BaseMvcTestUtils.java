package eu.europeana.entitymanagement.testutils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    
    public static final String CONCEPT_REGISTER_JSON = "/content/concept_register.json";
    public static final String AGENT_REGISTER_JSON = "/content/agent_register.json";
    public static final String AGENT_REGISTER_STALIN_JSON = "/content/agent_register_stalin.json";
    public static final String ORGANIZATION_REGISTER_JSON = "/content/organization_register.json";
    public static final String PLACE_REGISTER_JSON = "/content/place_register.json";
    public static final String TIMESPAN_REGISTER_JSON = "/content/timespan_register.json";  
    
    public static final String CONCEPT_UPDATE_JSON = "/content/concept_update.json";
    public static final String ORGANIZATION_VALIDATE_FIELDS_JSON = "/content/organization-validation.json";
    public static final String AGENT_VALIDATE_FIELDS_JSON = "/content/agent-validation.json";
    public static final String CONCEPT_EMPTY_UPDATE__JSON = "/content/concept_update_empty.json";
    
    public static final String CONCEPT_XML = "/metis-deref/concept.xml";
    public static final String AGENT_XML = "/metis-deref/agent.xml";
    public static final String AGENT_STALIN_XML = "/metis-deref/agent_stalin.xml";
    public static final String ORGANIZATION_XML = "/metis-deref/organization.xml";
    public static final String PLACE_XML = "/metis-deref/place.xml";
    public static final String TIMESPAN_XML = "/metis-deref/timespan.xml";  
    
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
    
    public static String loadFile(String resourcePath) throws IOException {
        return IOUtils.toString(BaseMvcTestUtils.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8).replace("\n", "");
    }


    /**
     * Gets the "{type}/{identifier}" from an EntityId string
     */
    public static String getEntityRequestPath(String entityId) {
        //entity id is "http://data.europeana.eu/{type}/{identifier}"
        String[] parts = entityId.split("/");

        // namespace is always base
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
    }

}
