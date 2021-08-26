package eu.europeana.entitymanagement.web;

import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_APPLICATION_RDF_XML;
import static eu.europeana.api.commons.web.http.HttpHeaders.VALUE_LDP_RESOURCE;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_ADMIN_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_EMPTY_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_BATHTUB_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Standalone test to check for response headers
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ResponseHeadersIT extends BaseWebControllerTest {

  @Test
  void registrationShouldReturnCorrectHeaders() throws Exception {
    ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
        .content(loadFile(CONCEPT_REGISTER_BATHTUB_JSON))
        .contentType(MediaType.APPLICATION_JSON_VALUE));

    checkAllowHeaderForPOST(results);
    checkCommonResponseHeaders(results);
  }

  @Test
  void registrationCorsShouldReturnCorrectHeaders() throws Exception {
    ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
        .content(loadFile(CONCEPT_REGISTER_BATHTUB_JSON))
        // CORS requests include the Origin header
        .header("Origin", "http://test-origin.eu")
        .contentType(MediaType.APPLICATION_JSON_VALUE));

    checkAllowHeaderForPOST(results);
    checkCommonResponseHeaders(results);
    checkCorsHeaders(results);
  }

  @Test
  void retrievalWithJsonldExtensionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results = mockMvc.perform(
        get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld"));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results);
  }


  @Test
  void retrievalWithJsonldExtensionCorsShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results = mockMvc.perform(
        get(BASE_SERVICE_URL + "/" + requestPath + ".jsonld")
            // CORS requests include the Origin header
            .header("Origin", "http://test-origin.eu"));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results);
    checkCorsHeaders(results);
  }

  @Test
  void retrievalWithXmlExtensionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results = mockMvc.perform(
            get(BASE_SERVICE_URL + "/" + requestPath + ".xml")
                .accept(CONTENT_TYPE_APPLICATION_RDF_XML))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
            is(CONTENT_TYPE_APPLICATION_RDF_XML)))
        .andExpect(header().exists(HttpHeaders.ETAG))
        .andExpect(header().string(HttpHeaders.LINK, is(VALUE_LDP_RESOURCE)))
        .andExpect(
            header().stringValues(HttpHeaders.VARY, hasItems(containsString(HttpHeaders.ACCEPT))));

    checkAllowHeaderForGET(results);
  }

  @Test
  void retrievalWithAcceptHeaderShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions results = mockMvc.perform(
        get(BASE_SERVICE_URL + "/" + requestPath)
            .accept(MediaType.APPLICATION_JSON));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeaders(results);
  }

  @Test
  void updateFromExternalDatasourceShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();
    ResultActions resultActions = mockMvc.perform(
        MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
            .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
            .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
            .contentType(MediaType.APPLICATION_JSON));

    checkAllowHeaderForPOST(resultActions);
    checkCommonResponseHeaders(resultActions);
  }

  @Test
  void updateEntityPUTShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    ResultActions resultActions = mockMvc.perform(
        MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
            .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
            .content(loadFile(CONCEPT_BATHTUB_EMPTY_UPDATE_JSON))
            .contentType(MediaType.APPLICATION_JSON));
    checkAllowHeaderForDPGP(resultActions);
    checkCommonResponseHeaders(resultActions);
  }

  @Test
  void permanentDeletionShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    mockMvc.perform(
            delete(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(
            header().stringValues(HttpHeaders.VARY,
                hasItems(
                    containsString(HttpHeaders.ORIGIN),
                    containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                    containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS))))
        .andExpect(header().stringValues(
            HttpHeaders.ALLOW, hasItems(
                containsString("DELETE"),
                containsString("POST")
            )));
  }

  @Test
  void deprecationShouldReturnCorrectHeaders() throws Exception {
    String requestPath = createEntity();

    mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(
            header().stringValues(HttpHeaders.VARY,
                hasItems(
                    containsString(HttpHeaders.ORIGIN),
                    containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD),
                    containsString(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS))))
        .andExpect(header().stringValues(
            HttpHeaders.ALLOW, hasItems(
                containsString("DELETE"),
                containsString("POST")
            )));
  }


  @Test
  void reEnablingDeprecatedEntityShouldReturnCorrectHeaders() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
    deprecateEntity(entityRecord);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    ResultActions resultActions = mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath)
            .accept(MediaType.APPLICATION_JSON));
    checkCommonResponseHeaders(resultActions);
    checkAllowHeaderForDPGP(resultActions);
  }


  private String createEntity() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    String entityId = createEntity(europeanaMetadata, metisResponse,
        CONCEPT_BATHTUB_URI).getEntityId();

    return getEntityRequestPath(entityId);
  }
}
