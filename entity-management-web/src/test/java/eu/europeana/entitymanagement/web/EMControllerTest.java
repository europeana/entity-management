package eu.europeana.entitymanagement.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.entitymanagement.EntityManagementApp;

@SpringBootTest(classes = { EntityManagementApp.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class EMControllerTest extends BaseMvcTestUtils{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    public void initApplication() {
//	if (mockMvc == null) {
//	    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//	}
    }
    
    @Test
    public void createEntity_concept() throws Exception {
	String requestJson = getJsonStringInput(CONCEPT_BATHTUB);

	String result = mockMvc
		.perform(post(BASE_SERVICE_URL).content(requestJson).header(HttpHeaders.AUTHORIZATION, "enable_authorization")
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
	String identifier = getEntityIdentifier(result);
//	getUserSetService().deleteUserSet(identifier);
    }
   
}
