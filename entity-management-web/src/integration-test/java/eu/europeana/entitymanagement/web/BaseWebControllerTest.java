package eu.europeana.entitymanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entitymanagement.AbstractIntegrationTest;
import eu.europeana.entitymanagement.batch.service.EntityUpdateService;
import eu.europeana.entitymanagement.common.config.AppConfigConstants;
import eu.europeana.entitymanagement.definitions.model.EntityRecord;
import eu.europeana.entitymanagement.web.model.EntityPreview;
import eu.europeana.entitymanagement.web.xml.model.XmlBaseEntityImpl;
import okhttp3.mockwebserver.MockResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static eu.europeana.api.commons.web.http.HttpHeaders.CONTENT_TYPE_JSONLD_UTF8;
import static eu.europeana.api.commons.web.http.HttpHeaders.VALUE_LDP_RESOURCE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

abstract class BaseWebControllerTest extends AbstractIntegrationTest {

    @Autowired
    private JAXBContext jaxbContext;

    @Autowired
    private EntityUpdateService entityUpdateService;

    @Qualifier(AppConfigConstants.BEAN_JSON_MAPPER)
    @Autowired
    private ObjectMapper objectMapper;


    protected static String loadFile(String resourcePath) throws IOException {
        InputStream is = BaseWebControllerTest.class.getResourceAsStream(resourcePath);
        assert is != null;
        return IOUtils.toString(is, StandardCharsets.UTF_8).replace("\n", "");
    }


    /**
     * Gets the "{type}/{identifier}" from an EntityId string
     */
    protected String getEntityRequestPath(String entityId) {
        //entity id is "http://data.europeana.eu/{type}/{identifier}"
        String[] parts = entityId.split("/");

        // namespace is always base
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
    }


    /**
     * Checks common response headers.
     * Allow header checked within each test method.
     */
    protected void checkCommonResponseHeaders(ResultActions results) throws Exception {
        results.andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                is(CONTENT_TYPE_JSONLD_UTF8)))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.LINK, is(VALUE_LDP_RESOURCE)))
                .andExpect(header().stringValues(HttpHeaders.VARY, hasItems(containsString(HttpHeaders.ACCEPT))));
    }


    protected void checkAllowHeaderForPOST(ResultActions results) throws Exception {
        results.andExpect(header().stringValues(
                HttpHeaders.ALLOW, hasItems(
                        containsString("POST")
                )));
    }

    protected void checkAllowHeaderForGET(ResultActions results) throws Exception {
        results.andExpect(header().stringValues(
                HttpHeaders.ALLOW, hasItems(
                        containsString("GET")
                )));
    }

    /**
     * Expects Allow header in response to contain DELETE,POST,GET,PUT
     */
    protected void checkAllowHeaderForDPGP(ResultActions results) throws Exception {
        results.andExpect(header().stringValues(
                HttpHeaders.ALLOW, hasItems(
                        containsString("GET"),
                        containsString("DELETE"),
                        containsString("POST"),
                        containsString("PUT")
                )));
    }


    protected EntityRecord createEntity(String europeanaMetadata, String metisResponse, String externalId) throws Exception {
        EntityPreview entityPreview = objectMapper.readValue(europeanaMetadata, EntityPreview.class);
        XmlBaseEntityImpl<?> xmlBaseEntity = MetisDereferenceUtils
                .parseMetisResponse(jaxbContext.createUnmarshaller(), externalId, metisResponse);

        assert xmlBaseEntity != null;
        EntityRecord savedRecord = entityRecordService.createEntityFromRequest(entityPreview, xmlBaseEntity.toEntityModel());

        // trigger update to generate consolidated entity
        entityUpdateService.runSynchronousUpdate(savedRecord.getEntityId());

        // return entityRecord version with consolidated entity
        return entityRecordService.retrieveByEntityId(savedRecord.getEntityId()).orElseThrow();
    }

    protected void deprecateEntity(EntityRecord entityRecord){
        entityRecordService.disableEntityRecord(entityRecord);
    }

    protected Optional<EntityRecord> retrieveEntity(String entityId){
        return entityRecordService.retrieveByEntityId(entityId);
    }
}
