package eu.europeana.entitymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants;
import eu.europeana.entitymanagement.config.DataSources;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.testutils.MongoContainer;
import eu.europeana.entitymanagement.testutils.SolrContainer;
import eu.europeana.entitymanagement.testutils.TestConfig;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.web.MetisDereferenceUtils;
import eu.europeana.entitymanagement.web.service.ConceptSchemeService;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import eu.europeana.entitymanagement.zoho.organization.ZohoConfiguration;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@ComponentScan(basePackageClasses = EntityManagementBasePackageMapper.class)
@AutoConfigureMockMvc
@DirtiesContext
public abstract class AbstractIntegrationTest {
  private static final Logger logger = LogManager.getLogger(AbstractIntegrationTest.class);
  private static final MongoContainer MONGO_CONTAINER;
  private static final SolrContainer SOLR_CONTAINER;

  @Autowired protected JAXBContext jaxbContext;

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired protected EntityUpdateService entityUpdateService;
  @Autowired protected DataSources datasources;
  @Autowired protected EntityRecordRepository entityRecordRepository;
  @Autowired protected ConceptSchemeService emConceptSchemeService;
  @Autowired protected EntityManagementConfiguration emConfig;
  @Autowired protected ZohoConfiguration zohoConfiguration;
  
  static {
    MONGO_CONTAINER =
        new MongoContainer("entity-management", "job-repository", "enrichment")
            .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

    MONGO_CONTAINER.start();

    SOLR_CONTAINER =
        new SolrContainer("entity-management")
            .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

    SOLR_CONTAINER.start();
  }

  /** MockWebServer needs to be static, so we can inject its port into the Spring context. */
  private static MockWebServer mockMetis;

  private static MockWebServer mockWikidata;
  private static MockWebServer mockSearchAndRecord;

  @Autowired protected EntityRecordService entityRecordService;

  @BeforeAll
  public static void setupAll() throws IOException {
    mockMetis = new MockWebServer();
    mockMetis.setDispatcher(setupMetisDispatcher());
    mockMetis.start();

    mockWikidata = new MockWebServer();
    mockWikidata.setDispatcher(setupWikidataDispatcher());
    mockWikidata.start();

    mockSearchAndRecord = new MockWebServer();
    mockSearchAndRecord.setDispatcher(setupSearchAndRecordDispatcher());
    mockSearchAndRecord.start();
  }

  @AfterAll
  public static void teardownAll() throws IOException {
    logger.info(
        "Shutdown metis server : host = {}; port={}", mockMetis.getHostName(), mockMetis.getPort());
    mockMetis.shutdown();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("mongo.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    registry.add("mongo.em.database", MONGO_CONTAINER::getEntityDb);
    registry.add("mongo.batch.database", MONGO_CONTAINER::getBatchDb);
    // enrichment database on the same test Mongo instance
    registry.add("mongo.enrichment.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
    registry.add("mongo.enrichment.database", MONGO_CONTAINER::getEnrichmentDb);
    registry.add(
        "metis.baseUrl",
        () -> String.format("http://%s:%s", mockMetis.getHostName(), mockMetis.getPort()));
    registry.add(
        "wikidata.baseUrl",
        () -> String.format("http://%s:%s", mockWikidata.getHostName(), mockWikidata.getPort()));
    registry.add(
        "europeana.searchapi.urlPrefix",
        () ->
            String.format(
                "http://%s:%s?wskey=api2demo",
                mockSearchAndRecord.getHostName(), mockSearchAndRecord.getPort()));

    registry.add("batch.computeMetrics", () -> "false");
    // Do not run scheduled entity updates in tests
    registry.add("batch.scheduling.enabled", () -> "false");
    registry.add("auth.read.enabled", () -> "false");
    registry.add("auth.write.enabled", () -> "false");
    registry.add("entitymanagement.solr.indexing.url", SOLR_CONTAINER::getConnectionUrl);
    // enable explicit commits while indexing to Solr in tests
    registry.add("entitymanagement.solr.indexing.explicitCommits", () -> true);
    // override setting in .properties file in case this is enabled
    registry.add("metis.proxy.enabled", () -> false);
    
    //overwrite default zoho properties
    registry.add("zoho.base.url", () -> TestConfig.MOCK_ZOHO_BASE_URL);
    //tests must not register organizations as this is updating the zoho
    //generate Europeana ID can be set to true when using the mock service, see TextConfig class 
    registry.add("zoho.generate.organization.europeanaid", () -> true);
    registry.add("zoho.country.mapping.file", () -> "/zoho_country_mapping_test.json");
    
    // could be used to fix eclipse issues
    registry.add("scmBranch", () -> "dev");
    registry.add("buildNumber", () -> "99");
    registry.add("timestamp", () -> System.currentTimeMillis());

    logger.info("MONGO_CONTAINER : {}", MONGO_CONTAINER.getConnectionUrl());
    logger.info("SOLR_CONTAINER : {}", SOLR_CONTAINER.getConnectionUrl());
    logger.info("METIS SERVER : host = {}; port={}", mockMetis.getHostName(), mockMetis.getPort());
  }

  private static Dispatcher setupMetisDispatcher() {
    return new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest request) throws InterruptedException {
        String externalId = Objects.requireNonNull(request.getRequestUrl()).queryParameter("uri");
        try {
          String responseBody =
              IntegrationTestUtils.loadFile(
                  IntegrationTestUtils.METIS_RESPONSE_MAP.getOrDefault(
                      externalId, IntegrationTestUtils.EMPTY_METIS_RESPONSE));
          return new MockResponse().setResponseCode(200).setBody(responseBody);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  /*
   * Currently this method is used for both the enrichment count and the isShownBy generation
   * services, since the same search and record base url property is used for both
   * (europeana.searchapi.urlPrefix).
   */
  private static Dispatcher setupSearchAndRecordDispatcher() {
    return new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest request) {
        String queryParam = Objects.requireNonNull(request.getRequestUrl()).queryParameter("query");
        try {
          /*
           * The full response is only returned for the TimeSpan type, and for the others the empty response.
           * The reason is the same generation of the isShownBy field for all entity types. If that changes in
           * the future, the mock needs to be updated.
           */
          // NOTE: the response returned is the one used for isShownBy
          String responseBody =
              queryParam.contains(EntityTypes.TimeSpan.getEntityType().toLowerCase())
                  ? IntegrationTestUtils.loadFile(
                      IntegrationTestUtils.TIMESPAN_1_CENTURY_SEARCH_AND_RECORD_JSON)
                  : IntegrationTestUtils.loadFile(
                      IntegrationTestUtils.SEARCH_AND_RECORD_EMPTY_JSON);
          return new MockResponse().setResponseCode(200).setBody(responseBody);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static Dispatcher setupWikidataDispatcher() {
    return new Dispatcher() {

      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest request) {
        try {
          if (IntegrationTestUtils.ORGANIZATION_NATURALIS_URI_WIKIDATA_PATH_SUFFIX.equals(
              request.getPath())) {
            String responseBody =
                IntegrationTestUtils.loadFile(
                    IntegrationTestUtils.ORGANIZATION_NATURALIS_WIKIDATA_RESPONSE_XML);
            return new MockResponse().setResponseCode(200).setBody(responseBody);
          } else if (IntegrationTestUtils.ORGANIZATION_GFM_WIKIDATA_URI_PATH_SUFFIX.equals(
                  request.getPath())
              || IntegrationTestUtils.ORGANIZATION_GFM_OLD_WIKIDATA_URI_PATH_SUFFIX.equals(
                  request.getPath())) {
            String responseBody =
                IntegrationTestUtils.loadFile(
                    IntegrationTestUtils.ORGANIZATION_GFM_WIKIDATA_RESPONSE_XML);
            return new MockResponse().setResponseCode(200).setBody(responseBody);
          } else if (IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_WIKIDATA_URI_PATH_SUFFIX
              .equals(request.getPath())) {
            String responseBody =
                IntegrationTestUtils.loadFile(
                    IntegrationTestUtils.ORGANIZATION_BERGER_MUSEUM_WIKIDATA_RESPONSE_XML);
            return new MockResponse().setResponseCode(200).setBody(responseBody);
          } else if (IntegrationTestUtils.ORGANIZATION_BNF_WIKIDATA_URI_PATH_SUFFIX.equals(
              request.getPath())) {
            String responseBody =
                IntegrationTestUtils.loadFile(
                    IntegrationTestUtils.ORGANIZATION_BNF_WIKIDATA_RESPONSE_XML);
            return new MockResponse().setResponseCode(200).setBody(responseBody);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        // for now, only one request is mocked in tests
        return new MockResponse().setResponseCode(404);
      }
    };
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
            europeanaProxyEntity, xmlBaseEntity.toEntityModel(), dataSource, null);

    // trigger update to generate consolidated entity
    entityUpdateService.runSynchronousUpdate(savedRecord.getEntityId());

    // return entityRecord version with consolidated entity
    return entityRecordService.retrieveByEntityId(savedRecord.getEntityId()).orElseThrow();
  }

  public static String loadFile(String resourcePath) throws IOException {
    InputStream is = AbstractIntegrationTest.class.getResourceAsStream(resourcePath);
    assert is != null;
    return IOUtils.toString(is, StandardCharsets.UTF_8).replace("\n", "");
  }

  public Optional<EntityRecord> retrieveEntity(String entityId) {
    return entityRecordService.retrieveByEntityId(entityId);
  }
}
