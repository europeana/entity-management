package eu.europeana.entitymanagement.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.web.exception.EuropeanaGlobalExceptionHandler;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entitymanagement.web.service.RequestPathMethodService;

@ControllerAdvice
@ConditionalOnWebApplication
public class EMExceptionHandler extends EuropeanaGlobalExceptionHandler {
  // exception handling inherited from parent

  private I18nService i18nService;

  @Autowired
  public EMExceptionHandler(
      RequestPathMethodService requestPathMethodService, I18nService i18nService) {
    this.requestPathMethodService = requestPathMethodService;
    this.i18nService = i18nService;
  }

  /**
   * Default handler for EuropeanaApiException types
   *
   * @param e caught exception
   */
  @ExceptionHandler
  public ResponseEntity<EuropeanaApiErrorResponse> handleCommonHttpException(
      HttpException e, HttpServletRequest httpRequest) {

    // TODO: harmonize the use of HTTP Exceptions and EuropeanaAPIExceptions
    EuropeanaApiErrorResponse response =
        new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
            .setStatus(e.getStatus().value())
            .setError(e.getStatus().getReasonPhrase())
            .setMessage(i18nService.getMessage(e.getI18nKey(), e.getI18nParams()))
            // code only included in JSON if a value is set in exception
            .setCode(e.getI18nKey())
            .build();

    return ResponseEntity.status(e.getStatus())
        .headers(createHttpHeaders(httpRequest))
        .body(response);
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

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<EuropeanaApiErrorResponse> handleNoHandlerFoundException(
      NoHandlerFoundException e, HttpServletRequest httpRequest) {

    EuropeanaApiErrorResponse response =
        new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled())
            .setStatus(HttpStatus.NOT_FOUND.value())
            .setError(HttpStatus.NOT_FOUND.getReasonPhrase())
            .setMessage(e.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
  }
  
}
