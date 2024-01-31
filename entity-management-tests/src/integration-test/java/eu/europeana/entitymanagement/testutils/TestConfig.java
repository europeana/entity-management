package eu.europeana.entitymanagement.testutils;

import java.util.Optional;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;

@TestConfiguration
public class TestConfig {

  
  public static final String MOCK_ZOHO_BASE_URL = "https://crm.zoho.com/crm/org51823723/tab/Accounts/";
  public static final String MOCK_ZOHO_COUNTRY_MAPPING_FILE = "/zoho_country_mapping_test.json";
  
  @Autowired
  ZohoConfiguration zohoConfiguration;
  
  /**
   * Since requests to Zoho are done via its SDK, and require authentication first, we mock out the
   * entire flow with Mockito.
   */
  @Primary
  @Bean
  public ZohoConfiguration configureZoho() throws Exception {
    ZohoConfiguration zohoConfiguration = Mockito.mock(ZohoConfiguration.class);
    ZohoAccessClient zohoClient = Mockito.mock(ZohoAccessClient.class);
    Mockito.when(zohoConfiguration.getZohoAccessClient()).thenReturn(zohoClient);
    Mockito.when(zohoConfiguration.getZohoBaseUrl()).thenReturn(zohoConfiguration.getZohoBaseUrl());

    // find matching JSON file based on zohoId argument, then create a Record object for it
    Mockito.doAnswer(
            (Answer<Optional<Record>>)
                invocation -> {
                  String zohoId = invocation.getArgument(0);
                  return IntegrationTestUtils.getZohoOrganizationRecord(zohoId);
                })
        .when(zohoClient)
        .getZohoRecordOrganizationById(ArgumentMatchers.any(String.class));

    return zohoConfiguration;
  }
}
