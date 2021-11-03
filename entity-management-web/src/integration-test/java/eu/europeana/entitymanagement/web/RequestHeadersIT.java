package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;

/** Standalone test to check for response headers */
@SpringBootTest
@AutoConfigureMockMvc
public class RequestHeadersIT extends BaseWebControllerTest {

  
  
  @Test
  void retrievalWithPathExtensionShouldIgnoreAcceptHeader() throws Exception {
    String requestPath = createEntity() + ".schema.jsonld?wskey=test";
    ResultActions results =
        mockMvc.perform(
             get(BASE_SERVICE_URL + "/" + requestPath).accept("application/xml") //accept header should be ignored
            .header("Origin", "http://test-origin.eu"));

    checkAllowHeaderForGET(results);
    checkCommonResponseHeadersForSchemaOrg(results);
    checkCorsHeadersForSchemaOrg(results);
  }

  
  @Test
  void retrievalWithWrongAcceptShouldReturn400() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
             get(BASE_SERVICE_URL + "/" + requestPath).accept("web/vtt1"));

    results.andExpect(status().isBadRequest());
  }
  
  @Test
  void retrievalWithUnsupportedFormatShouldReturn406() throws Exception {
    String requestPath = createEntity();
    ResultActions results =
        mockMvc.perform(
             get(BASE_SERVICE_URL + "/" + requestPath).accept("plain/text"));

    results.andExpect(status().isNotAcceptable());
  }
  
 
  private String createEntity() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    String entityId =
        createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI).getEntityId();

    return getEntityRequestPath(entityId);
  }
}
