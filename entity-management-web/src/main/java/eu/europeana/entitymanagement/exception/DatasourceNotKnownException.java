package eu.europeana.entitymanagement.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown during de-referenciation, if datasource returns an empty response, or 404 response.
 * Considered to be caused by a bad user request.
 */
public class DatasourceNotKnownException extends EuropeanaApiException {
    public DatasourceNotKnownException(String message) {
        super(message);
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
        return HttpStatus.BAD_REQUEST;
    }
}
