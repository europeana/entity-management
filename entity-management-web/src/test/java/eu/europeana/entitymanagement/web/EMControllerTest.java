package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import eu.europeana.entitymanagement.EntityManagementApp;
import eu.europeana.entitymanagement.mongo.repository.EntityRecordRepository;

@SpringBootTest(classes = { EntityManagementApp.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class EMControllerTest extends BaseMvcTestUtils{

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired EntityRecordRepository entityRecordRepository;
    
    @BeforeEach
    public void initTest() {
//	if (mockMvc == null) {
//	    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//	}
    }
    
    @Test
    public void registerEntity_concept() throws Exception {
	String externalId = "http://www.wikidata.org/entity/Q11019";
	//TOD: ensure the entity doesn't exist in the database (e.g. due to failing tests)
	String requestJson = getJsonStringInput(CONCEPT_BATHTUB);

	String result = mockMvc
		.perform(post(BASE_SERVICE_URL).content(requestJson).header(HttpHeaders.AUTHORIZATION, "enable_authorization")
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
	
	String entityId = getEntityId(result);
	assertNotNull(entityId);
	
	Long res = entityRecordRepository.deleteForGood(entityId);
	System.out.println(res);
//	assertEquals(1, res);
	
    }
   
}
