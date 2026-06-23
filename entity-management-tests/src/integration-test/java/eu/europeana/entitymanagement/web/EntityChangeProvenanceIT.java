package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.IntegrationTestUtils.*;
import static eu.europeana.entitymanagement.utils.EntityRecordUtils.getEntityRequestPath;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.entitymanagement.common.config.DataSource;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityProxy;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.testutils.IntegrationTestUtils;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.validation.constraints.AssertTrue;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityChangeProvenanceIT extends BaseWebControllerTest {

  @Test
  public void changeProvenanceSkosMismatch() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
            createEntity(
                    europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);

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
  public void changeProvenanceForExistingSameAs() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
            createEntity(
                    europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);

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
  public void changeProvenanceShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
        createEntity(
            europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);

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
  public void changeProvenanceForMultipleProxyShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(IntegrationTestUtils.AGENT_REGISTER_JAN_VERMEER);
    String metisResponse = loadFile(IntegrationTestUtils.AGENT_JAN_VERMEER_XML_VIAF);

    EntityRecord savedRecord =
            createEntity(
                    europeanaMetadata, metisResponse, AGENT_JAN_VERMEER_VIAF_URI);


    // assert content of default External proxy
    EntityProxy externalProxy = savedRecord.getExternalProxies().get(0);

    Assertions.assertEquals(
            AGENT_JAN_VERMEER_VIAF_URI, externalProxy.getProxyId());
    String requestPath = getEntityRequestPath(savedRecord.getEntityId());

    // pass wikidata url and existing one , country - EU_PUBLICATIONS_COUNTRY_AGO
    List<String> urls = Arrays.asList(AGENT_JAN_VERMEER_WIKIDATA_URI, AGENT_JAN_VERMEER_VIAF_URI, EU_PUBLICATIONS_COUNTRY_AGO);

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

    List<EntityProxy> proxies = entity.getExternalProxies();
    Assertions.assertEquals(3, proxies.size());
    // check that the order of the proxies is preserved
    Assertions.assertEquals(AGENT_JAN_VERMEER_WIKIDATA_URI, proxies.get(0).getProxyId());
    Assertions.assertEquals(AGENT_JAN_VERMEER_VIAF_URI, proxies.get(1).getProxyId());
    Assertions.assertEquals(EU_PUBLICATIONS_COUNTRY_AGO, proxies.get(2).getProxyId());

    // check proxyIn.id (aggregation id )
    System.out.println(entity.getEntity().getIsAggregatedBy().getAggregates());

    Assertions.assertEquals(entity.getEntityId()+ "#aggr_source_1", proxies.get(0).getProxyIn().getId());
    Assertions.assertEquals(entity.getEntityId()+ "#aggr_source_2", proxies.get(1).getProxyIn().getId());
    Assertions.assertEquals(entity.getEntityId()+ "#aggr_source_3", proxies.get(2).getProxyIn().getId());

    // check aggregates list
    Assertions.assertEquals(4,entity.getEntity().getIsAggregatedBy().getAggregates().size());
    Assertions.assertTrue(entity.getEntity().getIsAggregatedBy().getAggregates().contains(proxies.get(0).getProxyIn().getId()));
    Assertions.assertTrue(entity.getEntity().getIsAggregatedBy().getAggregates().contains(proxies.get(1).getProxyIn().getId()));
    Assertions.assertTrue(entity.getEntity().getIsAggregatedBy().getAggregates().contains(proxies.get(2).getProxyIn().getId()));

  }
}
