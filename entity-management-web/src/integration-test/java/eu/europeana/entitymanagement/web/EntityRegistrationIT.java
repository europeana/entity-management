package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_REGISTER_STALIN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_STALIN_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_ERROR_CHECK_1_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_1_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_ERROR_CHECK_2_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_METIS_ERROR_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_REGISTER_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_XML;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BATHTUB_DEREF;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.PLACE_JSON;
//import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.BatchService;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.exception.EntityRemovedException;
import eu.europeana.entitymanagement.exception.HttpBadRequestException;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import okhttp3.mockwebserver.MockResponse;

/**
 * Integration test for the main Entity Management controller in case of errors occur 
 */

public class EntityRegistrationIT extends AbstractIntegrationTest {

    /*
     * TODO: uncomment the @Test annotation below when the consolidated entity is populated because when 
	 * it is left empty (as in the current implementation of the register API), it is not possible to find
	 * the already existing entity by looking in the "co-reference" table of the consolidated entity
     */
    //@Test
    public void registerEntityErrorCheck_entityAlreadyExists() throws Exception {
    	/*
    	 * check the error when the entity already exists
    	 *  
    	 */
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)))
                .andReturn();

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));
    }
    
    @Test
    public void registerEntityErrorCheck_entityInSameAsFromMetisExists() throws Exception {
        /*
         * check the error if the entity exists with the id in the sameAs field of the metis entity
         */
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)))
                .andReturn();

        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_ERROR_CHECK_1_XML)));

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_ERROR_CHECK_1_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isMovedPermanently());
    }
    
    @Test
    public void registerEntityErrorCheck_entityIdNotInDatasources() throws Exception {
        /*
         * check the error if the entity id is not in the datasources
         */
        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_ERROR_CHECK_2_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof HttpBadRequestException));
    }
    
    @Test
    public void registerEntityErrorCheck_notSupportedMediaType() throws Exception {
        /*
         * check the error if the media type is not supported like e.g. text/html
         */
    	mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.TEXT_HTML))
    			.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof HttpMediaTypeNotSupportedException));   	
    }
    
    /*
     * TODO: this test is supposed to check the case when Metis returns an error,
     * but currently, since we use the MockWebServer to mock the Metis server, it is not possible to test 
     */
    public void registerEntityErrorCheck_metisError() throws Exception {

        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

        /*
         * check the error if the URI provided to Metis causes it to produce error
         */
    	mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_METIS_ERROR_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
    			.andExpect(status().is5xxServerError());   	
    }
    
    @Test
    public void registerEntityErrorCheck_entityDisabled() throws Exception {
    	/*
    	 * check the error if the entity is removed/disabled
    	 */
        Timespan timespan = objectMapper.readValue(loadFile(TIMESPAN_JSON), Timespan.class);
        EntityRecord entityRecord =  new EntityRecord();
        entityRecord.setEntity(timespan);
        entityRecord.setEntityId(timespan.getEntityId());
        entityRecordService.saveEntityRecord(entityRecord);
        
        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof EntityRemovedException));
    }

    /*
     * TODO: uncomment the @Test annotation below when the authorization of the wskey 
     * is added to the register API
     */
    //@Test
    public void registerEntityErrorCheck_wrongWsKey() throws Exception {
        /*
         * check the error if the wskey is wrong
         */
        mockMvc.perform(post(BASE_SERVICE_URL)
        		.param(CommonApiConstants.PARAM_WSKEY, "wrong-wskey-param")
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof ApplicationAuthenticationException));      
    }


    @Test
    public void registerConceptShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(CONCEPT_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(CONCEPT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE));
        
        results.andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties
    }

    @Test
    void registerAgentShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
        	.andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties
    }

    
    @Test
    void registerAgentStalinShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(AGENT_STALIN_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(AGENT_REGISTER_STALIN_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
                
        
                results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Agent.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));
        	//
        	//results.andExpect(jsonPath("$.prefLabel[*]", hasSize(24))).andExpect(jsonPath("$.altLabel[*]", hasSize(12)));
    }
    
    @Test
    public void registerOrganizationShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(ORGANIZATION_XML)));
       
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(ORGANIZATION_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))

                .andExpect(jsonPath("$.type", is(EntityTypes.Organization.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties

    }

    @Test
    void registerPlaceShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(PLACE_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(PLACE_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        System.out.println(results.andReturn().getResponse().getContentAsString());
        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Place.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

//        System.out.println(((MockMvc)results).val);
        //TODO assert other important properties
        //results.andExpect(jsonPath("$.prefLabel[*]", hasSize(5)))
//        .andExpect(jsonPath("$.lat", greaterThan(48.0)))
//        .andExpect(jsonPath("$.long", greaterThan(2.0)));
//        .andExpect(jsonPath("$.lat", is(48.85341)))
//        .andExpect(jsonPath("$.long", is(2.3488)));
}

    @Test
    void registerTimespanShouldBeSuccessful() throws Exception {
        // set mock Metis response
        mockMetis.enqueue(new MockResponse().setResponseCode(200).setBody(loadFile(TIMESPAN_XML)));

        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL)
                .content(loadFile(TIMESPAN_REGISTER_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isAccepted());
        
        results.andExpect(jsonPath("$.id", any(String.class)))
        //enable when working propertly
                .andExpect(jsonPath("$.type", is(EntityTypes.Timespan.name())))
//                .andExpect(jsonPath("$.entity").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        //TODO assert other important properties
}
    
    @TestConfiguration
    public static class TestConfig {

        /**
         * Do not trigger batch jobs in this test.
         */
        @Bean
        @Primary
        public BatchService batchServiceBean() throws Exception {
            BatchService batchService = Mockito.mock(BatchService.class);
            doNothing().when(batchService).launchSpecificEntityUpdate(anyList(), anyBoolean());
            return batchService;
        }
    }

}
