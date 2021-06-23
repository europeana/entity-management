package eu.europeana.entitymanagement.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import javax.annotation.Resource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Concept;

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

	String broader = entity.getBroader().get(0);
	assertEquals("http://www.wikidata.org/entity/Q987767", broader);
		
	assertEquals(7, entity.getNote().size());

	
    }
  
  
}