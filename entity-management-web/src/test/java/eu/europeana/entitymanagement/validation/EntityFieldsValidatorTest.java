package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.definitions.model.Concept;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.Organization;

@SpringBootTest(classes = {ValidatorConfig.class, EntityManagementConfiguration.class,
    SerializationConfig.class})
public class EntityFieldsValidatorTest {

  @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
  @Autowired
  private ObjectMapper objectMapper;
  @Resource(name = "emValidatorFactory")
  private ValidatorFactory emValidatorFactory;


    @Test
    public void validateEntityFieldsForOrganization() throws JsonMappingException, JsonProcessingException, IOException {
	
        Organization organization = objectMapper.readValue(loadFile(ORGANIZATION_VALIDATE_FIELDS_JSON), Organization.class);
        EntityRecord entityRecord1 = new EntityRecord();
        entityRecord1.setEntity(organization);
        entityRecord1.setEntityId(organization.getEntityId());        
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord1.getEntity());
        for (ConstraintViolation<Entity> violation : violations) {
            System.out.print(violation.getMessageTemplate());
        }   
        Assertions.assertTrue(violations.size()==21);
    }

    @Test
    public void validateEntityFieldsForAgent() throws JsonMappingException, JsonProcessingException, IOException {
        
        Agent agent = objectMapper.readValue(loadFile(AGENT_VALIDATE_FIELDS_JSON), Agent.class);
        EntityRecord entityRecord2 = new EntityRecord();
        entityRecord2.setEntity(agent);
        entityRecord2.setEntityId(agent.getEntityId());        
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord2.getEntity());
        for (ConstraintViolation<Entity> violation : violations) {
            System.out.print(violation.getMessageTemplate());
        }   
        Assertions.assertTrue(violations.size()==1);
      
    }  
    
  @Test
  public void validateEntityFieldsForConcept()
      throws JsonMappingException, JsonProcessingException, IOException {
    // read the test data for the Concept entity from the file
    Concept concept = objectMapper.readValue(loadFile(CONCEPT_VALIDATE_FIELDS_JSON), Concept.class);

    //check the validation of the entity fields
    Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator()
        .validate(concept);
      Assertions.assertEquals(3, violations.size());
  }

}
