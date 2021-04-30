package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.ORGANIZATION_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.AGENT_VALIDATE_FIELDS_JSON;
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
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.definitions.model.impl.AgentImpl;
import eu.europeana.entitymanagement.definitions.model.impl.ConceptImpl;
import eu.europeana.entitymanagement.definitions.model.impl.EntityRecordImpl;
import eu.europeana.entitymanagement.definitions.model.impl.OrganizationImpl;

@SpringBootTest
public class EntityFieldsValidatorTest {

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;
    
    @Resource(name = "emValidatorFactory")
    private ValidatorFactory emValidatorFactory;

    @Test
    public void validateEntityFields() throws JsonMappingException, JsonProcessingException, IOException {
	
        OrganizationImpl organization = objectMapper.readValue(loadFile(ORGANIZATION_VALIDATE_FIELDS_JSON), OrganizationImpl.class);
        EntityRecord entityRecord1 = new EntityRecordImpl();
        entityRecord1.setEntity(organization);
        entityRecord1.setEntityId(organization.getEntityId());        
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(entityRecord1.getEntity());
        for (ConstraintViolation<Entity> violation : violations) {
            System.out.print(violation.getMessageTemplate());
        }   
        Assertions.assertTrue(violations.size()==21);
  
        AgentImpl agent = objectMapper.readValue(loadFile(AGENT_VALIDATE_FIELDS_JSON), AgentImpl.class);
        EntityRecord entityRecord2 = new EntityRecordImpl();
        entityRecord2.setEntity(agent);
        entityRecord2.setEntityId(agent.getEntityId());        
        violations.clear();
        violations = emValidatorFactory.getValidator().validate(entityRecord2.getEntity());
        for (ConstraintViolation<Entity> violation : violations) {
            System.out.print(violation.getMessageTemplate());
        }   
        Assertions.assertTrue(violations.size()==1);
      
    }

}
