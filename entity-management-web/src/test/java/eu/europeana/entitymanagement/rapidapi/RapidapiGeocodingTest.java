package eu.europeana.entitymanagement.rapidapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.geocoding.RapidApiGeocodingClient;
import eu.europeana.entitymanagement.definitions.model.Address;

@SpringBootTest(classes = {SerializationConfig.class, EntityManagementConfiguration.class})
@ActiveProfiles("test")//enable application-test.yml
@Disabled
public class RapidapiGeocodingTest {

  @Autowired EntityManagementConfiguration emConfig;
  
  //@Test
  public void getRapidapiGeocodingURI() throws Exception {
    RapidApiGeocodingClient rapidapiGeocodingClient = new RapidApiGeocodingClient(null, emConfig.getRapidApiKey());
    
    
    Address address = new Address();
    address.setVcardStreetAddress("34 West 13th Street");
    address.setVcardPostalCode("10011");
    address.setVcardCountryName("USA");
    
    String geoURI = rapidapiGeocodingClient.getGeoURI(address);
    Assertions.assertNotNull(geoURI);
    Assertions.assertEquals("geo:40.7359607,-73.9959654", geoURI);
  }

}
