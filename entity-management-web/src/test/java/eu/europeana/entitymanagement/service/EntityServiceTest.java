package eu.europeana.entitymanagement.service;

import eu.europeana.entitymanagement.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityServiceTest {
    private final String SOURCE_TEST_URL_1 = "http://www.wikidata.org/";
    private final String SOURCE_TEST_RIGHTS_1 = "https://creativecommons.org/publicdomain/zero/1.0/";

    DataSources sources = new DataSources(Collections.singletonList(new DataSource(SOURCE_TEST_URL_1, SOURCE_TEST_RIGHTS_1)));

    @Test
    void shouldMatchEntityIdToDatasource() {
        EntityService service = new EntityService(sources, new MetisDereferenceService());

        assertTrue(service.checkSourceExists("http://www.wikidata.org/entity/Q11019"));
        assertFalse(service.checkSourceExists("http://random-url/1234"));
    }
}