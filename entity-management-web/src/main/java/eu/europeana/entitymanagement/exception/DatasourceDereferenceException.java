package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid response is received from external datasource.
 *
 * {@link DatasourceNotKnownException} should be used if the response is valid but empty (equivalent to HTTP 404 status code)
 */
public class DatasourceDereferenceException extends EuropeanaApiException {
    public DatasourceDereferenceException(String msg) {
        super(msg);
    }

    public DatasourceDereferenceException(String msg, Throwable t) {
        super(msg, t);
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_GATEWAY;
    }
}
