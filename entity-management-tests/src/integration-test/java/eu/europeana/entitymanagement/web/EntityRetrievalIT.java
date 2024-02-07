package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.PARAM_PROFILE_SYNC;
import static eu.europeana.entitymanagement.vocabulary.WebEntityConstants.QUERY_PARAM_PROFILE;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import java.util.Map;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.zoho.crm.api.record.Record;
import eu.europeana.entitymanagement.batch.service.FailedTaskService;
import eu.europeana.entitymanagement.definitions.batch.model.ScheduledUpdateType;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.FailedTaskJsonFields;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import eu.europeana.entitymanagement.vocabulary.WebEntityFields;
import eu.europeana.entitymanagement.web.xml.model.XmlConstants;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityRetrievalIT extends BaseWebControllerTest {

  private static final String BNF_LOGO =
      "http://commons.wikimedia.org/wiki/Special:FilePath/Logo_BnF.svg";
  private static final String BNF_LOGO_SOURCE =
      "http://commons.wikimedia.org/wiki/File:Logo_BnF.svg";

  @Autowired private FailedTaskService failedTaskService;

  Map<String, String> xmlNamespaces =
      Map.of(
          "edm", XmlConstants.NAMESPACE_EDM,
          "rdf", XmlConstants.NAMESPACE_RDF,
          "skos", XmlConstants.NAMESPACE_SKOS,
          "foaf", XmlConstants.NAMESPACE_FOAF,
          "dc", XmlConstants.NAMESPACE_DC);

  @Test
  public void shouldRetrieveEntitiesWithFailures() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);

    // create failed task for entity
    Exception testException = new Exception("TestMessage");
    failedTaskService.dropCollection();
    failedTaskService.persistFailure(
        entityRecord.getEntityId(), ScheduledUpdateType.FULL_UPDATE, testException);

    // MockMvc requests use "localhost" without a port
    String clickableUrl =
        "http://localhost/entity/"
            + getEntityRequestPath(entityRecord.getEntityId())
            + ".jsonld?profile=debug,internal&wskey=testKey";

    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/management/failed")
                .accept(MediaType.APPLICATION_JSON)
                .param(WebEntityConstants.QUERY_PARAM_WSKEY, "testKey"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$", containsInAnyOrder(clickableUrl)));
  }

  @Test
  void retrieveWithInvalidProfileShouldReturn400() throws Exception {
    /*
     * check the error if the profile parameter is wrong
     */

    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "wrong-profile-parameter")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void retrieveNonExistingEntityShouldReturn404() throws Exception {
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier.jsonld")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void retrievalErrorWithDebugProfileShouldIncludeStacktrace() throws Exception {
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL
                    + "/"
                    + "wrong-type/wrong-identifier.jsonld?profile=debug")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.trace").exists());
  }

  @Test
  void retrieveDeprecatedEntityShouldReturn410() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }

  @Test
  void retrieveConceptJsonExternalShouldBeSuccessful() throws Exception {

    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)));
  }

  @Test
  void retrieveConceptXmlExternalShouldBeSuccessful() throws Exception {

    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    String entityBaseXpath = "/rdf:RDF/skos:Concept";
    ResultActions resultActions =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML));
    resultActions
        .andExpect(status().isOk())
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)));
  }

  @Test
  void retrievalWithDebugProfileShouldIncludeFailedTask() throws Exception {

    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    // create FailedTask for entityId
    Exception testException = new Exception("TestMessage");
    failedTaskService.persistFailure(entityId, ScheduledUpdateType.FULL_UPDATE, testException);

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal,debug")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isAggregatedBy." + WebEntityFields.FAILURES).exists())
        .andExpect(
            jsonPath(
                "$.isAggregatedBy." + WebEntityFields.FAILURES + "." + FailedTaskJsonFields.TYPE,
                is(ScheduledUpdateType.FULL_UPDATE.getValue())))
        .andExpect(
            jsonPath(
                    "$.isAggregatedBy."
                        + WebEntityFields.FAILURES
                        + "."
                        + FailedTaskJsonFields.MESSAGE)
                .isNotEmpty())
        .andExpect(
            jsonPath(
                    "$.isAggregatedBy."
                        + WebEntityFields.FAILURES
                        + "."
                        + FailedTaskJsonFields.FIRST_TIME)
                .isNotEmpty())
        .andExpect(
            jsonPath(
                    "$.isAggregatedBy."
                        + WebEntityFields.FAILURES
                        + "."
                        + FailedTaskJsonFields.LAST_TIME)
                .isNotEmpty())
        .andExpect(
            jsonPath(
                    "$.isAggregatedBy."
                        + WebEntityFields.FAILURES
                        + "."
                        + FailedTaskJsonFields.STACKTRACE)
                .isNotEmpty());
  }

  @Test
  void retrieveConceptJsonExternalWithLanguageFilteringShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .param(WebEntityConstants.QUERY_PARAM_LANGUAGE, "en,de")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.getEntityType())))
        .andExpect(jsonPath("$.prefLabel[*]", hasSize(2)))
        .andExpect(jsonPath("$.altLabel[*]", hasSize(2)));
  }

  @Test
  public void retrieveAgentJsonExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.getEntityType())));
  }

  @Test
  public void retrieveAgentDeprecatedShouldBeRedirected() throws Exception {
    String europeanaMetadataBirch = loadFile(IntegrationTestUtils.AGENT_REGISTER_BIRCH_REDIRECTION_JSON);
    String metisResponseBirch = loadFile(IntegrationTestUtils.AGENT_BIRCH_XML);
    String entityIdBirch =
        createEntity(europeanaMetadataBirch, metisResponseBirch, IntegrationTestUtils.AGENT_BIRCH_URI)
            .getEntityId();
    
    String europeanaMetadataRaphael = loadFile(IntegrationTestUtils.AGENT_REGISTER_RAPHAEL_JSON);
    String metisResponseRaphael = loadFile(IntegrationTestUtils.AGENT_RAPHAEL_XML);
    String entityIdRaphael =
        createEntity(europeanaMetadataRaphael, metisResponseRaphael, IntegrationTestUtils.AGENT_RAPHAEL_URI)
            .getEntityId();

    String requestPathBirch = getEntityRequestPath(entityIdBirch);
    // sync deprecation
    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPathBirch)
                .param(QUERY_PARAM_PROFILE, PARAM_PROFILE_SYNC)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // retrieve
    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPathBirch + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON));
    result.andExpect(status().isMovedPermanently());
    result.andExpect(header().string("Location", entityIdRaphael));
  }

  @Test
  public void retrieveAgentNotFoundShouldBeRedirected() throws Exception {
    String europeanaMetadataBirch = loadFile(IntegrationTestUtils.AGENT_REGISTER_BIRCH_REDIRECTION_JSON);
    String metisResponseBirch = loadFile(IntegrationTestUtils.AGENT_BIRCH_XML);
    String entityIdBirch =
        createEntity(europeanaMetadataBirch, metisResponseBirch, IntegrationTestUtils.AGENT_BIRCH_URI)
            .getEntityId();
    
    String europeanaMetadataRaphael = loadFile(IntegrationTestUtils.AGENT_REGISTER_RAPHAEL_JSON);
    String metisResponseRaphael = loadFile(IntegrationTestUtils.AGENT_RAPHAEL_XML);
    String entityIdRaphael =
        createEntity(europeanaMetadataRaphael, metisResponseRaphael, IntegrationTestUtils.AGENT_RAPHAEL_URI)
            .getEntityId();
    
    deleteEntity(entityIdRaphael);

    String requestPathRaphael = getEntityRequestPath(entityIdRaphael);
    // retrieve
    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPathRaphael + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON));
    result.andExpect(status().isMovedPermanently());
    result.andExpect(header().string("Location", entityIdBirch));
  }

  @Test
  public void retrieveAgentXmlExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    String entityBaseXpath = "/rdf:RDF/edm:Agent";

    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML));
    result
        .andExpect(status().isOk())
        .andExpect(
            xpath(entityBaseXpath + "/edm:isShownBy/edm:WebResource/@rdf:about", xmlNamespaces)
                .nodeCount(greaterThan(0)))
        .andExpect(
            xpath(
                    entityBaseXpath + "/edm:isShownBy/edm:WebResource/dc:source/@rdf:resource",
                    xmlNamespaces)
                .nodeCount(greaterThan(0)))
        .andExpect(
            xpath(
                    entityBaseXpath + "/edm:isShownBy/edm:WebResource/foaf:thumbnail/@rdf:resource",
                    xmlNamespaces)
                .nodeCount(greaterThan(0)))
        .andExpect(
            xpath(entityBaseXpath + "/foaf:depiction/edm:WebResource/@rdf:about", xmlNamespaces)
                .nodeCount(greaterThan(0)))
        .andExpect(
            xpath(
                    entityBaseXpath + "/foaf:depiction/edm:WebResource/dc:source/@rdf:resource",
                    xmlNamespaces)
                .nodeCount(greaterThan(0)))
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)));
  }

  @Test
  void retrieveConceptJsonExternalSchemaOrgShouldBeSuccessful() throws Exception {

    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.@id", is(entityId)))
        .andExpect(jsonPath("$.name").isNotEmpty())
        .andExpect(jsonPath("$.description").isNotEmpty())
        .andExpect(jsonPath("$.alternateName").isNotEmpty());
  }

  @Test
  public void retrieveAgentExternalSchemaOrgShouldBeSuccessful() throws Exception {

    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_DA_VINCI_XML);
    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.AGENT_DA_VINCI_URI);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());
    ResultActions resultActions =
        mockMvc
            .perform(
                get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                    .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.@id", is(entityRecord.getEntityId())));

    for (String sameAsElem : entityRecord.getEntity().getSameReferenceLinks()) {
      resultActions.andExpect(jsonPath("$.sameAs", Matchers.hasItem(sameAsElem)));
    }
    resultActions.andExpect(jsonPath("$.gender").isNotEmpty());
    resultActions.andExpect(jsonPath("$.deathDate").isNotEmpty());
    resultActions.andExpect(jsonPath("$.birthDate").isNotEmpty());
    resultActions.andExpect(jsonPath("$.name").isNotEmpty());
    resultActions.andExpect(jsonPath("$.alternateName").isNotEmpty());
  }

  @Test
  public void retrieveOrganizationExternalSchemaOrgShouldBeSuccessful() throws Exception {
    // id in JSON matches ORGANIZATION_BNF_URI_ZOHO value
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";

    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();
    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.@id", is(entityId)))
        .andExpect(jsonPath("$.sameAs").isNotEmpty());
  }

  @Test
  public void retrievePlaceExternalSchemaOrgShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.PLACE_REGISTER_PARIS_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.PLACE_PARIS_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.PLACE_PARIS_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".schema.jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.@id", is(entityId)))
        .andExpect(jsonPath("$.geo").isNotEmpty())
        .andExpect(jsonPath("$.name").isNotEmpty())
        .andExpect(jsonPath("$.alternateName").isNotEmpty())
        .andExpect(jsonPath("$.sameAs").isNotEmpty());
  }

  @Test
  public void retrieveOrganizationJsonExternalWithCountryAndRoleDereference() throws Exception {
    //1. create a place "Sweden" to be used to dereference zoho country for the zoho GFM org
    String europeanaMetadata = loadFile(IntegrationTestUtils.PLACE_REGISTER_SWEDEN_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.PLACE_SWEDEN_XML);
    createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.PLACE_SWEDEN_URI);
    
//    //forcefully change the country mapping uri to the right one
//    List<CountryMapping> countryMap= entityRecordService.getCountryMapping();
//    for(CountryMapping cm : countryMap) {
//      if(cm.getZohoLabel().equals("Sweden, SE")) {
//        cm.setEntityUri("http://data.europeana.eu/place/1");
//      }
//    }

    //2. register zoho GFM org
    europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    ResultActions resultActions = mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external, dereference")
                .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.getEntityType())))
        .andExpect(jsonPath("$.sameAs").isNotEmpty())
        .andExpect(jsonPath("$.country.prefLabel.en", is("Sweden")))
        .andExpect(jsonPath("$.europeanaRole[0].prefLabel.en", is("Providing Institution")));
  }

  @Test
  public void retrieveOrganizationJsonExternal() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.getEntityType())))
        .andExpect(jsonPath("$.sameAs").isNotEmpty())
        .andExpect(jsonPath("$.hasAddress.countryName").isNotEmpty())
        .andExpect(jsonPath("$.countryPlace").doesNotExist());
  }

  @Test
  public void retrieveOrganizationJsonBNFShouldBeSuccessful() throws Exception {
    // id in JSON matches ORGANIZATION_BNF_URI_ZOHO value
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_BNF_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_BNF_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Organization.getEntityType())))
        .andExpect(jsonPath("$.logo.id", is(BNF_LOGO)))
        .andExpect(jsonPath("$.sameAs").isNotEmpty());
  }

  @Test
  public void retrieveOrganizationXmlExternalShouldBeSuccessful() throws Exception {
    // id in JSON matches ORGANIZATION_GFM_URI_ZOHO value
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_GFM_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_GFM_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    String entityBaseXpath = "/rdf:RDF/edm:Organization";

    String requestPath = getEntityRequestPath(entityId);
    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(
                    MediaType.APPLICATION_JSON)); // deliberately test the path extension precedence
    result
        .andExpect(status().isOk())
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)))
        .andExpect(xpath(entityBaseXpath + "/edm:countryPlace", xmlNamespaces).doesNotExist());
  }

  @Test
  public void retrieveOrganizationXmlBNFShouldBeSuccessful() throws Exception {
    // id in JSON matches ORGANIZATION_BNF_URI_ZOHO value
    String europeanaMetadata = loadFile(IntegrationTestUtils.ORGANIZATION_REGISTER_BNF_ZOHO_JSON);
    Optional<Record> zohoRecord =
        IntegrationTestUtils.getZohoOrganizationRecord(
            IntegrationTestUtils.ORGANIZATION_BNF_URI_ZOHO);

    assert zohoRecord.isPresent() : "Mocked Zoho response not loaded";
    String entityId = createOrganization(europeanaMetadata, zohoRecord.get()).getEntityId();

    String entityBaseXpath = "/rdf:RDF/edm:Organization";

    String requestPath = getEntityRequestPath(entityId);
    ResultActions result =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(
                    MediaType.APPLICATION_JSON)); // deliberately test the path extension precedence
    result
        .andExpect(status().isOk())
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/foaf:logo/edm:WebResource/@rdf:about", xmlNamespaces)
                .string(BNF_LOGO))
        .andExpect(
            xpath(
                    entityBaseXpath + "/foaf:logo/edm:WebResource/dc:source/@rdf:resource",
                    xmlNamespaces)
                .string(BNF_LOGO_SOURCE))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)));
  }

  @Test
  public void retrievePlaceJsonExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.PLACE_REGISTER_PARIS_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.PLACE_PARIS_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.PLACE_PARIS_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Place.getEntityType())));
  }

  @Test
  public void retrievePlaceXmlExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.PLACE_REGISTER_PARIS_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.PLACE_PARIS_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.PLACE_PARIS_URI)
            .getEntityId();
    String requestPath = getEntityRequestPath(entityId);

    String entityBaseXpath = "/rdf:RDF/edm:Place";
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)));
  }

  @Test
  public void retrieveTimespanJsonExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.TIMESPAN_1ST_CENTURY_XML);

    String entityId =
        createEntity(
                europeanaMetadata, metisResponse, IntegrationTestUtils.TIMESPAN_1ST_CENTURY_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.TimeSpan.getEntityType())));
  }

  @Test
  public void retrieveTimespanXmlExternalShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.TIMESPAN_1ST_CENTURY_XML);

    String entityId =
        createEntity(
                europeanaMetadata, metisResponse, IntegrationTestUtils.TIMESPAN_1ST_CENTURY_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    String entityBaseXpath = "/rdf:RDF/edm:TimeSpan";

    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(xpath(entityBaseXpath + "/@rdf:about", xmlNamespaces).string(entityId))
        .andExpect(
            xpath(entityBaseXpath + "/skos:prefLabel", xmlNamespaces).nodeCount(greaterThan(0)));
  }

  @Test
  public void retrieveEntityInternalJsonShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    String requestPath = getEntityRequestPath(entityId);
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(entityId)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.getEntityType())))
        .andExpect(jsonPath("$.proxies", hasSize(2)));
  }

  @Test
  public void proxiesShouldNotHaveMetrics() throws Exception {

    ResultActions results =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                    .content(loadFile(IntegrationTestUtils.AGENT_REGISTER_DAVINCI_JSON))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .param(WebEntityConstants.QUERY_PARAM_PROFILE, "internal"))
            .andExpect(status().isAccepted());

    results
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.type", is(EntityTypes.Agent.getEntityType())))
        .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
        .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
        // should have Europeana and Datasource proxies
        .andExpect(jsonPath("$.proxies", hasSize(2)));

    results
        .andExpect(jsonPath("$.proxies[0].proxyIn").exists())
        .andExpect(jsonPath("$.proxies[0].proxyIn.pageRank").doesNotExist())
        .andExpect(jsonPath("$.proxies[0].proxyIn.recordCount").doesNotExist())
        .andExpect(jsonPath("$.proxies[0].proxyIn.score").doesNotExist());

    results
        .andExpect(jsonPath("$.proxies[1].proxyIn").exists())
        .andExpect(jsonPath("$.proxies[1].proxyIn.pageRank").doesNotExist())
        .andExpect(jsonPath("$.proxies[1].proxyIn.recordCount").doesNotExist())
        .andExpect(jsonPath("$.proxies[1].proxyIn.score").doesNotExist());
  }

  // TODO: add tests for XML retrieval

  @Test
  void retrieveNonExistingConceptScheme() throws Exception {
    mockMvc
        .perform(
            get(IntegrationTestUtils.BASE_SCHEME_URL + "wrong-identifier.jsonld")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
