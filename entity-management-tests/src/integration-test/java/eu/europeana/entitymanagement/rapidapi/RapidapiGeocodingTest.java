package eu.europeana.entitymanagement.rapidapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.definitions.model.Address;
import eu.europeana.entitymanagement.zoho.organization.RapidapiGeocodingClient;

@SpringBootTest
public class RapidapiGeocodingTest extends AbstractIntegrationTest {

  @Autowired
  private RapidapiGeocodingClient rapidapiGeocodingClient;

  //@Test
  public void getRapidapiGeocodingURI() throws Exception {
    Address address = new Address();
    address.setVcardStreetAddress("34 West 13th Street");
    address.setVcardPostalCode("10011");
    address.setVcardCountryName("USA");
    
    String geoURI = rapidapiGeocodingClient.getGeoURI(address);
    Assertions.assertNotNull(geoURI);
    Assertions.assertTrue(geoURI.contains("geo:"));
  }

}
