package eu.europeana.entitymanagement.web.model.metis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Test;

import eu.europeana.entitymanagement.definitions.model.impl.BaseConcept;
import eu.europeana.entitymanagement.definitions.model.impl.BaseEntity;

class MetisResponseDeserializerTest {

    EnrichmentResultList resultList;

    @Test
    void testObjectToXml() throws JAXBException, FileNotFoundException {
        String XML_FILE = "/metis-deref/response.xml";
        InputStream is = getClass().getResourceAsStream(XML_FILE);
        JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        resultList = (EnrichmentResultList) unmarshaller.unmarshal(is);

        assertNotNull(resultList);
        assertEquals(1, resultList.getEnrichmentBaseResultWrapperList().size());

        // get unmarshalled object
        BaseEntity entity = resultList.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0);
        assertTrue(entity instanceof BaseConcept);

        // value should match rdf:About in xml file
        assertEquals("http://www.wikidata.org/entity/Q152095", entity.getAbout());
    }

}
