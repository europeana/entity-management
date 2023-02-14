package eu.europeana.entitymanagement.common.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception thrown when an a request is not accepted to be processed by the server. */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class HttpUnprocessableException extends EuropeanaApiException {

  /** */
  private static final long serialVersionUID = -4536642651964523519L;

  /**
   * Initialise a new exception for which there is no root cause
   *
   * @param msg error message
   */
  public HttpUnprocessableException(String msg) {
    super(msg);
  }

  /**
   * Initialise a new exception with the message and root cause
   *
   * @param msg error message
   */
  public HttpUnprocessableException(String msg, Throwable th) {
    super(msg, th);
  }

  /**
   * Initialise a new exception for which there is no root cause
   *
   * @param msg error message
   * @param errorCode error code
   */
  public HttpUnprocessableException(String msg, String errorCode) {
    super(msg, errorCode);
  }

  /**
   * We don't want to log the stack trace for this exception
   *
   * @return false
   */
  @Override
  public boolean doLogStacktrace() {
    return false;
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.UNPROCESSABLE_ENTITY;
  }
}
