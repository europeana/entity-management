package eu.europeana.entitymanagement.testutils;

import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.organization.ZohoAccessConfiguration;
import java.util.Optional;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

  
  public static final String MOCK_ZOHO_BASE_URL = "https://crm.zoho.com/crm/org51823723/tab/Accounts/";
  /**
   * Since requests to Zoho are done via its SDK, and require authentication first, we mock out the
   * entire flow with Mockito.
   */
  @Primary
  @Bean
  public ZohoAccessConfiguration configureZoho() throws Exception {
    ZohoAccessConfiguration zohoAccessConfiguration = Mockito.mock(ZohoAccessConfiguration.class);
    ZohoAccessClient zohoClient = Mockito.mock(ZohoAccessClient.class);
    Mockito.when(zohoAccessConfiguration.getZohoAccessClient()).thenReturn(zohoClient);
    Mockito.when(zohoAccessConfiguration.getZohoBaseUrl()). thenReturn(MOCK_ZOHO_BASE_URL);

    // find matching JSON file based on zohoId argument, then create a Record object for it
    Mockito.doAnswer(
            (Answer<Optional<Record>>)
                invocation -> {
                  String zohoId = invocation.getArgument(0);
                  return IntegrationTestUtils.getZohoOrganizationRecord(zohoId);
                })
        .when(zohoClient)
        .getZohoRecordOrganizationById(ArgumentMatchers.any(String.class));

    return zohoAccessConfiguration;
  }
}
