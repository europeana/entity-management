package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.web.MetisDereferenceUtils;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.testutils.BaseMvcTestUtils;

@SpringBootTest(classes = {ValidatorConfig.class,
    SerializationConfig.class, EntityManagementConfiguration.class})
public class EntityFieldsCleanerTest {
    
    @Autowired
    private LanguageCodes emLanguageCodes;

    @Autowired
    private JAXBContext jaxbContext;

    
    @Test
    public void shouldCleanEntityFields() throws Exception {
	Agent agent = (Agent) MetisDereferenceUtils
      .parseMetisResponse(jaxbContext.createUnmarshaller(), "http://www.wikidata.org/entity/Q855", loadFile(BaseMvcTestUtils.AGENT_STALIN_XML));
	
        EntityFieldsCleaner fieldCleaner = new EntityFieldsCleaner(emLanguageCodes);
        assert agent != null;
        fieldCleaner.cleanAndNormalize(agent);
        
        assertEquals(24, agent.getPrefLabel().size());
        assertEquals(12, agent.getAltLabel().size());
        //TODO: perform more robust testing
        //TODO: create json file with expected output and use entity comparator
        
    }

}
