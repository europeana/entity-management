package eu.europeana.entitymanagement.definitions.exceptions;

public class EntityModelCreationException extends RuntimeException {

  public EntityModelCreationException(String message) {
    super(message);
  }

  public EntityModelCreationException(String message, Throwable th) {
    super(message, th);
  }
}
