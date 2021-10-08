package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Timespan;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;
import java.util.Optional;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class EntityUpdateIT extends BaseWebControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void updatingNonExistingEntityShouldReturn404() throws Exception {
        /*
         * check the error if the entity does not exist prior to its update
         */
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + "concept/1")
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updatingDeprecatedEntityShouldReturn410() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        deprecateEntity(entityRecord);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());
    }


    @Test
    public void updatingNonExistingEntityFromExternalSourceShouldReturn404() throws Exception {
        /*
         * check the error if the entity does not exist
         */
        mockMvc.perform(post(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier/management/update")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updatingDeprecatedEntityFromExternalSourceShouldReturn410() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
        deprecateEntity(entityRecord);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(post(BASE_SERVICE_URL + "/" + requestPath + "/management/update")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());

    }

    @Test
    void updateFromExternalDatasourceShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }
    
    @Test
    void updateTimespanShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(TIMESPAN_REGISTER_1ST_CENTURY_JSON);
        String metisResponse = loadFile(TIMESPAN_1ST_CENTURY_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, TIMESPAN_1ST_CENTURY_URI);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        
        Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
        Assertions.assertTrue(entityRecordUpdated.isPresent());
        Timespan timespan = (Timespan)(entityRecordUpdated.get().getEntity());
        Assertions.assertNotNull(timespan.getPrefLabel());
        Assertions.assertFalse(timespan.getPrefLabel().isEmpty());
        Assertions.assertNotNull(timespan.getAltLabel());
        Assertions.assertNotNull(timespan.getBeginString());
        Assertions.assertNotNull(timespan.getEndString());
        Assertions.assertFalse(timespan.getAltLabel().isEmpty());

//        Assertions.assertNotNull(timespan.getIsPartOfArray());
//        Assertions.assertNotNull(timespan.getIsNextInSequence());
    }

    @Test
    public void updateConceptShouldBeSuccessful() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);

        String requestPath = getEntityRequestPath(entityRecord.getEntityId());
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
                .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

        final ObjectNode nodeReference = mapper.readValue(loadFile(CONCEPT_UPDATE_BATHTUB_JSON), ObjectNode.class);
        Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
        Assertions.assertTrue(entityRecordUpdated.isPresent());
        Assertions.assertEquals(nodeReference.path("depiction").path("id").asText(),
                entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction().getId());
        Assertions.assertEquals(nodeReference.path("note").path("en").path(0).asText(),
                entityRecordUpdated.get().getEuropeanaProxy().getEntity().getNote().get("en").get(0));
        // acquire the reader for the right type
        ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, String>>() {
        });
        Map<String, String> prefLabelToCheck = reader.readValue(nodeReference.path("prefLabel"));
        Map<String, String> prefLabelUpdated = entityRecordUpdated.get().getEuropeanaProxy().getEntity().getPrefLabel();
        for (Map.Entry<String, String> prefLabelEntry : prefLabelToCheck.entrySet()) {
            Assertions.assertTrue(prefLabelUpdated.containsKey(prefLabelEntry.getKey()));
            Assertions.assertTrue(prefLabelUpdated.containsValue(prefLabelEntry.getValue()));
        }
    }

    @Test
    void updatePUTShouldReplaceEuropeanaProxy() throws Exception {
        String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
        String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

        EntityRecord savedRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);


        // assert content of Europeana proxy
        Entity europeanaProxyEntity = savedRecord.getEuropeanaProxy().getEntity();

        // values match labels in json file
        Assertions.assertNotNull(europeanaProxyEntity.getPrefLabel().get("en"));
        Assertions.assertNotNull(europeanaProxyEntity.getAltLabel().get("en").get(0));
        Assertions.assertNotNull(europeanaProxyEntity.getAltLabel().get("en").get(1));
        Assertions.assertNotNull(europeanaProxyEntity.getDepiction());

        String requestPath = getEntityRequestPath(savedRecord.getEntityId());
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_BATHTUB_EMPTY_UPDATE_JSON))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        // check that update removed fields from Europeana proxy in original request
        Optional<EntityRecord> updatedRecord = retrieveEntity(savedRecord.getEntityId());
        Assertions.assertTrue(updatedRecord.isPresent());
        europeanaProxyEntity = updatedRecord.get().getEuropeanaProxy().getEntity();

        Assertions.assertNull(europeanaProxyEntity.getPrefLabel());
        Assertions.assertNull(europeanaProxyEntity.getAltLabel());
        Assertions.assertNull(europeanaProxyEntity.getNote());
        Assertions.assertNull(europeanaProxyEntity.getDepiction());
    }
}
