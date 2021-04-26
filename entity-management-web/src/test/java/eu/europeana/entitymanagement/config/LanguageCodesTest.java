package eu.europeana.entitymanagement.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.common.config.LanguageCodes;

/**
 * JUnit test to check if DataSources are properly deserialized from XML
 */
@SpringBootTest(classes = SerializationConfig.class)
public class LanguageCodesTest {

    @Autowired
    @Qualifier(AppConfigConstants.BEAN_XML_MAPPER)
    private XmlMapper xmlMapper;


    @Test
    public void shouldDeserializeLanugageCodes() throws IOException {
	String LANGUAGECODES_XML = "/languagecodes/test-languagecodes.xml";
        InputStream is = getClass().getResourceAsStream(LANGUAGECODES_XML);

        LanguageCodes languageCodes = xmlMapper.readValue(is, LanguageCodes.class);
        assertNotNull(languageCodes);
        assertNotNull(languageCodes.getLanguages());
        assertNotNull(languageCodes.getAltLangMap());
        assertEquals(24, languageCodes.getLanguages().size());
        assertEquals(54, languageCodes.getAltLangMap().size());
        assertTrue(languageCodes.isValidLanguageCode("en"));
        assertEquals("en", languageCodes.getByAlternativeCode("en-gb"));
        assertEquals("en", languageCodes.getByAlternativeCode("eng"));
        
    }

}
