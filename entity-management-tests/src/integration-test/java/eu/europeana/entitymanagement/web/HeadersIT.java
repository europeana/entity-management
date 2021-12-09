package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML;
import static eu.europeana.api.commons.web.http.HttpHeaders.VALUE_LDP_RESOURCE;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Standalone test for checking behaviour of headers */
@SpringBootTest
@AutoConfigureMockMvc
public class HeadersIT extends BaseWebControllerTest {

  @Test
  void registrationShouldReturnCorrectHeaders() throws Exception {
    ResultActions results =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));

    checkAllowHeaderForPOST(results);
    checkCommonResponseHeaders(results, false);
  }

  @Test
  void registrationCorsShouldReturnCorrectHeaders() throws Exception {
    ResultActions results =
        mockMvc.perform(
            MockMvcRequestBuilders.post(IntegrationTestUtils.BASE_SERVICE_URL)
                .content(loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON))
                // CORS requests include the Origin header
                .header("Origin", "http://test-origin.eu")
                .contentType(MediaType.APPLICATION_JSON_VALUE));

    checkAllowHeaderForPOST(results);
    checkCommonResponseHeaders(results, false);
    checkCorsHeaders(results, false);
  }

  @Test
  void retrievalWithJsonldExtensionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld"));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results, true);
  }

  @Test
  void retrievalWithJsonldExtensionCorsShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
                // CORS requests include the Origin header
                .header("Origin", "http://test-origin.eu"));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results, true);
    checkCorsHeaders(results, true);
  }

  @Test
  void retrievalWithXmlExtensionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc
            .perform(
                get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath + ".xml")
                    .accept(CONTENT_TYPE_APPLICATION_RDF_XML))
            .andExpect(
                header().string(HttpHeaders.CONTENT_TYPE, is(CONTENT_TYPE_APPLICATION_RDF_XML)))
            .andExpect(header().exists(HttpHeaders.ETAG))
            .andExpect(header().string(HttpHeaders.LINK, is(VALUE_LDP_RESOURCE)));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results, true, true);
  }

  @Test
  void retrievalWithAcceptHeaderShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON));

    checkAllowHeaderForDPGP(results);
    checkCommonResponseHeaders(results, false);
  }

  @Test
  void retrievalWithPathExtensionSchemaOrg() throws Exception {
    String requestPath = createEntity() + ".schema.jsonld?wskey=test";
    ResultActions results =
        mockMvc
            .perform(
                get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                    .header("Origin", "http://test-origin.eu"))
            .andExpect(status().isOk());

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results, true);
    checkCorsHeaders(results, true);
  }

  @Test
  void updateFromExternalDatasourceShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions resultActions =
        mockMvc
            .perform(
                MockMvcRequestBuilders.put(
                        IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                    .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                    .content(loadFile(IntegrationTestUtils.CONCEPT_UPDATE_BATHTUB_JSON))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());

    checkAllowHeaderForPOST(resultActions);
    checkCommonResponseHeaders(resultActions, false);
  }

  @Test
  void updateEntityPUTShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    ResultActions resultActions =
        mockMvc
            .perform(
                MockMvcRequestBuilders.put(
                        IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                    .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                    .content(loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_EMPTY_UPDATE_JSON))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    checkAllowHeaderForDPGP(resultActions);
    checkCommonResponseHeaders(resultActions, false);
  }

  @Test
  void permanentDeletionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    mockMvc
        .perform(
            delete(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + IntegrationTestUtils.BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(
            header()
                .stringValues(
                    HttpHeaders.VARY,
                    hasItems(
                        containsString(HttpHeaders.ORIGIN),
                        containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                        containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS))))
        .andExpect(
            header()
                .stringValues(
                    HttpHeaders.ALLOW, hasItems(containsString("DELETE"), containsString("POST"))));
  }

  @Test
  void deprecationShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    mockMvc
        .perform(
            delete(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(
            header()
                .stringValues(
                    HttpHeaders.VARY,
                    hasItems(
                        containsString(HttpHeaders.ORIGIN),
                        containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                        containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS))))
        .andExpect(
            header()
                .stringValues(
                    HttpHeaders.ALLOW, hasItems(containsString("DELETE"), containsString("POST"))));
  }

  @Test
  void reEnablingDeprecatedEntityShouldReturnCorrectHeaders() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    ResultActions resultActions =
        mockMvc
            .perform(
                post(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    checkCommonResponseHeaders(resultActions, false);
    checkAllowHeaderForDPGP(resultActions);
  }

  @Test
  void retrievalWithPathExtensionShouldIgnoreAcceptHeader() throws Exception {
    String requestPath = createEntity() + ".schema.jsonld?wskey=test";
    ResultActions results =
        mockMvc
            .perform(
                get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath)
                    .accept("application/xml") // accept header should be ignored
                    .header("Origin", "http://test-origin.eu"))
            .andExpect(status().isOk());

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results, true);
    checkCorsHeaders(results, true);
  }

  @Test
  void retrievalWithWrongAcceptShouldReturn406() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath).accept("web/vtt1"));

    results.andExpect(status().isNotAcceptable());
  }

  @Test
  void retrievalWithUnsupportedFormatShouldReturn406() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
            get(IntegrationTestUtils.BASE_SERVICE_URL + "/" + requestPath).accept("plain/text"));

    results.andExpect(status().isNotAcceptable());
  }

  private String createEntity() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(IntegrationTestUtils.CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, IntegrationTestUtils.CONCEPT_BATHTUB_URI)
            .getEntityId();

    return getEntityRequestPath(entityId);
  }
}
