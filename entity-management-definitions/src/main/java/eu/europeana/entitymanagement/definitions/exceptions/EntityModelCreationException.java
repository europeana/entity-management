package eu.europeana.entitymanagement.definitions.exceptions;

public class EntityModelCreationException extends Exception {

  public EntityModelCreationException(String message) {
    super(message);
  }

  public EntityModelCreationException(String message, Throwable th) {
    super(message, th);
  }
}
