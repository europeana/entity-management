package eu.europeana.entitymanagement.web.model.metis;

import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        XmlBaseEntityImpl entity = resultList.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0);
        assertTrue(entity instanceof XmlConceptImpl);

        // value should match rdf:About in xml file
        assertEquals("http://www.wikidata.org/entity/Q152095", entity.getAbout());
    }

}
