package eu.europeana.entitymanagement.zoho.organization;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.zoho.crm.api.record.Record;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

@SpringBootTest(classes = {ZohoOrganizationConverter.class, ZohoAccessConfiguration.class})
@Disabled("Excluded from automated runs as this depends on Zoho.")
public class ZohoAccessTest {

	  @Qualifier(AppConfigConstants.BEAN_ZOHO_ACCESS_CONFIGURATION)
	  @Autowired
	  private ZohoAccessConfiguration zohoAccessConfiguration;
	  
	  @Test
	  public void zohoAccessTest() throws ZohoException, Exception {
		  ZohoOrganizationConverter zohoOrganizationConverter = new ZohoOrganizationConverter();
		  String organizationId = "1482250000002112001";
		  
		  Optional<Record> zohoOrganization = zohoAccessConfiguration.getZohoAccessClient().getZohoRecordOrganizationById(organizationId);
		  Organization org = null;
		  if (zohoOrganization.isPresent()) {
			  org = zohoOrganizationConverter.convertToOrganizationEntity(zohoOrganization.get());
		  }
		  Assertions.assertNotNull(org);
	  }
}
