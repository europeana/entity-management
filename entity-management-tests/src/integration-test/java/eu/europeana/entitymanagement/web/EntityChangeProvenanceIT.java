package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.IntegrationTestUtils.AGENT_JAN_VERMEER_VIAF_URI;
import static eu.europeana.entitymanagement.testutils.IntegrationTestUtils.AGENT_JAN_VERMEER_WIKIDATA_URI;
import static eu.europeana.entitymanagement.testutils.IntegrationTestUtils.AGENT_JAN_VERMEER_GND_URI;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityChangeProvenanceIT extends BaseWebControllerTest {

  
  EntityRecord createRecordVerneer() throws IOException, Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
            createEntity(
                    europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);
    return savedRecord;
  }

  @Test
  void changeProvenanceBadRequest() throws Exception {
    EntityRecord savedRecord = createRecordVerneer();

    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(
            AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // pass the existing same as url and a non-existing one
    List<String> urls = Arrays.asList("http://testing_nonexisting_one",
            AGENT_JAN_VERMEER_VIAF_URI);

    mockMvc
            .perform(
                    MockMvcRequestBuilders.put(
                                    IntegrationTestUtils.BASE_SERVICE_URL
                                            + "/"
                                            + requestPath
                                            + "/management/source?profile=internal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urls))
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }


  @Test
  void changeProvenanceForExistingSameAs() throws Exception {
    EntityRecord savedRecord = createRecordVerneer();

    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(
            AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // pass the existing same as url for update - AGENT_JAN_VERMEER_VIAF_URI
    List<String> urls = Arrays.asList(AGENT_JAN_VERMEER_VIAF_URI);

    mockMvc
            .perform(
                    MockMvcRequestBuilders.put(
                                    IntegrationTestUtils.BASE_SERVICE_URL
                                            + "/"
                                            + requestPath
                                            + "/management/source?profile=internal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urls))
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            // external proxyId should NOT have changed.
            .andExpect(
                    jsonPath(
                            "$.proxies[1].id",
                            containsString(AGENT_JAN_VERMEER_VIAF_URI)));
  }


  @Test
  void changeProvenanceShouldBeSuccessful() throws Exception {
    EntityRecord savedRecord = createRecordVerneer();

    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(
        AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // pass wikidata url - AGENT_JAN_VERMEER_WIKIDATA_URI
    List<String> urls = Arrays.asList(AGENT_JAN_VERMEER_WIKIDATA_URI);

    // request internal profile so proxies are included in response
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(
                    IntegrationTestUtils.BASE_SERVICE_URL
                        + "/"
                        + requestPath
                        + "/management/source?profile=internal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(urls))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        // external proxyId should have changed to wikidata. expects external proxy to come second!
        .andExpect(
            jsonPath(
                "$.proxies[1].id",
                containsString(AGENT_JAN_VERMEER_WIKIDATA_URI)));
  }

  @Test
  void changeProvenanceForMultipleProxyShouldBeSuccessful() throws Exception {
    EntityRecord savedRecord = createRecordVerneer();


    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(
            AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // pass wikidata url and existing one , country - EU_PUBLICATIONS_COUNTRY_AGO
    List<String> urls = Arrays.asList(AGENT_JAN_VERMEER_WIKIDATA_URI, AGENT_JAN_VERMEER_VIAF_URI);

    // request internal profile so proxies are included in response
    mockMvc
            .perform(
                    MockMvcRequestBuilders.put(
                                    IntegrationTestUtils.BASE_SERVICE_URL
                                            + "/"
                                            + requestPath
                                            + "/management/source?profile=internal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urls))
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    EntityRecord entity = entityRecordService.retrieveEntityRecord("http://data.europeana.eu/agent/1", savedRecord.getEntityId(), false);

    List<EntityProxy> proxies = entity.getProxies();
    int expected_proxies = 3;
    Assertions.assertEquals(expected_proxies, proxies.size());
    // check that the order of the proxies is preserved
    Assertions.assertEquals(AGENT_JAN_VERMEER_WIKIDATA_URI, proxies.get(1).getProxyId());//first external proxy
    Assertions.assertEquals(AGENT_JAN_VERMEER_VIAF_URI, proxies.get(2).getProxyId());//second external proxy
    
    // check proxyIn.id (aggregation id )
    Assertions.assertEquals(entity.getEntityId()+ "#aggr_source_1", proxies.get(1).getProxyIn().getId());
    Assertions.assertEquals(entity.getEntityId()+ "#aggr_source_2", proxies.get(2).getProxyIn().getId());
    
    // check aggregates list
    List<String> aggregatesList = entity.getEntity().getIsAggregatedBy().getAggregates();
    Assertions.assertNotNull(aggregatesList);

    //
    Assertions.assertEquals(expected_proxies, aggregatesList.size());
    Assertions.assertTrue(aggregatesList.contains(proxies.get(0).getProxyIn().getId()));
    Assertions.assertTrue(aggregatesList.contains(proxies.get(1).getProxyIn().getId()));
    Assertions.assertTrue(aggregatesList.contains(proxies.get(2).getProxyIn().getId()));
    
    
  }
}
