package eu.europeana.entitymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.util.Choice;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.dereference.Dereferencer;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.testutils.TestConfig;
import eu.europeana.entitymanagement.web.service.DereferenceServiceLocator;
import eu.europeana.entitymanagement.zoho.organization.ZohoOrganizationConverter;
import eu.europeana.entitymanagement.zoho.utils.ZohoConstants;
import eu.europeana.entitymanagement.zoho.utils.ZohoException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** JUnit test for testing the DereferenceService class */
// enable test config to use zoho mocking
@Import(TestConfig.class)
// enable tests only on local machine
// @Disabled
@SpringBootTest
class DereferenceServiceIT extends AbstractIntegrationTest {

  @Autowired private DereferenceServiceLocator dereferenceServiceLocator;

  // @Test
  public void dereferenceConceptById() throws Exception {

    // bathtube
    String entityId = "http://www.wikidata.org/entity/Q152095";
    Dereferencer dereferencer = dereferenceServiceLocator.getDereferencer(entityId, "Concept");
    Optional<Entity> entityOptional = dereferencer.dereferenceEntityById(entityId);
    assertTrue(entityOptional.isPresent());

    Concept entity = (Concept) entityOptional.get();
    assertEquals(entityId, entity.getEntityId());

    // get unmarshalled object
    assertEquals("http://www.wikidata.org/entity/Q152095", entity.getAbout());

    // check prefLabels
    assertNotNull(entity.getPrefLabel());
    assertEquals(23, entity.getPrefLabel().size());
    assertEquals("bathtub", entity.getPrefLabel().get("en"));

    // check altLabels
    assertNotNull(entity.getAltLabel());
    assertEquals(8, entity.getAltLabel().size());
    assertEquals("Wannenbad", entity.getAltLabel().get("de").get(0));

    String broader = entity.getBroader().get(0);
    assertEquals("http://www.wikidata.org/entity/Q987767", broader);

    assertEquals(8, entity.getNote().size());
  }

  //  @Test
  public void zohoOrganizationDereferenceTest() throws Exception {
    String organizationId = IntegrationTestUtils.ORGANIZATION_BNF_URI_ZOHO;
    Dereferencer dereferencer =
        dereferenceServiceLocator.getDereferencer(organizationId, "Organization");
    Optional<Entity> orgOptional = dereferencer.dereferenceEntityById(organizationId);

    Assertions.assertTrue(orgOptional.isPresent());

    Organization org = (Organization) orgOptional.get();
    assertEquals(2, org.getPrefLabel().size());
    assertNull(org.getAltLabel());
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

  //  @Test
  public void zohoOrganizationDereferenceGFMTest() throws Exception {
    String organizationId = IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO;
    Dereferencer dereferencer =
        dereferenceServiceLocator.getDereferencer(organizationId, "Organization");
    Optional<Entity> orgOptional = dereferencer.dereferenceEntityById(organizationId);

    Assertions.assertTrue(orgOptional.isPresent());

    Organization org = (Organization) orgOptional.get();
    assertEquals(2, org.getPrefLabel().size());
    assertNotNull(org.getSameReferenceLinks());
  }

  //  @Test
  public void zohoOrganizationDereferenceLabelsTest() throws Exception {

    Record record = new Record();
    record.setId((long) 1);
    Choice<String> choice = new Choice<String>("EN");
    record.addKeyValue(ZohoConstants.LANG_ORGANIZATION_NAME_FIELD, choice);
    record.addKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD, "Austrian Institute of Technology");

    record.addKeyValue(
        ZohoConstants.ALTERNATIVE_FIELD + "_1", "Аустријски институт за технологију");
    choice = new Choice<String>("SR");
    record.addKeyValue(ZohoConstants.LANG_ALTERNATIVE_FIELD + "_1", choice);

    record.addKeyValue(
        ZohoConstants.ALTERNATIVE_FIELD + "_2", "AIT - Austrian Institute of Technology");
    choice = new Choice<String>("EN");
    record.addKeyValue(ZohoConstants.LANG_ALTERNATIVE_FIELD + "_2", choice);

    record.addKeyValue(
        ZohoConstants.ALTERNATIVE_FIELD + "_3", "Austrian Institute of Technology - AIT");
    choice = new Choice<String>("EN");
    record.addKeyValue(ZohoConstants.LANG_ALTERNATIVE_FIELD + "_3", choice);

    //    record.addKeyValue(ZohoConstants.ALTERNATIVE_FIELD + "_4", "Austrian Institute of
    // Technology");
    //    choice = new Choice<String>("EN");
    //    record.addKeyValue(ZohoConstants.LANG_ALTERNATIVE_FIELD + "_4", choice);

    Organization org = ZohoOrganizationConverter.convertToOrganizationEntity(record);

    Assertions.assertEquals(2, org.getPrefLabel().size());
    Assertions.assertEquals(1, org.getAltLabel().size());
    Assertions.assertEquals(2, org.getAltLabel().get("en").size());
  }

  @Test
  public void wikidataOrganizationDereferenceTest() throws ZohoException, Exception {
    // Naturalis
    dereferenceWikidataOrganization(IntegrationTestUtils.ORGANIZATION_NATURALIS_URI_WIKIDATA_URI);
  }

  @Test
  public void wikidataOrganizationBNFDereferenceTest() throws ZohoException, Exception {
    // BNF
    dereferenceWikidataOrganization(IntegrationTestUtils.ORGANIZATION_BNF_URI_WIKIDATA_URI);
  }

  @Test
  public void wikidataOrganizationBergerMuseumDereferenceTest() throws ZohoException, Exception {
    // Berger Museum
    dereferenceWikidataOrganization(IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI);
  }

  @Test
  public void wikidataOrganizationGFMDereferenceTest() throws ZohoException, Exception {
    // GFM
    dereferenceWikidataOrganization(IntegrationTestUtils.ORGANIZATION_GFM_URI_WIKIDATA_URI);
  }

  void dereferenceWikidataOrganization(String organizationId) throws Exception {
    Dereferencer dereferencer =
        dereferenceServiceLocator.getDereferencer(organizationId, "Organization");
    Optional<Entity> orgOptional = dereferencer.dereferenceEntityById(organizationId);
    Assertions.assertTrue(orgOptional.isPresent());
    Assertions.assertNotNull(orgOptional.get().getPrefLabel());
  }
}
