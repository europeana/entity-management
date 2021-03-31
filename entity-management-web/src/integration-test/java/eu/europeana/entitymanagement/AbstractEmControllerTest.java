package eu.europeana.entitymanagement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.web.service.impl.EntityRecordService;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractEmControllerTest extends AbstractIntegrationTest {
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected EntityRecordService entityRecordService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

}
