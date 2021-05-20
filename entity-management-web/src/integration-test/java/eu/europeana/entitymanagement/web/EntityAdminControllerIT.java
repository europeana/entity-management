package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_ADMIN_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.getEntityRequestPath;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

/**
 * Integration test for the main Entity Management Admin controller
 */
@Disabled
@SpringBootTest
@AutoConfigureMockMvc
public class EntityAdminControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EntityRecordService entityRecordService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //ensure a clean db between test runs
        this.entityRecordService.dropRepository();
    }

    /**
     * Test that WebApplicationContext loads correctly
     */
    @Test
    public void shouldLoadCorrectly() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean(EntityAdminController.class));
    }

    @Test
    void permanentDeletionShouldBeSuccessful() throws Exception {
        // create entity in DB
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_JSON), Concept.class);
        EntityRecord entityRecord = new EntityRecord();
        entityRecord.setEntity(concept);
        entityRecord.setEntityId(concept.getEntityId());
        EntityRecord record = entityRecordService.saveEntityRecord(entityRecord);

        String requestPath = getEntityRequestPath(record.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // check that record is deleted
        Optional<EntityRecord> dbRecordOptional = entityRecordService.retrieveEntityRecordByUri(record.getEntityId());
        Assertions.assertTrue(dbRecordOptional.isEmpty());
    }
}
