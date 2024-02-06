package eu.europeana.entitymanagement.exception;

public class ApplicationInitializationException extends Exception {
  
  public ApplicationInitializationException(String message) {
    super(message);
  }

  public ApplicationInitializationException(String message, Throwable th) {
    super(message, th);
  }
}
