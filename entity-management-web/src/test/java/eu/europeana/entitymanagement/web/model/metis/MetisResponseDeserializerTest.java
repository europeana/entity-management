package eu.europeana.entitymanagement.web.model.metis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.entitymanagement.web.xml.model.LabelledResource;
import eu.europeana.entitymanagement.web.xml.model.XmlAgentImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlConceptImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlOrganizationImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlPlaceImpl;
import eu.europeana.entitymanagement.web.xml.model.XmlTimespanImpl;
import eu.europeana.entitymanagement.web.xml.model.metis.EnrichmentResultList;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MetisResponseDeserializerTest {

	private static JAXBContext jaxbContext;

	@BeforeAll
	static void beforeAll() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
	}

	@Test
    void shouldDeserializeConcept() throws Exception {
			EnrichmentResultList resultList = getEnrichmentResultList("/metis-deref/concept-response.xml");

			// get unmarshalled object
	XmlConceptImpl xmlEntity = (XmlConceptImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
		.getEnrichmentBaseList().get(0);
	assertEquals("http://www.wikidata.org/entity/Q152095", xmlEntity.getAbout());

	//check prefLabels
	assertNotNull(xmlEntity.getPrefLabel());
	assertEquals(23, xmlEntity.getPrefLabel().size());
	LabelledResource enLabel = xmlEntity.getPrefLabel().stream()
		.filter(label -> label.getLang().equals("en")).findFirst().get();
	assertEquals("bathtub", enLabel.getValue());

	//check altLabels
	assertNotNull(xmlEntity.getAltLabel());
	assertEquals(14, xmlEntity.getAltLabel().size());
	LabelledResource deAltLabel = xmlEntity.getAltLabel().stream()
		.filter(altLabel -> altLabel.getLang().equals("de")).findFirst().get();
	assertEquals("Wannenbad", deAltLabel.getValue());


	String broader = xmlEntity.getBroader().get(0).getResource();
	assertEquals("http://www.wikidata.org/entity/Q987767", broader);

	assertEquals(7, xmlEntity.getNote().size());

	//TODO: Add tests for other entity types

    }

	@Test
	void shouldDeserializeAgent() throws Exception {
		EnrichmentResultList resultList = getEnrichmentResultList("/metis-deref/agent.xml");
		XmlAgentImpl xmlEntity = (XmlAgentImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
				.getEnrichmentBaseList().get(0);

		assertEquals("http://www.wikidata.org/entity/Q762", xmlEntity.getAbout());

		//check prefLabels
		assertNotNull(xmlEntity.getPrefLabel());
		assertEquals(26, xmlEntity.getPrefLabel().size());
		LabelledResource enLabel = xmlEntity.getPrefLabel().stream()
				.filter(label -> label.getLang().equals("en")).findFirst().get();
		assertEquals("Leonardo da Vinci", enLabel.getValue());

		// check dates
		assertEquals("1452-04-24T00:00:00Z", xmlEntity.getDateOfBirth());
		assertEquals("1519-05-12T00:00:00Z", xmlEntity.getDateOfDeath());

		assertEquals(27, xmlEntity.getProfessionOrOccupation().size());
		//TODO: assert other fields
	}

	@Test
	void shouldDeserializeTimespan() throws Exception {
		EnrichmentResultList resultList = getEnrichmentResultList("/metis-deref/timespan.xml");
		XmlTimespanImpl timespan = (XmlTimespanImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
				.getEnrichmentBaseList().get(0);

		assertEquals("http://www.wikidata.org/entity/Q8106", timespan.getAbout());

		assertEquals(1, timespan.getIsPartOf().size());
		assertEquals("0001-01-01", timespan.getBegin());
		assertEquals("0100-12-31", timespan.getEnd());

		//TODO: assert other properties
	}


	@Test
	void shouldDeserializePlace() throws Exception {
		EnrichmentResultList resultList = getEnrichmentResultList("/metis-deref/place.xml");
		XmlPlaceImpl place = (XmlPlaceImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
				.getEnrichmentBaseList().get(0);

		assertEquals("https://sws.geonames.org/2988507/", place.getAbout());

		assertEquals(1, place.getIsPartOf().size());
		assertEquals(48.85341F, place.getLatitude());
		assertEquals(2.3488F, place.getLongitude());
		assertEquals("https://sws.geonames.org/3017382/", place.getIsPartOf().get(0).getResource());

		//TODO: assert other properties
	}

	@Test
	void shouldDeserializeOrganizations() throws Exception {
		EnrichmentResultList resultList = getEnrichmentResultList("/metis-deref/organization.xml");
		XmlOrganizationImpl organization = (XmlOrganizationImpl) resultList.getEnrichmentBaseResultWrapperList().get(0)
				.getEnrichmentBaseList().get(0);

		assertEquals("http://www.wikidata.org/entity/Q193563", organization.getAbout());
		assertEquals(72, organization.getAltLabel().size());
		assertEquals(23, organization.getPrefLabel().size());

		//TODO: complete assertions
	}

	@NotNull
	private EnrichmentResultList getEnrichmentResultList(String xmlFile) throws Exception {
		InputStream is = getClass().getResourceAsStream(xmlFile);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		EnrichmentResultList resultList = (EnrichmentResultList) unmarshaller.unmarshal(is);

		assertNotNull(resultList);
		assertEquals(1, resultList.getEnrichmentBaseResultWrapperList().size());
		return resultList;
	}

}
