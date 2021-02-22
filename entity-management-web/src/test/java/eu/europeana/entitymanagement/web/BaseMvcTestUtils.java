package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseMvcTestUtils {

    public static final String BASE_SERVICE_URL = "/entity/";
    public static final String BASE_ENTITY_URL = null;

    public static final String CONCEPT_BATHTUB = "/content/concept_bathtube.json";

    /**
     * This method extracts JSON content from a file
     * 
     * @param resource
     * @return JSON string
     * @throws IOException
     */
    protected String getJsonStringInput(String resource) throws IOException {

	try (InputStream resourceAsStream = getClass().getResourceAsStream(resource)) {
	    List<String> lines = IOUtils.readLines(resourceAsStream, StandardCharsets.UTF_8);
	    StringBuilder out = new StringBuilder();
	    for (String line : lines) {
		out.append(line);
	    }
	    return out.toString();
	}
    }

    protected String getEntityIdentifier(String result) throws JSONException {
	String id = getEntityId(result);
	String identifier = id.replace(BASE_ENTITY_URL, "");
	return identifier;
    }

    protected String getEntityId(String result) throws JSONException {
	assertNotNull(result);
	JSONObject json = new JSONObject(result);
	String id = json.getString("id");
	assertNotNull(id);
	return id;
    }
}
