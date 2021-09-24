package eu.europeana.entitymanagement.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import java.util.Optional;
import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;
import eu.europeana.entitymanagement.wikidata.WikidataAccessService;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

/**
 * JUnit test for testing the EMControllerTest class
 */
//EntityManagementApp.class
//@ContextConfiguration(classes = { JobLauncherConfig.class, EntityManagementConfiguration.class, AppConfig.class, EnrichmentConfig.class, ValidatorConfig.class,
//        SerializationConfig.class, MetisDereferenceService.class, })
//@ExtendWith(SpringExtension.class)
@Disabled("Excluded from automated runs as this depends on Metis")
public class MetisDereferenceServiceTest extends AbstractIntegrationTest {

    @Autowired
    private DereferenceServiceLocator dereferenceServiceLocator;
	  
    @Test
    public void dereferenceConceptById() throws Exception {

	//bathtube
	String entityId = "http://www.wikidata.org/entity/Q152095";
      Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(entityId, "Concept");
      Optional<Entity> entityOptional = dereferencer.dereferenceEntityById(entityId);
	assertTrue(entityOptional.isPresent());

	Concept entity = (Concept) entityOptional.get();
	assertEquals(entityId, entity.getEntityId());
	
	// get unmarshalled object
	assertEquals("http://www.wikidata.org/entity/Q152095", entity.getAbout());
		
	//check prefLabels
	assertNotNull(entity.getPrefLabel());
	assertEquals(23, entity.getPrefLabel().size());
	assertEquals("bathtub", entity.getPrefLabel().get("en"));

	//check altLabels
	assertNotNull(entity.getAltLabel());
	assertEquals(8, entity.getAltLabel().size());
	assertEquals("Wannenbad", entity.getAltLabel().get("de").get(0));

	String broader = entity.getBroader().get(0);
	assertEquals("http://www.wikidata.org/entity/Q987767", broader);
		
	assertEquals(8, entity.getNote().size());

	
    }
  
  
    @Test
    public void zohoOrganizationDereferenceTest() throws Exception {
            String organizationId = "https://crm.zoho.com/crm/org51823723/tab/Accounts/1482250000002112001";
      Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(organizationId, "Organization");
            Optional<Entity> orgOptional =  dereferencer.dereferenceEntityById(organizationId);
            
            Assertions.assertTrue(orgOptional.isPresent());

            Organization org = (Organization) orgOptional.get();
            assertEquals(1, org.getPrefLabel().size());
            assertEquals(1, org.getAltLabel().size());
            assertEquals(1, org.getAcronym().size());
            assertEquals(1, org.getEuropeanaRole().size());
            assertEquals(1, org.getGeographicLevel().size());
            assertEquals(1, org.getAcronym().size());
            assertEquals("FR", org.getCountry());
            Assertions.assertNotNull(org.getHomepage());
            Assertions.assertNotNull(org.getLogo());
            Assertions.assertNotNull(org.getAddress().getVcardStreetAddress());
            Assertions.assertNotNull(org.getAddress().getVcardCountryName());
            
    }
 
	@Test
  public void wikidataOrganizationDereferenceTest() throws ZohoException, Exception {
    String organizationId = "http://www.wikidata.org/entity/Q1781094";
    Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(organizationId,
        "Organization");
    Optional<Entity> orgOptional = dereferencer.dereferenceEntityById(organizationId);
    Assertions.assertTrue(orgOptional.isPresent());
  }
}