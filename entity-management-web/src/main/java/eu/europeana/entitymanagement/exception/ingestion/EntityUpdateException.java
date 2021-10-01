package eu.europeana.entitymanagement.exception.ingestion;

import eu.europeana.api.commons.error.EuropeanaApiException;

public class EntityUpdateException extends EuropeanaApiException {

  /** */
  private static final long serialVersionUID = 6189347322304269972L;

  public EntityUpdateException(String message, Throwable th) {
    super(message, th);
  }

  public EntityUpdateException(String message) {
    super(message);
  }
}
