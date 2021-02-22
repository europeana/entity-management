package eu.europeana.entitymanagement.web.model.metis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Test;

import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.serialization.XmlMultilingualString;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;

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
	ConceptImpl xmlEntity = (ConceptImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
		.getEnrichmentBaseList().get(0);
	assertEquals("http://www.wikidata.org/entity/Q152095", xmlEntity.getAbout());
	
	//check prefLabels
	assertNotNull(xmlEntity.getPrefLabel());
	assertEquals(23, xmlEntity.getPrefLabel().size());	
	Optional<List<String>> optionalIsbn = xmlEntity.getPrefLabel().entrySet().stream()
			  .filter(e -> e.getKey().contains("en"))
			  .map(Map.Entry::getValue)
			  .findFirst();	
	assertEquals("bathtub", optionalIsbn.get().get(0));

	//check altLabels
	assertNotNull(xmlEntity.getAltLabel());
	assertEquals(14, xmlEntity.getAltLabel().size());
	Optional<List<String>> optionalIsbn2 = xmlEntity.getPrefLabel().entrySet().stream()
			  .filter(e -> e.getKey().contains("de"))
			  .map(Map.Entry::getValue)
			  .findFirst();	
	assertEquals("Wannenbad", optionalIsbn2.get().get(0));

	
	String[] broader = xmlEntity.getBroader();
	if (broader!=null) assertEquals("http://www.wikidata.org/entity/Q987767", broader[0]);
	
	assertEquals(7, xmlEntity.getNote().size());
		
//	System.out.println(xmlEntity.getPrefLabel());
	
    }

}
