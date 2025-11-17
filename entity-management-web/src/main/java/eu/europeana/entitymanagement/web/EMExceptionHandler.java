package eu.europeana.entitymanagement.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.web.exception.EuropeanaGlobalExceptionHandler;

import static eu.europeana.entitymanagement.common.vocabulary.AppConfigConstants.BEAN_I18N_SERVICE;

@ControllerAdvice
@ConditionalOnWebApplication
public class EMExceptionHandler extends EuropeanaGlobalExceptionHandler {
  // exception handling inherited from parent

  @Resource(name = BEAN_I18N_SERVICE)
  I18nService i18nService;

  protected I18nService getI18nService() {
    return i18nService;
  }

  @ExceptionHandler
  public ResponseEntity<EuropeanaApiErrorResponse> handleException(
      HttpMessageNotReadableException e, HttpServletRequest httpRequest) {
    EuropeanaApiErrorResponse response =
        new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
            .setStatus(HttpStatus.BAD_REQUEST.value())
            .setError("Error parsing request body")
            .setMessage("JSON is either malformed or missing required 'type' property")
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
        .headers(createHttpHeaders(httpRequest))
        .body(response);
  }

  @ExceptionHandler(HttpMediaTypeException.class)
  public ResponseEntity<EuropeanaApiErrorResponse> handleInvalidMediaType(
      HttpMediaTypeException e, HttpServletRequest httpRequest) {

    EuropeanaApiErrorResponse response =
        new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
            .setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
            .setError(e.getMessage())
            .setMessage(
                "Unsupported media type. Supported types are: "
                    + MediaType.toString(e.getSupportedMediaTypes()))
            .build();

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
        .headers(createHttpHeaders(httpRequest))
        .body(response);
  }
  
}
