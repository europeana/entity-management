package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/** Exception thrown when a requested entity isn't found */
public class EntityNotFoundException extends EuropeanaApiException {

  private static final long serialVersionUID = -2506967519765835153L;

  public EntityNotFoundException(String entityUri) {
    super("No entity found with for specified uri(s): '" + entityUri + "'");
  }

  public EntityNotFoundException(String message, String identifier, Throwable t) {
    super(message + identifier);
  }

  public EntityNotFoundException(String entityUri, Throwable t) {
    super("No entity found with for specified uri(s): '" + entityUri + "'", t);
  }

  @Override
  public boolean doLog() {
    return false;
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public boolean doLogStacktrace() {
    return false;
  }
}
