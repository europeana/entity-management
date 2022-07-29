package eu.europeana.entitymanagement.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/** JUnit test to check if DataSources are properly deserialized from XML */
@SpringBootTest(classes = SerializationConfig.class)
public class DataSourcesTest {

  @Autowired
  @Qualifier(AppConfigConstants.BEAN_XML_MAPPER)
  private XmlMapper xmlMapper = new com.fasterxml.jackson.dataformat.xml.XmlMapper();

  String WIKIDATA = "http://www.wikidata.org/";
  String GEONAMES = "https://sws.geonames.org/";
  String EUROPEANA_FASHION = "http://thesaurus.europeanafashion.eu/thesaurus/";
  String EUROPEANA = "https://www.europeana.eu/";

  @Test
  public void shouldDeserializeDatasources() throws IOException {
    String DATASOURCES_XML = "/datasource/test-datasources.xml";
    InputStream is = getClass().getResourceAsStream(DATASOURCES_XML);

    DataSources dataSources = xmlMapper.readValue(is, DataSources.class);
    assertNotNull(dataSources);
    assertTrue(dataSources.getDatasources().size() >= 7);
    assertTrue(dataSources.hasDataSource(WIKIDATA));
    assertTrue(dataSources.hasDataSource(GEONAMES));
    assertTrue(dataSources.hasDataSource(EUROPEANA_FASHION));
    assertTrue(dataSources.getEuropeanaDatasource().isPresent());
  }
}
