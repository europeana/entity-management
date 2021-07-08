package eu.europeana.entitymanagement.web.model.metis;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.entitymanagement.web.MetisDereferenceUtils;
import eu.europeana.entitymanagement.web.xml.model.*;
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

	private static Unmarshaller unmarshaller;

	@BeforeAll
	static void beforeAll() throws JAXBException {
		 unmarshaller = JAXBContext.newInstance(EnrichmentResultList.class).createUnmarshaller();
	}

	@Test
    void shouldDeserializeConcept() throws Exception {
		String uri = "http://www.wikidata.org/entity/Q152095";
		// get unmarshalled object
		XmlConceptImpl xmlEntity = (XmlConceptImpl)MetisDereferenceUtils.parseMetisResponse(unmarshaller, uri,
				loadFile("/metis-deref/concept-response.xml"));
		assert xmlEntity != null;
		assertEquals(uri, xmlEntity.getAbout());

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
		String uri = "http://www.wikidata.org/entity/Q762";
		XmlAgentImpl xmlEntity = (XmlAgentImpl)MetisDereferenceUtils.parseMetisResponse(unmarshaller, uri,
				loadFile("/metis-deref/agent_da_vinci.xml"));
		assert xmlEntity != null;

		assertEquals(uri, xmlEntity.getAbout());

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
		String uri = "http://www.wikidata.org/entity/Q8106";
		XmlTimespanImpl timespan = (XmlTimespanImpl)MetisDereferenceUtils.parseMetisResponse(unmarshaller, uri,
				loadFile("/metis-deref/timespan.xml"));
		assert timespan != null;

		assertEquals("http://www.wikidata.org/entity/Q8106", timespan.getAbout());

		assertEquals(1, timespan.getIsPartOf().size());
		assertEquals("0001-01-01", timespan.getBegin());
		assertEquals("0100-12-31", timespan.getEnd());

		//TODO: assert other properties
	}


	@Test
	void shouldDeserializePlace() throws Exception {
		String uri = "https://sws.geonames.org/2988507/";
		XmlPlaceImpl place = (XmlPlaceImpl)MetisDereferenceUtils.parseMetisResponse(unmarshaller, uri,
				loadFile("/metis-deref/place.xml"));
		assert place != null;

		assertEquals("https://sws.geonames.org/2988507/", place.getAbout());

		assertEquals(1, place.getIsPartOf().size());
		assertEquals(48.85341F, place.getLatitude());
		assertEquals(2.3488F, place.getLongitude());
		assertEquals("https://sws.geonames.org/3017382/", place.getIsPartOf().get(0).getResource());

		//TODO: assert other properties
	}

	@Test
	void shouldDeserializeOrganizations() throws Exception {
		String uri = "http://www.wikidata.org/entity/Q193563";
		XmlOrganizationImpl organization = (XmlOrganizationImpl)MetisDereferenceUtils.parseMetisResponse(unmarshaller, uri,
				loadFile("/metis-deref/organization.xml"));
		assert organization != null;
		assertEquals("http://www.wikidata.org/entity/Q193563", organization.getAbout());
		assertEquals(72, organization.getAltLabel().size());
		assertEquals(23, organization.getPrefLabel().size());

		//TODO: complete assertions
	}
}
