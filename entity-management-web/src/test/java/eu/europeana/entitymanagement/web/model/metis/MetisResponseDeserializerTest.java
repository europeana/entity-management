package eu.europeana.entitymanagement.web.model.metis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.entitymanagement.web.xml.model.LabelResource;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

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
	XmlConceptImpl xmlEntity = (XmlConceptImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
		.getEnrichmentBaseList().get(0);
	assertEquals("http://www.wikidata.org/entity/Q152095", xmlEntity.getAbout());
	
	//check prefLabels
	assertNotNull(xmlEntity.getPrefLabel());
	assertEquals(23, xmlEntity.getPrefLabel().size());
	LabelResource enLabel = xmlEntity.getPrefLabel().stream()
		.filter(label -> label.getLang().equals("en")).findFirst().get();
	assertEquals("bathtub", enLabel.getValue());

	//check altLabels
	assertNotNull(xmlEntity.getAltLabel());
	assertEquals(14, xmlEntity.getAltLabel().size());
	LabelResource deAltLabel = xmlEntity.getAltLabel().stream()
		.filter(altLabel -> altLabel.getLang().equals("de")).findFirst().get();
	assertEquals("Wannenbad", deAltLabel.getValue());

	
	String broader = xmlEntity.getBroader().get(0).getResource();
	assertEquals("http://www.wikidata.org/entity/Q987767", broader);
	
	assertEquals(7, xmlEntity.getNote().size());

	//TODO: Add tests for other entity types
	
    }

}
