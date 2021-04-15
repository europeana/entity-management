package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.config.AppConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.exception.EntityCreationException;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.testutils.BaseMvcTestUtils;
import eu.europeana.entitymanagement.web.service.MetisDereferenceService;

@SpringBootTest
public class EntityFieldsCleanerTest {

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;
    
    @Resource(name = "emLanguageCodes")
    LanguageCodes emLanguageCodes;
    
    @Resource(name = AppConfig.BEAN_METIS_DEREF_SERVICE)
    MetisDereferenceService metisDerefService;
    
    @Test
    public void validateEntityFields() throws JsonMappingException, JsonProcessingException, IOException, EntityCreationException, EuropeanaApiException {
        // read the test data for the Concept entity from the file
//        ConceptImpl concept = objectMapper.readValue(loadFile(CONCEPT_VALIDATE_FIELDS_JSON), ConceptImpl.class);
	Agent agent = (Agent) metisDerefService.parseMetisResponse("http://www.wikidata.org/entity/Q855", loadFile(BaseMvcTestUtils.AGENT_STALIN_XML)); 
	
        EntityFieldsCleaner fieldCleaner = new EntityFieldsCleaner(emLanguageCodes);
        fieldCleaner.cleanAndNormalize(agent);
        
    }

}
