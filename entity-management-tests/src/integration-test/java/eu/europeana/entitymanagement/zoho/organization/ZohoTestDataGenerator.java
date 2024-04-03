package eu.europeana.entitymanagement.zoho.organization;

import static org.junit.Assert.assertNotNull;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.testutils.ZohoRecordTestDeserializer;

/** JUnit test to check if DataSources are properly deserialized from XML */
@SpringBootTest(classes = {SerializationConfig.class, ZohoConfiguration.class, EntityManagementConfiguration.class})
public class ZohoTestDataGenerator {

  @Autowired
  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  private ObjectMapper jsonMapper;
  
  @Autowired
  ZohoConfiguration zohoConfiguration;
  EntityManagementConfiguration emConfig;
  
  @Bean
  ZohoDereferenceService getZohoDereferenceService() {
    return new ZohoDereferenceService(zohoConfiguration, emConfig);
  }  

//  @Test
  //manually enable the test when data needs to be generated
  public void generateBNFJson() throws Exception {
    //get original zoho record
    Optional<Record> zohoOrganization =
        zohoConfiguration.getZohoAccessClient().getZohoRecordOrganizationById(IntegrationTestUtils.ORGANIZATION_PCCE_URI_ZOHO);
    String zohoRecord = getZohoDereferenceService().serialize(zohoOrganization.get());
    //if you need to see original enable : System.out.println(zohoRecord);
    System.out.println(zohoRecord);
    
    //deserialize data with the test deserializer
    ZohoRecordTestDeserializer zohoRecordDeserializer = new ZohoRecordTestDeserializer();
    //TODO: many fields are missing or need to be renamed in the deserializer, need to include all fields from ZohoMaping except for the sensitive ones (like email addresses)
    Record zohoTestRecord = zohoRecordDeserializer.deserialize(jsonMapper.getFactory().createParser(zohoRecord), jsonMapper.getDeserializationContext());
    
    //serialize test data
    String zohoTestRecordJson = getZohoDereferenceService().serialize(zohoTestRecord);
    System.out.println(zohoTestRecordJson);
    
    Record zohoTestRecordFiltered = zohoRecordDeserializer.deserialize(jsonMapper.getFactory().createParser(zohoTestRecordJson), jsonMapper.getDeserializationContext());
    assertNotNull(zohoTestRecordFiltered.getId());
    assertNotNull(zohoTestRecordFiltered.getKeyValue("Account_Name"));
    
   }
}