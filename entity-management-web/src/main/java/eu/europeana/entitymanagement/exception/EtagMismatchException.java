package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;


public class EtagMismatchException extends EuropeanaApiException {

  public EtagMismatchException(String msg) {
    super(msg);
  }

  @Override
  public boolean doLog() {
    return false;
  }

  @Override
  public boolean doLogStacktrace() {
    return false;
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.PRECONDITION_FAILED;
  }
}
