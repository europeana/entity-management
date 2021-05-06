package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.CONCEPT_VALIDATE_FIELDS_JSON;
import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.definitions.model.Concept;
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

@SpringBootTest(classes = {ValidatorConfig.class, EntityManagementConfiguration.class,
    SerializationConfig.class})
public class EntityFieldsValidatorTest {

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;
    
    @Resource(name = "emValidatorFactory")
    private ValidatorFactory emValidatorFactory;

    @Test
    public void validateEntityFields() throws JsonMappingException, JsonProcessingException, IOException {
        // read the test data for the Concept entity from the file
        Concept concept = objectMapper.readValue(loadFile(CONCEPT_VALIDATE_FIELDS_JSON), Concept.class);

        //check the validation of the entity fields
        Set<ConstraintViolation<Entity>> violations = emValidatorFactory.getValidator().validate(concept);
        Assertions.assertTrue(violations.size()==3);
        Assertions.assertTrue(!concept.getEntityId().contains(" "));        
    }

}
