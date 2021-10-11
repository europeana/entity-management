package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityChangeProvenanceIT extends BaseWebControllerTest {

  @Test
  public void changeProvenanceShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
        createEntity(europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);

    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // request internal profile so proxies are included in response
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(
                    BASE_SERVICE_URL + "/" + requestPath + "/management/source?profile=internal")
                // change url to wikidata
                .param(WebEntityConstants.PATH_PARAM_URL, AGENT_JAN_VERMEER_WIKIDATA_URI)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        // external proxyId should have changed to wikidata. expects external proxy to come second!
        .andExpect(jsonPath("$.proxies[1].id", containsString(AGENT_JAN_VERMEER_WIKIDATA_URI)));
  }
}
