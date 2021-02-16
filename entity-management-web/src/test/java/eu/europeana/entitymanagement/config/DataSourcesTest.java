package eu.europeana.entitymanagement.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.common.config.DataSources;

/**
 * JUnit test to check if DataSources are properly deserialized from XML
 */
@ContextConfiguration(classes = { EntityManagementApp.class, AppConfig.class })
@ExtendWith(SpringExtension.class)
public class DataSourcesTest {

    String WIKIDATA = "http://www.wikidata.org/";
    String GEONAMES = "https://sws.geonames.org/";
    String EUROPEANA_FASHION = "http://thesaurus.europeanafashion.eu/thesaurus/";

    @Resource(name = AppConfig.BEAN_EM_DATA_SOURCES)
    DataSources dataSources;

    @Test
    public void getDataSources() {
	assertNotNull(dataSources);
	assertTrue(dataSources.getDatasources().size() >= 0);
	assertTrue(dataSources.hasDataSource(WIKIDATA));
	assertTrue(dataSources.hasDataSource(GEONAMES));
	assertTrue(dataSources.hasDataSource(EUROPEANA_FASHION));

    }

}
