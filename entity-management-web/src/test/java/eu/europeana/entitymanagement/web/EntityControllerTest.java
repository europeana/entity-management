package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entity.edm.internal.Entity;
import eu.europeana.entitymanagement.config.DataSource;
import eu.europeana.entitymanagement.model.EntityRequest;
import eu.europeana.entitymanagement.service.EntityService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static eu.europeana.entity.utils.Constants.CONTEXT_ENTITY;
import static eu.europeana.entity.utils.Constants.ID_PREFIX_CONCEPT;
import static eu.europeana.entity.utils.Constants.MEDIA_TYPE_JSONLD;
import static eu.europeana.entity.utils.Constants.TYPE_CONCEPT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit test for testing the RecommendController class
 */
@SpringBootTest
@AutoConfigureMockMvc
class EntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntityService entityService;

    private final String derefEntityId1 = "xyz";
    private final Entity dereferencedEntity1 = new Entity.Builder(ID_PREFIX_CONCEPT + "/" + derefEntityId1, TYPE_CONCEPT)
            .addPrefLabel("en", Collections.singletonList("Bathtub"))
            .addAltLabel("en", Arrays.asList("bath", "tub"))
            .setDepiction("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/1rm60rt0t_rtbcwh.jpg/330px-1rm60rt0t_rtbcwh.jpg")
            .addSameAs("http://www.wikidata.org/entity/Q152095")
            .build();

    private final String testRequestId = "http://www.wikidata.org/entity/Q152095";
    private final EntityRequest testRequest = new EntityRequest(testRequestId,
            CONTEXT_ENTITY,
            Map.of("en", "bathtub"),
            Map.of("en", Arrays.asList("bath", "tub")),
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/1rm60rt0t_rtbcwh.jpg/330px-1rm60rt0t_rtbcwh.jpg"
    );


    @Test
    void createEntityShouldReturnSuccessfully() throws Exception {
        // mock datasource lookup
        when(entityService.checkSourceExists(eq(testRequestId))).thenReturn(true);

        when(entityService.createNewEntity(eq(testRequest))).thenCallRealMethod();

        mockMvc.perform(post("/entity")
                .content(asJsonString(testRequest))
                .contentType(MEDIA_TYPE_JSONLD)
                .accept(MEDIA_TYPE_JSONLD)
        )
                .andExpect(status().isAccepted());
        //TODO: assert response body
    }

    @Test
    void createEntityShouldReturn301IfEntityExists() throws Exception {
        // mock existing entity
        when(entityService.checkEntityExists(testRequestId))
                .thenReturn(Optional.ofNullable(dereferencedEntity1));

        mockMvc.perform(post("/entity")
                .content(asJsonString(testRequest))
                .contentType(MEDIA_TYPE_JSONLD)
                .accept(MEDIA_TYPE_JSONLD)
        )
                .andExpect(status().isMovedPermanently())
                // location should be /entity/{type}/{id}.jsonld, using values from dereferencedEntity1
                .andExpect(header().string("Location", String.format("/entity/concept/%s.jsonld", derefEntityId1)));
    }

    @Test
    void createEntityShouldReturn400IfIdSourceNotMatched() throws Exception {
        mockMvc.perform(post("/entity")
                .content(asJsonString(testRequest))
                .contentType(MEDIA_TYPE_JSONLD)
                .accept(MEDIA_TYPE_JSONLD)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEntityShouldReturn301IfMetisCorefExists() throws Exception {
        // mock datasource lookup
        when(entityService.checkSourceExists(eq(testRequestId))).thenReturn(true);
        // mock behaviour of Metis deferencing
        when(entityService.checkCoreferencedEntity(eq(testRequestId))).thenReturn(Optional.ofNullable(dereferencedEntity1));

        mockMvc.perform(post("/entity")
                .content(asJsonString(testRequest))
                .contentType(MEDIA_TYPE_JSONLD)
                .accept(MEDIA_TYPE_JSONLD)
        )
                .andExpect(status().isMovedPermanently())
                // location should be /entity/{type}/{id}.jsonld, using values from dereferencedEntity1
                .andExpect(header().string("Location", String.format("/entity/concept/%s.jsonld", derefEntityId1)));

    }


    /**
     * Helper method to create JSON string from objects
     *
     * @param obj object to serialize
     * @return serialized object representation.
     * @throws JsonProcessingException if serialization fails
     */
    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
