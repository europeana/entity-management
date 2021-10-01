package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.common.config.AppConfigConstants.BEAN_EM_VALIDATOR_FACTORY;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_VALIDATE_FIELDS_EMPTY_PREFLABEL_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;
import eu.europeana.entitymanagement.normalization.EntityFieldsCompleteValidatorGroup;
import eu.europeana.entitymanagement.normalization.EntityFieldsMinimalValidatorGroup;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
      ValidatorConfig.class,
      EntityManagementConfiguration.class,
      SerializationConfig.class
    })
public class EntityFieldsValidatorTest {

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  private ObjectMapper objectMapper;

  @Resource(name = BEAN_EM_VALIDATOR_FACTORY)
  private ValidatorFactory emValidatorFactory;

  @Test
  public void completeValidationEntityFieldsOrganization()
      throws JsonMappingException, JsonProcessingException, IOException {
    // TODO: the input file must be updated to comply with the correct jsonld serialization
    Organization organization =
        objectMapper.readValue(loadFile(ORGANIZATION_VALIDATE_FIELDS_JSON), Organization.class);
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntity(organization);
    entityRecord.setEntityId(organization.getEntityId());
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entityRecord.getEntity(), EntityFieldsCompleteValidatorGroup.class);
    for (ConstraintViolation<Entity> violation : violations) {
      System.out.println(violation.getMessageTemplate());
    }
    // TODO: remove constraine violation: "The entity fields values are valid."
    Assertions.assertEquals(18, violations.size());
  }

  @Test
  public void minimalValidationEntityFieldsOrganization()
      throws JsonMappingException, JsonProcessingException, IOException {
    // TODO: the input file must be updated to comply with the correct jsonld serialization
    Organization organization =
        objectMapper.readValue(loadFile(ORGANIZATION_VALIDATE_FIELDS_JSON), Organization.class);
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntity(organization);
    entityRecord.setEntityId(organization.getEntityId());
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entityRecord.getEntity(), EntityFieldsMinimalValidatorGroup.class);
    for (ConstraintViolation<Entity> violation : violations) {
      System.out.println(violation.getMessageTemplate());
    }
    // TODO: remove constraine violation: "The entity fields values are valid."
    Assertions.assertEquals(3, violations.size());
  }

  @Test
  public void completeValidationEntityFieldsAgent()
      throws JsonMappingException, JsonProcessingException, IOException {

    Agent agent = objectMapper.readValue(loadFile(AGENT_VALIDATE_FIELDS_JSON), Agent.class);
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntity(agent);
    entityRecord.setEntityId(agent.getEntityId());
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entityRecord.getEntity(), EntityFieldsCompleteValidatorGroup.class);
    for (ConstraintViolation<Entity> violation : violations) {
      System.out.println(violation.getMessageTemplate());
    }
    // TODO: remove constraine violation: "The entity fields values are valid."
    Assertions.assertEquals(2, violations.size());
  }

  @Test
  public void minimalValidationEntityFieldsAgent()
      throws JsonMappingException, JsonProcessingException, IOException {

    Agent agent = objectMapper.readValue(loadFile(AGENT_VALIDATE_FIELDS_JSON), Agent.class);
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntity(agent);
    entityRecord.setEntityId(agent.getEntityId());
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entityRecord.getEntity(), EntityFieldsMinimalValidatorGroup.class);
    for (ConstraintViolation<Entity> violation : violations) {
      System.out.println(violation.getMessageTemplate());
    }
    // TODO: remove constraine violation: "The entity fields values are valid."
    Assertions.assertEquals(2, violations.size());
  }

  @Test
  void validationShouldFailIfPrefLabelIsEmpty() throws IOException {
    // file contains same content as AGENT_VALIDATE_FIELDS_JSON, except empty prefLabel
    Agent agent =
        objectMapper.readValue(loadFile(AGENT_VALIDATE_FIELDS_EMPTY_PREFLABEL_JSON), Agent.class);
    EntityRecord entityRecord = new EntityRecord();
    entityRecord.setEntity(agent);
    entityRecord.setEntityId(agent.getEntityId());
    Set<ConstraintViolation<Entity>> violations =
        emValidatorFactory
            .getValidator()
            .validate(entityRecord.getEntity(), EntityFieldsMinimalValidatorGroup.class);
    for (ConstraintViolation<Entity> violation : violations) {
      System.out.println(violation.getMessageTemplate());
    }

    // file contains same content as AGENT_VALIDATE_FIELDS_JSON, except empty prefLabel
    Assertions.assertEquals(3, violations.size());
  }
}
