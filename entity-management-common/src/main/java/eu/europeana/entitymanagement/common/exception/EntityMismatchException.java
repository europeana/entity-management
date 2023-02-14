package eu.europeana.entitymanagement.common.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/** Exception thrown when trying to combine metadata of different entity types */
public class EntityMismatchException extends EuropeanaApiException {

  public EntityMismatchException(String message) {
    super(message);
  }

  @Override
  public boolean doLog() {
    return true;
  }

  @Override
  public boolean doLogStacktrace() {
    return false;
  }

  @Override
  public boolean doExposeMessage() {
    return true;
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
