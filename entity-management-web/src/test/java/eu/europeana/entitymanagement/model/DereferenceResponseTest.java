package eu.europeana.entitymanagement.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class DereferenceResponseTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldBeDeserializeCorrectly() throws Exception {
        String testExactMatch = "http://data.europeana.eu/test/xyz";

        String json = "{\"enrichmentBaseWrapperList\": [{\"enrichmentBase\": {\"exactMatch\": \"" + testExactMatch + "\"}}]}";
        DereferenceResponse dereferenceResponse = mapper.readValue(json, DereferenceResponse.class);

        assertNotNull(dereferenceResponse);
        assertEquals(testExactMatch, dereferenceResponse.getExactMatch());
    }
}