package eu.europeana.entitymanagement.common.exception;

public class HttpClientException extends Exception {

  public HttpClientException(String message) {
    super(message);
  }
  public HttpClientException(String message, Throwable ex) {
    super(message, ex);
  }

}
