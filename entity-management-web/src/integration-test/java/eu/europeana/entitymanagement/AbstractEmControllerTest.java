package eu.europeana.entitymanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.web.service.EntityRecordService;

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
