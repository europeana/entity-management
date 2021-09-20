package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown during dereferencing when external datasource is down
 */
public class DatasourceNotReachableException extends EuropeanaApiException {

  public DatasourceNotReachableException(String msg) {
    super(msg);
  }

  @Override
  public boolean doExposeMessage() {
    // suppress message for this exception, as it typically contains response from upstream server
    return false;
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
  public HttpStatus getResponseStatus() {
    return HttpStatus.GATEWAY_TIMEOUT;
  }
}
