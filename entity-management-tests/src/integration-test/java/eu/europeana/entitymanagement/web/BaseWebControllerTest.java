package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML;
import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
import static eu.europeana.api.commons.web.http.HttpHeaders.VALUE_LDP_RESOURCE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.batch.service.ScheduledTaskService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTask;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledTaskType;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.exception.ingestion.EntityUpdateException;
import eu.europeana.entitymanagement.solr.service.SolrService;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.testutils.TestConfig;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;

@Import(TestConfig.class)
abstract class BaseWebControllerTest extends AbstractIntegrationTest {

  protected MockMvc mockMvc;

  @Autowired protected SolrService solrService;

  @Autowired protected ScheduledTaskService scheduledTaskService;

  @Autowired private EntityUpdateService entityUpdateService;

  @Qualifier(AppConfig.BEAN_EM_SOLR_SERVICE)
  @Autowired
  protected SolrService emSolrService;

  @Autowired private WebApplicationContext webApplicationContext;

  @BeforeEach
  protected void setup() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

    // ensure a clean db between test runs
    this.entityRecordService.dropRepository();
    this.scheduledTaskService.dropCollection();
    this.emConceptSchemeService.dropRepository();
    emSolrService.deleteAllDocuments();
  }

  /** Checks common response headers. Allow header checked within each test method. */
  protected void checkCommonResponseHeaders(ResultActions results, boolean hasPathExtension)
      throws Exception {
    checkCommonResponseHeaders(results, hasPathExtension, false);
  }

  protected void checkCommonResponseHeaders(
      ResultActions results, boolean hasPathExtension, boolean hasXmlResponse) throws Exception {
    results
        .andExpect(header().exists(HttpHeaders.ETAG))
        .andExpect(header().string(HttpHeaders.LINK, is(VALUE_LDP_RESOURCE)));
    if (!hasPathExtension) {
      results.andExpect(
          header().stringValues(HttpHeaders.VARY, hasItems(containsString(HttpHeaders.ACCEPT))));
    }
    if (hasXmlResponse) {
      results.andExpect(
          header().string(HttpHeaders.CONTENT_TYPE, is(CONTENT_TYPE_APPLICATION_RDF_XML)));
    } else {
      results.andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(CONTENT_TYPE_JSONLD_UTF8)));
    }
  }

  protected void checkCorsHeaders(ResultActions results, boolean hasPathExtension)
      throws Exception {
    results.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, is("*")));

    if (hasPathExtension) {
      results.andExpect(
          header()
              .stringValues(
                  HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                  HttpHeaders.ALLOW,
                  HttpHeaders.LINK,
                  HttpHeaders.ETAG));
    } else {
      results.andExpect(
          header()
              .stringValues(
                  HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                  HttpHeaders.ALLOW,
                  HttpHeaders.LINK,
                  HttpHeaders.VARY,
                  HttpHeaders.ETAG));
    }
  }

  protected void checkAllowHeaderForPOST(ResultActions results) throws Exception {
    results.andExpect(header().stringValues(HttpHeaders.ALLOW, hasItems(containsString("POST"))));
  }

  protected void checkAllowHeaderForGET(ResultActions results) throws Exception {
    results.andExpect(header().stringValues(HttpHeaders.ALLOW, hasItems(containsString("GET"))));
  }

  /** Expects Allow header in response to contain DELETE,POST,GET,PUT */
  protected void checkAllowHeaderForDPGP(ResultActions results) throws Exception {
    results.andExpect(
        header()
            .stringValues(
                HttpHeaders.ALLOW,
                hasItems(
                    containsString("GET"),
                    containsString("DELETE"),
                    containsString("POST"),
                    containsString("PUT"))));
  }

  protected EntityRecord createEntity(
      String europeanaProxyEntityStr, String metisResponse, String externalId) throws Exception {
    Entity europeanaProxyEntity = objectMapper.readValue(europeanaProxyEntityStr, Entity.class);
    XmlBaseEntityImpl<?> xmlBaseEntity =
        MetisDereferenceUtils.parseMetisResponse(
            jaxbContext.createUnmarshaller(), externalId, metisResponse);

    assert xmlBaseEntity != null;
    DataSource dataSource = datasources.verifyDataSource(externalId, false);
    EntityRecord savedRecord =
        entityRecordService.createEntityFromRequest(
            europeanaProxyEntity, xmlBaseEntity.toEntityModel(), dataSource);

    // trigger update to generate consolidated entity
    entityUpdateService.runSynchronousUpdate(savedRecord.getEntityId());

    // return entityRecord version with consolidated entity
    return entityRecordService.retrieveByEntityId(savedRecord.getEntityId()).orElseThrow();
  }

  protected EntityRecord createConcept() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    return createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
  }

  protected EntityRecord createTimeSpan() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.TIMESPAN_1ST_CENTURY_XML);

    return createEntity(
        europeanaMetadata, metisResponse, IntegrationTestUtils.TIMESPAN_1ST_CENTURY_URI);
  }

  protected EntityRecord createOrganization(String europeanaProxyEntityStr, Record zohoRecord)
      throws Exception {
    Entity europeanaProxyEntity = objectMapper.readValue(europeanaProxyEntityStr, Entity.class);
    DataSource dataSource = datasources.verifyDataSource(europeanaProxyEntity.getEntityId(), false);
    Organization zohoOrganization =
        zohoOrgConverter.convertToOrganizationEntity(zohoRecord);
    EntityRecord savedRecord =
        entityRecordService.createEntityFromRequest(
            europeanaProxyEntity, zohoOrganization, dataSource);

    // trigger update to generate consolidated entity
    entityUpdateService.runSynchronousUpdate(savedRecord.getEntityId());

    // return entityRecord version with consolidated entity
    return entityRecordService.retrieveByEntityId(savedRecord.getEntityId()).orElseThrow();
  }

  protected void deprecateEntity(EntityRecord entityRecord) throws EntityUpdateException {
    entityRecordService.disableEntityRecord(entityRecord, true);
  }

  protected void assertedTaskScheduled(String entityId, ScheduledTaskType taskType) {
    // check that record is disabled
    Optional<ScheduledTask> scheduledTask = scheduledTaskService.getTask(entityId);
    Assertions.assertTrue(scheduledTask.isPresent());

    Assertions.assertEquals(taskType, scheduledTask.get().getUpdateType());
  }
}
