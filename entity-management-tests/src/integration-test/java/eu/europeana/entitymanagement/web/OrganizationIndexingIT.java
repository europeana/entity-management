package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Optional;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.config.AppAutoconfig;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.solr.model.SolrEntity;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;

@SpringBootTest
@AutoConfigureMockMvc
public class OrganizationIndexingIT extends BaseWebControllerTest {

  @Qualifier(AppAutoconfig.BEAN_EM_SOLR_SERVICE)
  @Autowired
  private SolrService emSolrService;

  @Test
  public void searchOrganizationByDereferencedProps() throws Exception {
    // 1. create a place "Sweden" to be used to dereference zoho country for the zoho GFM org
    String europeanaMetadata = loadFile(IntegrationTestUtils.PLACE_REGISTER_SWEDEN_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.PLACE_SWEDEN_XML);
    createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.PLACE_SWEDEN_URI);

    // 2. register zoho GFM org
    europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord = IntegrationTestUtils
        .getZohoOrganizationRecord(IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    // search by ISO Code
    List<SolrEntity<Entity>> res = emSolrService.searchByQuery(new SolrQuery("country:SE"));
    assertEquals(1, res.size());
    assertEquals(entityId, res.get(0).getEntityId());

    // search by europeanaRole
    res = emSolrService.searchByQuery(new SolrQuery(
        "europeanaRole:\"http://data.europeana.eu/vocabulary/role/ProvidingInstitution\""));
    assertEquals(1, res.size());
    assertEquals(entityId, res.get(0).getEntityId());

    // search by country label
    res = emSolrService.searchByQuery(new SolrQuery("countryLabel.en:Sweden"));
    assertEquals(1, res.size());
    assertEquals(entityId, res.get(0).getEntityId());
  }



}
