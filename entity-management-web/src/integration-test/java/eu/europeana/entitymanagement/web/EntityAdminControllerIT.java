package eu.europeana.entitymanagement.web;

import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.testutils.BaseMvcTestUtils;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityAdminControllerIT extends BaseWebControllerTest {

    @Test
    void permanentDeletionShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertDeletion(entityRecord.getEntityId());
    }

    @Test
    void permanentDeletionForDeprecatedEntityShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        deprecateEntity(entityRecord);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(delete(BASE_SERVICE_URL + "/" + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertDeletion(entityRecord.getEntityId());
    }

    @Disabled
    @Test
    void migrationExistingEntityShouldBeSuccessful() throws Exception {
        String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";
        String entityId = EntityRecordUtils.buildEntityIdUri("concept", "1");
        ResultActions results = mockMvc.perform(post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "concept", "1")
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isAccepted());

        results.andExpect(jsonPath("$.id", any(String.class)))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())))
                .andExpect(jsonPath("$.isAggregatedBy").isNotEmpty())
                .andExpect(jsonPath("$.isAggregatedBy.aggregates", hasSize(2)))
                // should have Europeana and Datasource proxies
                .andExpect(jsonPath("$.proxies", hasSize(2)));

        // check that record is present
        Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityId);
        Assertions.assertFalse(dbRecordOptional.isEmpty());

    }

    @Disabled
    @Test
    void migrationExistingEntityInvalidEntityType() throws Exception {
        String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";
        mockMvc.perform(post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "testing", "1")
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    @Disabled
    @Test
    void migrationExistingEntityInvalidDataSource() throws Exception {
        String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.INVALID_MIGRATION_ID + "\"}";
        mockMvc.perform(post(BASE_SERVICE_URL + "/{type}/{identifier}" + BASE_ADMIN_URL, "concept", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Disabled
    @Test
    void migrationExistingEntityAlreadyExist() throws Exception {
        String requestBody = "{\"id\" : \"" + BaseMvcTestUtils.VALID_MIGRATION_ID + "\"}";

        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + requestPath + BASE_ADMIN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private void assertDeletion(String entityId) {
        // check that record is deleted
        Optional<EntityRecord> dbRecordOptional = retrieveEntity(entityId);
        Assertions.assertTrue(dbRecordOptional.isEmpty());
    }
}
