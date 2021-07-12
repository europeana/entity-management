package eu.europeana.entitymanagement.validation;

import static eu.europeana.entitymanagement.testutils.BaseMvcTestUtils.loadFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.xml.bind.JAXBContext;

import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import eu.europeana.entitymanagement.common.config.EntityManagementConfiguration;
import eu.europeana.entitymanagement.common.config.LanguageCodes;
import eu.europeana.entitymanagement.config.SerializationConfig;
import eu.europeana.entitymanagement.config.ValidatorConfig;
import eu.europeana.entitymanagement.definitions.model.Agent;
import eu.europeana.entitymanagement.normalization.EntityFieldsCleaner;
import eu.europeana.entitymanagement.testutils.BaseMvcTestUtils;
import eu.europeana.entitymanagement.web.MetisDereferenceUtils;

@SpringBootTest(classes = {ValidatorConfig.class,
    SerializationConfig.class, EntityManagementConfiguration.class})
public class EntityFieldsCleanerTest {

    private static final String AGENT_STALIN_CLEANING_XML = "/metis-deref/agent_stalin_cleaning.xml";

    @Autowired
    private LanguageCodes emLanguageCodes;

    @Autowired
    private JAXBContext jaxbContext;

    
    @Test
    public void shouldCleanEntityFields() throws Exception {
        XmlBaseEntityImpl<?> xmlAgent = MetisDereferenceUtils
                .parseMetisResponse(jaxbContext.createUnmarshaller(), "http://www.wikidata.org/entity/Q855", loadFile(AGENT_STALIN_CLEANING_XML));
        assert xmlAgent != null;

        Agent agent = (Agent) xmlAgent.toEntityModel();
        EntityFieldsCleaner fieldCleaner = new EntityFieldsCleaner(emLanguageCodes);
        fieldCleaner.cleanAndNormalize(agent);
        assertEquals(24, agent.getPrefLabel().size());
        assertEquals(12, agent.getAltLabel().size());
        //TODO: perform more robust testing
        //TODO: create json file with expected output and use entity comparator
        for(String dateOfBirth : agent.getDateOfBirth()) {
        	Assertions.assertFalse(dateOfBirth.contains("T"));
        }
        Assertions.assertFalse(agent.getDateOfEstablishment().contains("T"));
        
    }

}
