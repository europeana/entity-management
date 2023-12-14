package eu.europeana.entitymanagement.exception;

import org.springframework.http.HttpStatus;
import eu.europeana.api.commons.error.EuropeanaApiException;

/**
 * Exception class to indicate multiple choice for redirection, while only one option is expected
 */
public class MultipleChoicesException extends EuropeanaApiException {

  private static final long serialVersionUID = -8030793786850765917L;

  public MultipleChoicesException(String message) {
    super(message);
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
    return HttpStatus.MULTIPLE_CHOICES;
  }
}
