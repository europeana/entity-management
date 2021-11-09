package eu.europeana.entitymanagement.web;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.BASE_SERVICE_URL;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_EMPTY_UPDATE_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_BATHTUB_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_REGISTER_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_UPDATE_BATHTUB_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.IS_SHOWN_BY_CSV;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_1ST_CENTURY_URI;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_1ST_CENTURY_XML;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.TIMESPAN_REGISTER_1ST_CENTURY_JSON;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.TimeSpan;
import eu.europeana.entitymanagement.definitions.model.WebResource;
import eu.europeana.entitymanagement.utils.EntityObjectFactory;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import eu.europeana.entitymanagement.vocabulary.WebEntityConstants;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityUpdateIT extends BaseWebControllerTest {

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  private ObjectMapper mapper;

  @Test
  public void updatingNonExistingEntityShouldReturn404() throws Exception {
    /*
     * check the error if the entity does not exist prior to its update
     */
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + "concept/1")
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

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
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
    mockMvc
        .perform(
            post(BASE_SERVICE_URL + "/" + "wrong-type/wrong-identifier/management/update")
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

    mockMvc
        .perform(
            post(BASE_SERVICE_URL + "/" + requestPath + "/management/update")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isGone());
  }

  @Test
  void updateFromExternalDatasourceShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
    String metisResponse = loadFile(CONCEPT_BATHTUB_XML);

    EntityRecord entityRecord = createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());
  }

  @Test
  void updateTimespanShouldBeSuccessful() throws Exception {
    String europeanaMetadata = loadFile(TIMESPAN_REGISTER_1ST_CENTURY_JSON);
    String metisResponse = loadFile(TIMESPAN_1ST_CENTURY_XML);

    EntityRecord entityRecord =
        createEntity(europeanaMetadata, metisResponse, TIMESPAN_1ST_CENTURY_URI);

    String requestPath = getEntityRequestPath(entityRecord.getEntityId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .param(WebEntityConstants.QUERY_PARAM_PROFILE, "external")
                .content(europeanaMetadata)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted());

    Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(entityRecordUpdated.isPresent());
    TimeSpan timespan = (TimeSpan) (entityRecordUpdated.get().getEntity());
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
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                .content(loadFile(CONCEPT_UPDATE_BATHTUB_JSON))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
        .andExpect(jsonPath("$.type", is(EntityTypes.Concept.name())));

    final ObjectNode nodeReference =
        mapper.readValue(loadFile(CONCEPT_UPDATE_BATHTUB_JSON), ObjectNode.class);
    Optional<EntityRecord> entityRecordUpdated = retrieveEntity(entityRecord.getEntityId());
    Assertions.assertTrue(entityRecordUpdated.isPresent());
    Assertions.assertEquals(
        nodeReference.path("depiction").path("id").asText(),
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getDepiction().getId());
    Assertions.assertEquals(
        nodeReference.path("note").path("en").path(0).asText(),
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getNote().get("en").get(0));
    // acquire the reader for the right type
    ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, String>>() {});
    Map<String, String> prefLabelToCheck = reader.readValue(nodeReference.path("prefLabel"));
    Map<String, String> prefLabelUpdated =
        entityRecordUpdated.get().getEuropeanaProxy().getEntity().getPrefLabel();
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
    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
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

  @Test
  void updateIsShownByFromCSVFile() throws Exception {
    try (Reader inputCsv = new FileReader(new File(IS_SHOWN_BY_CSV), StandardCharsets.UTF_8);
        CSVParser csvParser = new CSVParser(inputCsv, CSVFormat.DEFAULT); ) {
      /*
       * First the entities to be updated need to be created. Here for the demonstration, we create only one.
       */
      String europeanaMetadata = loadFile(CONCEPT_REGISTER_BATHTUB_JSON);
      String metisResponse = loadFile(CONCEPT_BATHTUB_XML);
      EntityRecord entityRecord =
          createEntity(europeanaMetadata, metisResponse, CONCEPT_BATHTUB_URI);
      List<String> entityIds = new ArrayList<String>();
      entityIds.add(entityRecord.getEntityId());
      int count = 0;

      for (CSVRecord record : csvParser) {
        /*
         * This check is to update only the entities that have been previously generated so that the test can successfully pass.
         */
        if (count >= entityIds.size()) continue;

        String entityId = record.get(0);
        String isShownById = record.get(1);
        String isShownBySource = record.get(2);
        String isShownByThumbnail = record.get(3);

        WebResource isShownBy = new WebResource();
        isShownBy.setId(isShownById);
        isShownBy.setSource(isShownBySource);
        isShownBy.setThumbnail(isShownByThumbnail);

        Entity entity =
            EntityObjectFactory.createProxyEntityObject(EntityTypes.getByEntityId(entityId).name());
        entity.setIsShownBy(isShownBy);
        ObjectNode updateApiBodyNode = mapper.valueToTree(entity);

        /*
         * calling the update api to update the isShownBy field
         */
        String requestPath = getEntityRequestPath(entityIds.get(count));
        mockMvc
            .perform(
                MockMvcRequestBuilders.put(BASE_SERVICE_URL + "/" + requestPath)
                    .content(updateApiBodyNode.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id", is(entityRecord.getEntityId())))
            .andExpect(jsonPath("$.isShownBy.id", is(isShownById)))
            .andExpect(jsonPath("$.isShownBy.source", is(isShownBySource)))
            .andExpect(jsonPath("$.isShownBy.thumbnail", is(isShownByThumbnail)));

        count++;
      }
    }
  }
}
