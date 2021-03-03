package eu.europeana.entitymanagement.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileWriter;

import javax.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;

/**
 * JUnit test for testing the EMControllerTest class
 */
@ContextConfiguration(classes = { EntityManagementApp.class})
@ExtendWith(SpringExtension.class)
@Disabled("Excluded from automated runs as this depends on Metis")
public class MetisDereferenceServiceTest {

    @Resource(name=AppConfig.BEAN_METIS_DEREF_SERVICE)
    MetisDereferenceService metisDerefService;

    @Test
    public void dereferenceConceptById() throws Exception {

	//bathtube
	String entityId = "http://www.wikidata.org/entity/Q152095";
	Concept entity = (Concept) metisDerefService.dereferenceEntityById(entityId);
	assertNotNull(entity);
	assertEquals(entityId, entity.getEntityId());
	
	// get unmarshalled object
	assertEquals("http://www.wikidata.org/entity/Q152095", entity.getAbout());
		
	//check prefLabels
	assertNotNull(entity.getPrefLabel());
	assertEquals(23, entity.getPrefLabel().size());
	assertEquals("bathtub", entity.getPrefLabelStringMap().get("en"));

	//check altLabels
	assertNotNull(entity.getAltLabel());
	assertEquals(8, entity.getAltLabel().size());
	assertEquals("Wannenbad", entity.getAltLabel().get("de").get(0));

	String broader = entity.getBroader()[0];
	assertEquals("http://www.wikidata.org/entity/Q987767", broader);
		
	assertEquals(7, entity.getNote().size());

	
    }

    @Test
    public void dereferenceEntitiesById() throws Exception {
    	
		//Agent
    	String XML_FILE_AGENT = "src\\test\\resources\\metis-deref\\agent.xml";
		String agentId = "http://www.wikidata.org/entity/Q762";
		String metisResponseBodyAgent = metisDerefService.getMetisWebClient().get()
				.uri(uriBuilder -> uriBuilder.path("/dereference").queryParam("uri", agentId).build())
				.accept(MediaType.APPLICATION_XML).retrieve()
				// return 400 for 4xx responses from Metis
				.onStatus(HttpStatus::is4xxClientError,
					response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
				// return 500 for everything else
				.onStatus(HttpStatus::isError,
					response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
				.bodyToMono(String.class).block();

		FileWriter fw_agent = new FileWriter(XML_FILE_AGENT);
		fw_agent.append(metisResponseBodyAgent);
		fw_agent.close();
	    
	    //Place
    	String XML_FILE_PLACE = "src\\test\\resources\\metis-deref\\place.xml";
		String placeId = "https://sws.geonames.org/2988507/";
		String metisResponseBodyPlace = metisDerefService.getMetisWebClient().get()
				.uri(uriBuilder -> uriBuilder.path("/dereference").queryParam("uri", placeId).build())
				.accept(MediaType.APPLICATION_XML).retrieve()
				// return 400 for 4xx responses from Metis
				.onStatus(HttpStatus::is4xxClientError,
					response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
				// return 500 for everything else
				.onStatus(HttpStatus::isError,
					response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
				.bodyToMono(String.class).block();

		FileWriter fw_place = new FileWriter(XML_FILE_PLACE);
		fw_place.write(metisResponseBodyPlace);
		fw_place.close();
	    
	    //Concept
    	String XML_FILE_CONCEPT = "src\\test\\resources\\metis-deref\\concept.xml";
		String conceptId = "http://www.wikidata.org/entity/Q152095";
		String metisResponseBodyConcept = metisDerefService.getMetisWebClient().get()
				.uri(uriBuilder -> uriBuilder.path("/dereference").queryParam("uri", conceptId).build())
				.accept(MediaType.APPLICATION_XML).retrieve()
				// return 400 for 4xx responses from Metis
				.onStatus(HttpStatus::is4xxClientError,
					response -> response.bodyToMono(String.class).map(HttpBadRequestException::new))
				// return 500 for everything else
				.onStatus(HttpStatus::isError,
					response -> response.bodyToMono(String.class).map(EuropeanaApiException::new))
				.bodyToMono(String.class).block();

		FileWriter fw_concept = new FileWriter(XML_FILE_CONCEPT);
		fw_concept.write(metisResponseBodyConcept);
		fw_concept.close();	    
    }
  
}
