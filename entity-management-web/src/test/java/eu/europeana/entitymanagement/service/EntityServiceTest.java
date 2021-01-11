package eu.europeana.entitymanagement.service;

import eu.europeana.entitymanagement.config.DataSource;
import eu.europeana.entitymanagement.config.DataSources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EntityServiceTest {

    @MockBean
    MetisDereferenceService dereferenceService;

    private final String SOURCE_TEST_URL_1 = "http://www.wikidata.org/";
    private final String SOURCE_TEST_RIGHTS_1 = "https://creativecommons.org/publicdomain/zero/1.0/";

    DataSources sources = new DataSources(Collections.singletonList(new DataSource(SOURCE_TEST_URL_1, SOURCE_TEST_RIGHTS_1)));

    @Test
    void shouldMatchEntityIdToDatasource() {
        EntityService service = new EntityService(sources, dereferenceService);

        assertTrue(service.checkSourceExists("http://www.wikidata.org/entity/Q11019"));
        assertFalse(service.checkSourceExists("http://random-url/1234"));
    }
}