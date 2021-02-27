package eu.europeana.entitymanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.config.DataSources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test to check if DataSources are properly deserialized from XML
 */
@SpringBootTest(classes = SerializationConfig.class)
public class DataSourcesTest {

    @Autowired
    private ObjectMapper objectMapper;


    String WIKIDATA = "http://www.wikidata.org/";
    String GEONAMES = "https://sws.geonames.org/";
    String EUROPEANA_FASHION = "http://thesaurus.europeanafashion.eu/thesaurus/";


    @Test
    public void shouldDeserializeDatasources() throws IOException {
        String DATASOURCES_XML = "/datasource/test-datasources.xml";
        InputStream is = getClass().getResourceAsStream(DATASOURCES_XML);

        DataSources dataSources = objectMapper.readValue(is, DataSources.class);
        assertNotNull(dataSources);
        assertTrue(dataSources.getDatasources().size() >= 7);
        assertTrue(dataSources.hasDataSource(WIKIDATA));
        assertTrue(dataSources.hasDataSource(GEONAMES));
        assertTrue(dataSources.hasDataSource(EUROPEANA_FASHION));

    }

}
