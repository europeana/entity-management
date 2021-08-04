package eu.europeana.entitymanagement.zoho.organization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import eu.europeana.entitymanagement.web.xml.model.XmlOrganizationImpl;
import eu.europeana.entitymanagement.wikidata.WikidataAccessDao;
import eu.europeana.entitymanagement.wikidata.WikidataAccessService;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

@SpringBootTest(classes = {WikidataAccessService.class, WikidataAccessDao.class})
@Disabled("Excluded from automated runs as this depends on Wikidata.")
public class WikidataDereferenceTest {

	  @Test
	  public void wikidataDereferenceTest() throws ZohoException, Exception {
		  WikidataAccessService wikidataService = new WikidataAccessService(new WikidataAccessDao());
		  String organizationId = "http://www.wikidata.org/entity/Q1781094";
		  XmlOrganizationImpl org = wikidataService.dereference(organizationId);
		  Assertions.assertNotNull(org);
	  }
}
