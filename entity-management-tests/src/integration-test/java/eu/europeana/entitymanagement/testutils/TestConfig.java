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
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;

@TestConfiguration
public class TestConfig {

  
  public static final String MOCK_ZOHO_BASE_URL = "https://crm.zoho.eu/crm/org20085137532/tab_test/";
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
    Mockito.when(zohoConfiguration.getZohoBaseUrlOrganizations()).thenReturn(
        MOCK_ZOHO_BASE_URL + ZohoConstants.ACCOUNTS_MODULE_NAME);
    Mockito.when(zohoConfiguration.getZohoBaseUrlAggregators()).thenReturn(
        MOCK_ZOHO_BASE_URL + ZohoConstants.AGGREGATORS_MODULE_NAME);
   
    //configure zoho mock up to return JSON files responses for the real zoho calls
    Mockito.doAnswer(
        (Answer<Optional<Record>>)
            invocation -> {
              String zohoUrl = invocation.getArgument(0);
              return IntegrationTestUtils.getZohoOrganizationByUrl(zohoUrl);
            })
    .when(zohoClient)
    .getZohoOrganizationByUrl(ArgumentMatchers.any(String.class));

    Mockito.doAnswer(
        (Answer<Optional<Record>>)
            invocation -> {
              String zohoUrl = invocation.getArgument(0);
              return IntegrationTestUtils.searchZohoOrganizationByName(zohoUrl);
            })
    .when(zohoClient)
    .searchZohoOrganizationByName(ArgumentMatchers.any(String.class));

    Mockito.doAnswer(
        (Answer<Optional<Record>>)
            invocation -> {
              String orgUrl = invocation.getArgument(0);
              return IntegrationTestUtils.getZohoAggregatorByOrgUrl(orgUrl);
            })
    .when(zohoClient)
    .getZohoAggregatorByOrgUrl(ArgumentMatchers.any(String.class));

    Mockito.doAnswer(
        (Answer<Optional<Record>>)
            invocation -> {
              String orgName = invocation.getArgument(0);
              //String aggregName = invocation.getArgument(1);
              return IntegrationTestUtils.searchZohoAggregatedViaModule(orgName);
            })
    .when(zohoClient)
    .searchZohoAggregatedViaModule(ArgumentMatchers.any(String.class));

    return zohoConfiguration;
  }
}
