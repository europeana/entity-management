package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class EntityRemovedException extends EuropeanaApiException {

  public EntityRemovedException(String entityUri) {
    super(String.format("Entity %s has been removed", entityUri));
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.GONE;
  }
}
