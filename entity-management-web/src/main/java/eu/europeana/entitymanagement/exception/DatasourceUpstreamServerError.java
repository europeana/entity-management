package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/** Exception thrown during dereferencing when the response from the upstream server is invalid */
@SuppressWarnings("serial")
public class DatasourceUpstreamServerError extends EuropeanaApiException {

  public DatasourceUpstreamServerError(String msg) {
    super(msg);
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.BAD_GATEWAY;
  }
}
