package eu.europeana.entitymanagement.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.logging.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.web.http.HttpHeaders;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestController
@ConditionalOnWebApplication
public class EMErrorController extends AbstractErrorController {

    public EMErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @GetMapping(value = "/error", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD})
    @ResponseBody
    public ResponseEntity<EuropeanaApiErrorResponse> errorGetMethod(final HttpServletRequest request) {
        return getErrorAttributes(request);
    }

    @PostMapping(value = "/error", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD})
    @ResponseBody
    public ResponseEntity<EuropeanaApiErrorResponse> errorPostMethod(final HttpServletRequest request) {
        return getErrorAttributes(request);
    }

    /**
     * Generates EuropeanaApiErrorResponse for "/error" mappings
     * @param request http request
     * @return error response
     */
    private ResponseEntity<EuropeanaApiErrorResponse> getErrorAttributes(final HttpServletRequest request) {
        Map<String, Object> map = this.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        int status = getStatus(map);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(status)
                .headers(headers)
                .body(
                new EuropeanaApiErrorResponse.Builder(request, true, null, true)
                        .setStatus(status)
                        .setError(getKeyValues(map, "error"))
                        .setMessage(getKeyValues(map, "message"))
                        .build());
    }

    /**
     * get the https status value
     * if there is an error return the Internal Server Error
     * @param map
     * @return
     */
    private int getStatus(Map<String, Object> map) {
        try {
            return Integer.parseInt(map.get("status").toString());
        } catch (NumberFormatException e) {
            return 500;
        }
    }

    private String getKeyValues(Map<String, Object> map, String key) {
        if (map.get(key) != null) {
            return map.get(key).toString();
        }
        return "";

    }
}
