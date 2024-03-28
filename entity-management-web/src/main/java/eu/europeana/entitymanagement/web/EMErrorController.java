package eu.europeana.entitymanagement.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.web.http.HttpHeaders;

@RestController
@ConditionalOnWebApplication
public class EMErrorController extends AbstractErrorController {

    public EMErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @GetMapping(value = "/error", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD})
    @ResponseBody
    public Map<String, Object> errorGetMethod(final HttpServletRequest request) {
        return getErrorAttributes(request);
    }

    @PostMapping(value = "/error", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD})
    @ResponseBody
    public Map<String, Object> errorPostMethod(final HttpServletRequest request) {
        return getErrorAttributes(request);
    }
    
    Map<String, Object> getErrorAttributes(final HttpServletRequest request) {
      return this.getErrorAttributes(request, ErrorAttributeOptions.defaults());
    }
}
