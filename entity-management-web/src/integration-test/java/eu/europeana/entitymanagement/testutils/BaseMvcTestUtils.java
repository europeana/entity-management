package eu.europeana.entitymanagement.testutils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseMvcTestUtils {

    public static final String BASE_SERVICE_URL = "/entity/";

    public static final String CONCEPT_BATHTUB = "/content/concept_bathtube.json";
    public static final String BATHTUB_DEREF = "/content/deref_bathtub.xml";

    public static String loadFile(String resourcePath) throws IOException {
        return IOUtils.toString(BaseMvcTestUtils.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8).replace("\n", "");
    }


    /**
     * Gets the "{type}/{namespace}/{identifier}" from an EntityId string
     */
    public static String getEntityRequestPath(String entityId) {
        //entity id is "http://data.europeana.eu/{type}/{identifier}"
        String[] parts = entityId.split("/");

        // namespace is always base
        return parts[parts.length - 2] + "/base/" + parts[parts.length - 1];
    }

}
