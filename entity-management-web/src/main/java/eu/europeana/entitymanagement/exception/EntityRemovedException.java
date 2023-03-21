package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class EntityRemovedException extends EuropeanaApiException {

  private static final long serialVersionUID = -6898015316847953613L;

  public EntityRemovedException(String message) {
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
    return HttpStatus.GONE;
  }
}
