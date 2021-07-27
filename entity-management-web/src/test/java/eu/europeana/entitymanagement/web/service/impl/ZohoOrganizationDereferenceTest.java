package eu.europeana.entitymanagement.web.service.impl;

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
import eu.europeana.entitymanagement.zoho.ZohoAccessClient;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationImporterConfiguration;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;

@SpringBootTest(classes = {ZohoOrganizationConverter.class, ZohoOrganizationImporterConfiguration.class})
@Disabled("Excluded from automated runs as this depends on Zoho.")
public class ZohoOrganizationDereferenceTest {

	  @Qualifier(AppConfigConstants.BEAN_ZOHO_ORGANIZATION_CONVERTER)
	  @Autowired
	  private ZohoOrganizationConverter zohoOrganizationConverter;
	  
	  @Qualifier(AppConfigConstants.BEAN_ZOHO_ACCESS_CLIENT)
	  @Autowired
	  private ZohoAccessClient zohoAccessClient;
	  
	  @Test
	  public void organizationDereferenceTest() throws ZohoException, Exception {
		  String organizationId = "1482250000004513401";
		  Optional<Record> zohoOrganization = zohoAccessClient.getZohoRecordOrganizationById(organizationId);
		  Organization org = null;
		  if (zohoOrganization.isPresent()) {
			  org = zohoOrganizationConverter.convertToOrganizationEntity(zohoOrganization.get());
		  }
		  Assertions.assertNotNull(org);
	  }
}
