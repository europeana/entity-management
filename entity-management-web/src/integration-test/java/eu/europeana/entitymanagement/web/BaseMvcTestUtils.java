package eu.europeana.entitymanagement.web;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseMvcTestUtils {

    public static final String BASE_SERVICE_URL = "/entity/";
    public static final String BASE_ENTITY_URL = null;
    public static final String CONCEPT_BATHTUB = "/content/concept-bathtub.json";
    public static final String CONCEPT_CONSOLIDATED_BATHTUB = "/consolidated/concept-consolidated-bathtub.json";
    public static final String CONCEPT_JSON = "/content/concept.json";
    public static final String CONCEPT_DATA_RECONCELIATION_XML = "/metis-deref/concept-data-reconceliation.xml";
    public static final String CONCEPT_METIS_BATHTUB = "/metis-deref/concept-metis-bathtub.xml";
    public static final String CONCEPT_XML = "/metis-deref/concept.xml";

    public static String getEntityIdentifier(String result) throws JSONException {
        String id = getEntityId(result);
        String identifier = id.replace(BASE_ENTITY_URL, "");
        return identifier;
    }

    public static String getEntityId(String result) throws JSONException {
        assertNotNull(result);
        JSONObject json = new JSONObject(result);
        String id = json.getString("id");
        assertNotNull(id);
        return id;
    }

    public static String loadJson(String resourcePath) throws IOException {
        return IOUtils.toString(BaseMvcTestUtils.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8).replace("\n", "");
    }
}
